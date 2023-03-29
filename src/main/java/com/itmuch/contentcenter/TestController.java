package com.itmuch.contentcenter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.feignclient.TestBaiduFeignClient;
import com.itmuch.contentcenter.feignclient.TestUserCenterFeignClient;
import com.itmuch.contentcenter.rocketmq.MySource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private ShareMapper shareMapper;

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/test")
    public List<Share> testInsert(){
        Share share = new Share();
        share.setCreateTime(new Date());
        share.setUpdateTime(new Date());
        share.setTitle("xxx");
        share.setCover("xxx");
        share.setAuthor("chuan");
        share.setBuyCount(1);

        this.shareMapper.insertSelective(share);

        List<Share> shares = this.shareMapper.selectAll();
        return shares;
    }

    @GetMapping("/testnacos")
    public List<ServiceInstance> setDiscoveryClient(){
        //查询指定服务的所有实例的信息
        return this.discoveryClient.getInstances("user-center");
    }


    @Autowired
    private TestUserCenterFeignClient testUserCenterFeignClient;
    @GetMapping("/testFeign")
    public UserDTO testUserCenter(UserDTO userDTO){
        return testUserCenterFeignClient.query(userDTO);
//        return testUserCenterFeignClient.query(userDTO.getId(),userDTO.getWxId());
    }

    @Autowired
    private TestBaiduFeignClient testBaiduFeignClient;
    @GetMapping("/baidu")
    public String testBaidu(){
        return testBaiduFeignClient.index();
    }

    @Autowired
    private TestService testService;
    @GetMapping("/test-a")
    public String testA(){
        testService.common();
        return "test-a";
    }
    @GetMapping("/test-b")
    public String testB(){
        testService.common();
        return "test-b";
    }

//    @GetMapping("/test-sentinel-api")
//    public String testSentinelAPI(@RequestParam(required = false) String a){
//
//        String resourceName = "test-sentinel-api";
//
//        //定义一个sentinel保护的资源,资源名称是test-sentinel-api
//        Entry entry = null;
//        try {
//            entry = SphU.entry(resourceName);
//            //被保护的业务逻辑
//            if (StringUtils.isBlank(a)){
//                throw new IllegalArgumentException("a cannot be empty");
//            }
//            return a;
//        }
//        //如果被保护的资源被限流或者降级了，就会抛BlockException
//        catch (BlockException e) {
//            log.warn("限流或者降级了",e);
//            return "限流或者降级了";
//        }
//        //注意：sentinel默认请求下之后统计BlockException及其子类，对于try中的抛出的IllegalArgumentException异常，它根本不统计
//        catch (IllegalArgumentException e){
//            //统计IllegalArgumentException【发生的次数、发生占比】
//            Tracer.trace(e);
//            return "参数非法";
//        }
//        finally {
//            if (entry != null){
//                entry.exit();
//            }
//        }
//    }

    @GetMapping("/test-sentinel-api")
    /**
     * test-sentinel-api:被保护的资源名称
     * blockHandler:指定被保护的资源被限流或者降级了，触发的方法
     * fallback: 指定降级之后，处理的方法
     */
    @SentinelResource(value = "test-sentinel-api",blockHandler = "block",fallback = "fallback")
    public String testSentinelResource(@RequestParam(required = false) String a){

        //被保护的业务逻辑
        if (StringUtils.isBlank(a)){
            throw new IllegalArgumentException("a cannot be empty");
        }
        return a;
    }

    /**
     * 处理限流或者降级
     * @param a
     * @param e
     * @return
     */
    public String block(String a,BlockException e){
        log.warn("限流或者降级了 block",e);
        return "限流或者降级了 block";
    }

    /**
     * 处理降级
     * @param a
     * @return
     */
    public String fallback(String a,Throwable e){
        return "限流或者降级了 fallback" + e.getMessage();
    }

    @Autowired
    private RestTemplate restTemplate;
    @GetMapping("/test-rest-template-sentinel/{userId}")
    public UserDTO testSentinelRestTemplate(@PathVariable Integer userId){

        return this.restTemplate.getForObject("http://user-center/users/{userId}",
                UserDTO.class,userId);
    }

    @GetMapping("/tokenRelay/{userId}")
    public ResponseEntity<UserDTO> tokenRelay(@PathVariable Integer userId, HttpServletRequest request){
        String token = request.getHeader("x-token");
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-token",token);
        headers.add("x-tanid","yyy");
        return this.restTemplate
                .exchange("http://user-center/users/{userId}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    UserDTO.class,
                        userId);
    }

    @Autowired
    private Source source;
    @GetMapping("/test-stream")
    public String testStream(){
        this.source.output()
                .send(
                    MessageBuilder
                            .withPayload("消息体咳咳")
                            .build()
                );
        return "success";
    }

    @Autowired
    private MySource mySource;
    @GetMapping("/test-mystream")
    public String testMyStream(){
        this.mySource.output()
                .send(
                        MessageBuilder
                                .withPayload("自定义接口生产--消息，消息体：嘻嘻")
                                .build()
                );
        return "success1";
    }

    @GetMapping("/test-condition")
    public String testStreamCondition(){
        this.source.output()
                .send(MessageBuilder.withPayload("myheader-消息体")
                .setHeader("my-header","myheader")
                .build());
        return "success condition";
    }
}
















