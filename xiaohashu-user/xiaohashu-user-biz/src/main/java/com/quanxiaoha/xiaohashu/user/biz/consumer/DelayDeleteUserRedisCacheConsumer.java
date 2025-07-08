package com.quanxiaoha.xiaohashu.user.biz.consumer;


import com.quanxiaoha.xiaohashu.user.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.user.biz.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;

/**
 * @author lbwxxc
 * @date 2025/7/8 15:54
 * @description:
 */
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_DELAY_DELETE_USER_REDIS_CACHE,
        topic = MQConstants.TOPIC_DELAY_DELETE_USER_REDIS_CACHE)
public class DelayDeleteUserRedisCacheConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        Long userId = Long.valueOf(body);
        log.info("## 延迟消息消费成功, userId: {}", userId);

        // 删除 Redis 用户缓存
        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);
        String userProfileRedisKey = RedisKeyConstants.buildUserProfileKey(userId);
        // 批量删除
        redisTemplate.delete(Arrays.asList(userInfoRedisKey, userProfileRedisKey));
    }
}
