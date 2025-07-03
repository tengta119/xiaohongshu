package com.quanxiaoha.xiaohashu.count.biz.consumer;


import cn.hutool.core.collection.CollUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.CommentDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.enums.CommentLevelEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountPublishCommentMqDTO;
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
import java.util.Set;
import java.util.stream.Collectors;
/**
 * @author lbwxxc
 * @date 2025/6/27 11:05
 * @description: 二级评论计数
 */
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_child_comment_total" + MQConstants.TOPIC_COUNT_NOTE_COMMENT, topic = MQConstants.TOPIC_COUNT_NOTE_COMMENT)
@Component
@Slf4j
public class CountNoteChildCommentConsumer implements RocketMQListener<String> {

    @Resource
    CommentDOMapper commentDOMapper;
    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(5000)
            .batchSize(1000)
            .linger(Duration.ofSeconds(1))
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【笔记二级评论数】聚合消息, size: {}", bodys.size());
        log.info("==> 【笔记二级评论数】聚合消息, {}", JsonUtils.toJsonString(bodys));

        List<CountPublishCommentMqDTO> countPublishCommentMqDTOS = Lists.newArrayList();
        bodys.forEach(body -> {
            try {
                countPublishCommentMqDTOS.addAll(JsonUtils.parseList(body, CountPublishCommentMqDTO.class));
            } catch (Exception e) {
                log.error("==> 解码出现错误, body: {}", body, e);
            }
        });
        Map<Long, List<CountPublishCommentMqDTO>> groupMap = countPublishCommentMqDTOS.stream()
                .filter(countPublishCommentMqDTO -> Objects.equals(countPublishCommentMqDTO.getLevel(), CommentLevelEnum.TWO.getCode()))
                .collect(Collectors.groupingBy(CountPublishCommentMqDTO::getParentId));

        // 不存在二级评论则直接返回
        if (CollUtil.isEmpty(groupMap)) {
            return;
        }

        for (Map.Entry<Long, List<CountPublishCommentMqDTO>> entry : groupMap.entrySet()) {
            Long parentId = entry.getKey();
            int count = entry.getValue().size();

            String key = RedisKeyConstants.buildCountCommentKey(parentId);
            Boolean hasKey = redisTemplate.hasKey(key);
            if (hasKey) {
                redisTemplate.opsForHash()
                        .increment(key, RedisKeyConstants.FIELD_CHILD_COMMENT_TOTAL, count);
            }

            // 更新一级评论的下级评论总数，进行累加操作
            commentDOMapper.updateChildCommentTotal(parentId, count);
        }


        Set<Long> commentIds = groupMap.keySet();
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(commentIds)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COMMENT_HEAT_UPDATE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【评论热度值更新】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【评论热度值更新】MQ 发送异常: ", throwable);
            }
        });
    }
}
