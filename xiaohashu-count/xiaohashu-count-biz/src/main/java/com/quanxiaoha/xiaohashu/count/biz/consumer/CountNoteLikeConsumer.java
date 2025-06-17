package com.quanxiaoha.xiaohashu.count.biz.consumer;


import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Maps;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.count.biz.enums.LikeUnlikeNoteTypeEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountLikeUnlikeNoteMqDTO;
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
 * @date 2025/6/17 17:23
 * @description: 计数: 笔记点赞数
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COUNT_NOTE_LIKE,
        topic = MQConstants.TOPIC_COUNT_NOTE_LIKE)
@Slf4j
public class CountNoteLikeConsumer implements RocketMQListener<String> {

    @Resource
    RedisTemplate<String, Object> redisTemplate;
    @Resource
    RocketMQTemplate rocketMQTemplate;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(5000)
            .batchSize(1000)
            .linger(Duration.ofMillis(1))
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【笔记点赞数】聚合消息, size: {}", bodys.size());
        log.info("==> 【笔记点赞数】聚合消息, {}", JsonUtils.toJsonString(bodys));

        // List<String> 转 List<CountLikeUnlikeNoteMqDTO>
        List<CountLikeUnlikeNoteMqDTO> countLikeUnlikeNoteMqDTOS = bodys.stream()
                .map(body -> JsonUtils.parseObject(body, CountLikeUnlikeNoteMqDTO.class)).toList();

        // 按笔记 ID 进行分组
        Map<Long, List<CountLikeUnlikeNoteMqDTO>> groupMap = countLikeUnlikeNoteMqDTOS.stream()
                .collect(Collectors.groupingBy(CountLikeUnlikeNoteMqDTO::getNoteId));

        // 按组汇总数据，统计出最终的计数
        // key 为笔记 ID, value 为最终操作的计数
        Map<Long, Integer> countMap = Maps.newHashMap();

        for (Map.Entry<Long, List<CountLikeUnlikeNoteMqDTO>> entry : groupMap.entrySet()) {
            List<CountLikeUnlikeNoteMqDTO> list = entry.getValue();
            // 最终的计数值，默认为 0
            int finalCount = 0;
            for (CountLikeUnlikeNoteMqDTO countLikeUnlikeNoteMqDTO : list) {
                Integer type = countLikeUnlikeNoteMqDTO.getType();
                LikeUnlikeNoteTypeEnum likeUnlikeNoteTypeEnum = LikeUnlikeNoteTypeEnum.valueOf(type);
                // 若枚举为空，跳到下一次循环
                if (Objects.isNull(likeUnlikeNoteTypeEnum)) continue;
                switch (likeUnlikeNoteTypeEnum) {
                    case LIKE -> finalCount++;
                    case UNLIKE -> finalCount--;
                }
            }
            // 将分组后统计出的最终计数，存入 countMap 中
            countMap.put(entry.getKey(), finalCount);
        }

        log.info("## 【笔记点赞数】聚合后的计数数据: {}", JsonUtils.toJsonString(countMap));

        // 更新 Redis
        countMap.forEach((k, v) -> {
            // Redis Key
            String redisKey = RedisKeyConstants.buildCountNoteKey(k);
            // 判断 Redis 中 Hash 是否存在
            boolean isExisted = redisTemplate.hasKey(redisKey);

            // 若存在才会更新
            // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
            if (isExisted) {
                // 对目标用户 Hash 中的点赞数字段进行计数操作
                redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_LIKE_TOTAL, v);
            }
        });

        // 发送 MQ, 笔记点赞数据落库
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countMap))
                .build();

        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：笔记点赞数入库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：笔记点赞数入库】MQ 发送异常: ", throwable);
            }
        });
    }
}
