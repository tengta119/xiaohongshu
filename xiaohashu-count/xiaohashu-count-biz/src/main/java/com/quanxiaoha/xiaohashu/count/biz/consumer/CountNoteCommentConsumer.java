package com.quanxiaoha.xiaohashu.count.biz.consumer;


import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.github.phantomthief.collection.BufferTrigger;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.NoteCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountPublishCommentMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lbwxxc
 * @date 2025/6/24 11:19
 * @description:
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COUNT_NOTE_COMMENT ,topic = MQConstants.TOPIC_COUNT_NOTE_COMMENT)
public class CountNoteCommentConsumer implements RocketMQListener<String> {

    @Resource
    private NoteCountDOMapper  noteCountDOMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(1000)
            .batchSize(1000)
            .linger(Duration.ofSeconds(1))
            .setConsumerEx(this::consumerMessage)
            .build();


    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);
    }

    private void consumerMessage(List<String> bodys) {
        log.info("==> 【笔记评论数】聚合消息, size: {}", bodys.size());
        log.info("==> 【笔记评论数】聚合消息, {}", JsonUtils.toJsonString(bodys));

        // 将聚合后的消息体 Json 转 List<CountPublishCommentMqDTO>
        List<CountPublishCommentMqDTO> countPublishCommentMqDTOList = Lists.newArrayList();
        bodys.forEach(body -> {
            try {
                List<CountPublishCommentMqDTO> countPublishCommentMqDTOS = JsonUtils.parseList(body, CountPublishCommentMqDTO.class);
                countPublishCommentMqDTOList.addAll(countPublishCommentMqDTOS);
            } catch (Exception e) {
                log.error("{}", body);
            }
        });
        Map<Long, List<CountPublishCommentMqDTO>> groupMap = countPublishCommentMqDTOList.stream()
                .collect(Collectors.groupingBy(CountPublishCommentMqDTO::getNoteId));
        for (Map.Entry<Long, List<CountPublishCommentMqDTO>> entry : groupMap.entrySet()) {
            Long noteId = entry.getKey();
            int count = entry.getValue().size();

            String key = RedisKeyConstants.buildCountNoteKey(noteId);
            Boolean hasKey = redisTemplate.hasKey(key);
            if (hasKey) {
                redisTemplate.opsForHash().increment(key, RedisKeyConstants.FIELD_COMMENT_TOTAL, count);
            }

            noteCountDOMapper.insertOrUpdateCommentTotalByNoteId(count, noteId);
        }
    }
}
