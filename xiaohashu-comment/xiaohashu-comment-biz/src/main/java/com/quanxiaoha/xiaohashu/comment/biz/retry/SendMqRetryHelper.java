package com.quanxiaoha.xiaohashu.comment.biz.retry;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author lbwxxc
 * @date 2025/6/22 10:13
 * @description: 发送 MQ 重试工具类
 */
@Component
@Slf4j
public class SendMqRetryHelper {

    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private RetryTemplate retryTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 异步发送 MQ
     */
    public void asyncSend(String topic, String body) {
        log.info("==> 开始异步发送 MQ, Topic: {}, Body: {}", topic, body);

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(body)
                .build();

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【评论发布】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【评论发布】MQ 发送异常: ", throwable);
                handleRetry(topic, message);
            }
        });
    }

    /**
     * 重试处理
     */
    private void handleRetry(String topic, Message<String> message) {
        threadPoolTaskExecutor.submit(() -> {
            try {
                retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
                    log.info("==> 开始重试 MQ 发送, 当前重试次数: {}, 时间: {}", context.getRetryCount() + 1, LocalDateTime.now());
                    // 同步发送 MQ
                    rocketMQTemplate.syncSend(topic, message);
                    return null;
                });
            } catch (Exception e) {
                fallback(e, topic, message.getPayload());
            }

        });
    }


    /**
     * 兜底方案: 将发送失败的 MQ 写入数据库，之后，通过定时任务扫表，将发送失败的 MQ 再次发送，最终发送成功后，将该记录物理删除
     */
    private void fallback(Exception e, String topic, String bodyJson) {
        log.error("==> 多次发送失败, exception: {}, 进入兜底方案, Topic: {}, bodyJson: {}",e.getMessage(), topic, bodyJson);

        // TODO:
    }
}
