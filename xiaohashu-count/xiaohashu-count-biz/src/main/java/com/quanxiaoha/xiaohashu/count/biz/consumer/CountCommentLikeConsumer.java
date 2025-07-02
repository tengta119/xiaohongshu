package com.quanxiaoha.xiaohashu.count.biz.consumer;


import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.count.biz.enums.LikeUnlikeCommentTypeEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.AggregationCountLikeUnlikeCommentMqDTO;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountLikeUnlikeCommentMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lbwxxc
 * @date 2025/7/2 10:38
 * @description:
 */
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_count_" + MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE,
        topic = MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE)
@Component
@Slf4j
public class CountCommentLikeConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage) // 设置消费者方法
            .build();

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【评论点赞数】聚合消息, size: {}", bodys.size());
        log.info("==> 【评论点赞数】聚合消息, {}", JsonUtils.toJsonString(bodys));

        List<CountLikeUnlikeCommentMqDTO> countLikeUnlikeCommentMqDTOS = bodys.stream()
                .map(body -> JsonUtils.parseObject(body, CountLikeUnlikeCommentMqDTO.class))
                .toList();

        Map<Long, List<CountLikeUnlikeCommentMqDTO>> commentIdAndCommentMap = countLikeUnlikeCommentMqDTOS.stream()
                .collect(Collectors.groupingBy(CountLikeUnlikeCommentMqDTO::getCommentId));

        List<AggregationCountLikeUnlikeCommentMqDTO> countList = Lists.newArrayList();
        for (Map.Entry<Long, List<CountLikeUnlikeCommentMqDTO>> entry : commentIdAndCommentMap.entrySet()) {

            Long commentId = entry.getKey();
            List<CountLikeUnlikeCommentMqDTO> comments = entry.getValue();
            int finalCount = 0;
            for (CountLikeUnlikeCommentMqDTO commentMqDTO : comments) {
                LikeUnlikeCommentTypeEnum likeUnlikeCommentTypeEnum = LikeUnlikeCommentTypeEnum.valueOf(commentMqDTO.getType());
                if (Objects.isNull(likeUnlikeCommentTypeEnum)) {
                    throw new RuntimeException("出错了");
                }
                switch (likeUnlikeCommentTypeEnum) {
                    case LIKE -> finalCount++;
                    case UNLIKE -> finalCount--;
                }
            }
            countList.add(AggregationCountLikeUnlikeCommentMqDTO.builder().commentId(commentId).count(finalCount).build());
        }

        log.info("## 【评论点赞数】聚合后的计数数据: {}", JsonUtils.toJsonString(countList));

        // 更新 Redis
        countList.forEach(item -> {
            // 评论 ID
            Long commentId = item.getCommentId();
            // 聚合后的计数
            Integer count = item.getCount();

            // Redis 中评论计数 Hash Key
            String countCommentRedisKey = RedisKeyConstants.buildCountCommentKey(commentId);
            // 判断 Redis 中 Hash 是否存在
            boolean isCountCommentExisted = redisTemplate.hasKey(countCommentRedisKey);

            // 若存在才会更新
            // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
            if (isCountCommentExisted) {
                // 对目标用户 Hash 中的点赞数字段进行计数操作
                redisTemplate.opsForHash().increment(countCommentRedisKey, RedisKeyConstants.FIELD_LIKE_TOTAL, count);
            }
        });

        // 发送 MQ, 评论点赞数据落库
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countList))
                .build();

        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_COMMENT_LIKE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：评论点赞数写库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：评论点赞数写库】MQ 发送异常: ", throwable);
            }
        });

    }
}
