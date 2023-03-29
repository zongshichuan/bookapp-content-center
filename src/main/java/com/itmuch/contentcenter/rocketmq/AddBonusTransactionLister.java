package com.itmuch.contentcenter.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.itmuch.contentcenter.dao.messaging.RocketmqTransactionLogMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.entity.messaging.RocketmqTransactionLog;
import com.itmuch.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * RocketMq本地事务操作类
 */
@RocketMQTransactionListener(txProducerGroup = "tx-add-bonus-group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AddBonusTransactionLister implements RocketMQLocalTransactionListener {

    private final ShareService shareService;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    /**
     * 半消息发送成功后，生产者执行本地事务的操作
     *
      * @param message
     * @param arg  生产者使用sendMessageInTransaction(String txProducerGroup, String destination, Message<?> message,
     *            Object arg)方法发送消息时的arg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        //1、获取消息头信息
        MessageHeaders headers = message.getHeaders();
        String transactionId = (String)headers.get(RocketMQHeaders.TRANSACTION_ID);
        Integer shareId = Integer.valueOf((String) headers.get("share_id"));

        //使用spring cloud stream + rocketmq时，只能从header中拿去dto
        String dtoString = (String)headers.get("dto");
        ShareAuditDTO dto = JSON.parseObject(dtoString, ShareAuditDTO.class);
        //2、获取本地事务执行状态
        try {
            this.shareService.auditByIdWithRocketMqLog(shareId,dto,transactionId);
            //3-1、到这里说明，本地事务执行成功，给MQ Server确认成功提交
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            //3-2、到这里说明，本地事务执行失败，给MQ Server确认失败
            return RocketMQLocalTransactionState.ROLLBACK;
        }

    }

    /**
     * 消息长时间一直处于半消息的状态，MQ Server发送回查消息到生产者，生产者在这里拿到MQ Server的请求信息
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        MessageHeaders headers = message.getHeaders();
        String transactionId = (String)headers.get(RocketMQHeaders.TRANSACTION_ID);

        RocketmqTransactionLog transactionLog = this.rocketmqTransactionLogMapper.selectOne(
                RocketmqTransactionLog.builder()
                        .transactionId(transactionId)
                        .build()
        );
        if (transactionLog != null){
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}
