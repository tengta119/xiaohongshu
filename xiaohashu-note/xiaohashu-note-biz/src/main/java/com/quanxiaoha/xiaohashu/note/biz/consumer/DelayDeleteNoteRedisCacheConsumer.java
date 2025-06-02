package com.quanxiaoha.xiaohashu.note.biz.consumer;


import com.quanxiaoha.xiaohashu.note.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author lbwxxc
 * @date 2025/6/2 10:15
 * @description: 延时删除 Redis 笔记缓存
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE,
        topic = MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE
)
public class DelayDeleteNoteRedisCacheConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        Long noteId = Long.valueOf(body);
        log.info("## 延迟消息消费成功, noteId: {}", noteId);

        // 删除 Redis 笔记缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);
    }
}
