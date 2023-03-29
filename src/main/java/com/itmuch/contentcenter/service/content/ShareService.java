package com.itmuch.contentcenter.service.content;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itmuch.contentcenter.dao.content.MidUserShareMapper;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.dao.messaging.RocketmqTransactionLogMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.itmuch.contentcenter.domain.dto.user.UserAddBonusDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.MidUserShare;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.domain.entity.messaging.RocketmqTransactionLog;
import com.itmuch.contentcenter.domain.enums.AuditStatusEnum;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {

    private final ShareMapper shareMapper;
//    private final RestTemplate restTemplate;

    private final UserCenterFeignClient userCenterFeignClient;

    private final RocketMQTemplate rocketMQTemplate;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    private final Source source;
    private final MidUserShareMapper midUserShareMapper;

//    private final DiscoveryClient discoveryClient;

    public ShareDTO findById(Integer id,String token){
        //获取分享详情
        Share share = this.shareMapper.selectByPrimaryKey(id);
        //发布人id
        Integer userId = share.getUserId();

//        //用nacos客户端拿到user-center所有实例信息
//        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
//        //获取所有用户中心实例的请求地址
//        List<String> targetURLs = instances.stream().map(instance -> instance.getUri().toString() + "/users/{id}")
//                .collect(Collectors.toList());
//
//        int i = ThreadLocalRandom.current().nextInt(targetURLs.size());
//
//        log.info("请求的目标地址：{}",targetURLs.get(i));

//        //用HTTP GET方法去请求，获得user_center的用户信息
//        UserDTO userDTO = this.restTemplate.getForObject(
//                targetURLs.get(i),
//                UserDTO.class, userId
//        );

        //使用Ribbon,可以根据服务名user-center请求
//        UserDTO userDTO = this.restTemplate.getForObject(
//                "http://user-center/users/{id}",
//                UserDTO.class, userId
//        );

        //使用feign
        UserDTO userDTO = this.userCenterFeignClient.findById(userId,token);

        ShareDTO shareDTO = new ShareDTO();
        //消息装配
        BeanUtils.copyProperties(share,shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }

    /**
     * rocketMq(rocketMQTemplate)实现分布式事务
     * @param id
     * @param auditDTO
     * @return
     */
//    public Share auditById(Integer id,ShareAuditDTO auditDTO){
//        //1、查询share是否存在,不存在或者当前的audit_status != NOT_YET,那么跑异常
//        Share share = this.shareMapper.selectByPrimaryKey(id);
//        if (share == null){
//            throw new IllegalArgumentException("参数非法! 该分享不存在!");
//        }
//        if (!Objects.equals("NOT_YET",share.getAuditStatus())){
//            throw new IllegalArgumentException("参数非法! 分享已审核通过或者不通过!");
//        }
//
//
//        //3、如果是PASS，那么发送消息给rocketmq，让用户中心去消费，并为发布人添加积分
//        if (AuditStatusEnum.PASS.equals(auditDTO.getAuditStatusEnum())){
//            //1、发送半消息
//            String transactionId = RocketMQHeaders.TRANSACTION_ID;
//            this.rocketMQTemplate.sendMessageInTransaction(
//                    "tx-add-bonus-group","add-bonus",
//                    MessageBuilder.withPayload(UserAddBonusMsgDTO.builder()
//                            .userId(share.getUserId())
//                            .bonus(50)
//                            .build())
//                            .setHeader(transactionId, UUID.randomUUID().toString())
//                            .setHeader("share_id",id)
//                            .build(),
//                    auditDTO
//                    );
//        }else {
//            //审核资源，将状态设为PASS/REJECT
//            auditByIdInDB(id,auditDTO);
//        }
//
//
//        //异步执行
////        userCenterFeignClient.addBonus(id,500);
//
//        return share;
//    }

    /**
     * spring cloud stream 加 rocketmq实现分布式事务
     * @param id
     * @param auditDTO
     * @return
     */
    public Share auditById(Integer id,ShareAuditDTO auditDTO){
        //1、查询share是否存在,不存在或者当前的audit_status != NOT_YET,那么跑异常
        Share share = this.shareMapper.selectByPrimaryKey(id);
        if (share == null){
            throw new IllegalArgumentException("参数非法! 该分享不存在!");
        }
        if (!Objects.equals("NOT_YET",share.getAuditStatus())){
            throw new IllegalArgumentException("参数非法! 分享已审核通过或者不通过!");
        }


        //3、如果是PASS，那么发送消息给rocketmq，让用户中心去消费，并为发布人添加积分
        if (AuditStatusEnum.PASS.equals(auditDTO.getAuditStatusEnum())){
            String transactionId = RocketMQHeaders.TRANSACTION_ID;
            //1、发送半消息
            this.source.output()
                    .send(
                        MessageBuilder.withPayload(UserAddBonusMsgDTO.builder()
                                .userId(share.getUserId())
                                .bonus(50)
                                .build())
                                .setHeader(transactionId, UUID.randomUUID().toString())
                                .setHeader("share_id",id)
                                .setHeader("dto", JSON.toJSONString(auditDTO))
                                .build());

        }else {
            //审核资源，将状态设为PASS/REJECT
            auditByIdInDB(id,auditDTO);
        }
        return share;
    }

    public void auditByIdInDB(Integer id,ShareAuditDTO auditDTO){
        Share share = Share.builder()
                .id(id)
                .auditStatus(auditDTO.getAuditStatusEnum().toString())
                .reason(auditDTO.getReason())
                .build();
        this.shareMapper.updateByPrimaryKeySelective(share);
    }

    /**
     * 为了MQ Server消息回查，所以需要在本地事务完成之后，记录一条记录在表中，来确认本地事务是否执行成功
     * @param id
     * @param auditDTO
     * @param transactionId
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditByIdWithRocketMqLog(Integer id,ShareAuditDTO auditDTO,String transactionId){
        this.auditByIdInDB(id,auditDTO);

        this.rocketmqTransactionLogMapper.insertSelective(RocketmqTransactionLog.builder()
                .transactionId(transactionId)
                .log("审核分享")
                .build());
    }

    public PageInfo<Share> q(String title, Integer pageNo, Integer pageSize, Integer userId) {
        //它会切入下面这条不分页的sql,自动拼接分页的sql
        //本质就是利用了mybatis的拦截器，在sql上加入了limit语句
        PageHelper.startPage(pageNo,pageSize);
        List<Share> shares = this.shareMapper.selectByParam(title);

        //1、如果用户未登录，那么downloadUrl全部设置为空
        List<Share> sharesDealed = new ArrayList<>();
        if (userId == null){
            sharesDealed = shares.stream().peek(share -> {
                share.setDownloadUrl(null);
            }).collect(Collectors.toList());
        }
        //2、如果用户登录了，那么查询一下mid_user_share，如果没有数据，那么这条share的
        //   downloadUrl也设置为空
        else {
            sharesDealed = shares.stream().peek(share -> {
                MidUserShare midUserShare = this.midUserShareMapper.selectOne(
                        MidUserShare.builder()
                                .shareId(share.getId())
                                .userId(userId)
                                .build());
                if (midUserShare == null){
                    share.setDownloadUrl(null);
                }
            }).collect(Collectors.toList());
        }

        return new PageInfo<>(sharesDealed);
    }


    public Share exchangeById(Integer id, HttpServletRequest request) {
        //1、根据id查询share，校验是否存在
        Share share = this.shareMapper.selectByPrimaryKey(id);
        if (share == null){
            throw new IllegalArgumentException("该分享不存在!");
        }

        //2、根据当前登录的用户id,查询积分是否够
        Integer userId = (Integer)request.getAttribute("id");
        String token = request.getHeader("x-token");
        //如果当前用户已经兑换过该分享，则直接返回
        MidUserShare midUserShare = this.midUserShareMapper.selectOne(
                MidUserShare.builder()
                        .userId(userId)
                        .shareId(id)
                        .build());
        if (midUserShare != null){
            return share;
        }

        UserDTO userDTO = this.userCenterFeignClient.findById(userId, token);
        Integer price = share.getPrice();
        if (price > userDTO.getBonus()){
            throw new IllegalArgumentException("用户积分不够!");
        }
        //3、扣减积分 & 往mid_user_share里插入一条数据
        this.userCenterFeignClient.addBonus(
                UserAddBonusDTO.builder()
                        .userId(userId)
                        .bonus(0 - price)
                        .build());

        this.midUserShareMapper.insert(
                MidUserShare.builder()
                        .userId(userId)
                        .shareId(id)
                        .build());
        return share;
    }
}















