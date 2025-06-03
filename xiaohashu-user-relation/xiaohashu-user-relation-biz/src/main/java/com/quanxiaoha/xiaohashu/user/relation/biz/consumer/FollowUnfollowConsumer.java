package com.quanxiaoha.xiaohashu.user.relation.biz.consumer;


import com.quanxiaoha.framework.common.util.DateUtils;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.user.relation.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.user.relation.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.dataobject.FansDO;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.dataobject.FollowingDO;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.mapper.FansDOMapper;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.dto.FollowUserMqDTO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.dto.UnfollowUserMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.apache.rocketmq.common.message.Message;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 * @author lbwxxc
 * @date 2025/6/2 17:34
 * @description:
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
        consumeMode = ConsumeMode.ORDERLY
)
@Slf4j
public class FollowUnfollowConsumer implements RocketMQListener<Message> {

    @Resource
    private FollowingDOMapper followingDOMapper;
    @Resource
    private FansDOMapper fansDOMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message) {
        // 消息体
        String bodyJsonStr = new String(message.getBody());
        // 标签
        String tags = message.getTags();

        log.info("==> FollowUnfollowConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        // 根据 MQ 标签，判断操作类型
        if (Objects.equals(tags, MQConstants.TAG_FOLLOW)) { // 关注
            handleFollowTagMessage(bodyJsonStr);
        } else if (Objects.equals(tags, MQConstants.TAG_UNFOLLOW)) { // 取关
            handleUnfollowTagMessage(bodyJsonStr);
        }
    }

    /**
     * 关注
     * @param bodyJsonStr
     */
    private void handleFollowTagMessage(String bodyJsonStr) {

        FollowUserMqDTO followUserMqDTO = JsonUtils.parseObject(bodyJsonStr, FollowUserMqDTO.class);
        // 判空
        if (Objects.isNull(followUserMqDTO)) {
            return;
        }
        // 幂等性：通过联合唯一索引保证

        Long userId = followUserMqDTO.getUserId();
        Long followUserId = followUserMqDTO.getFollowUserId();
        LocalDateTime createTime = followUserMqDTO.getCreateTime();

        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                int count = followingDOMapper.insert(FollowingDO.builder().
                        userId(userId)
                        .followingUserId(followUserId)
                        .createTime(createTime).build());
                if (count > 0) {
                    fansDOMapper.insert(FansDO.builder()
                            .userId(followUserId)
                            .fansUserId(userId)
                            .createTime(createTime)
                            .build());
                }
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("", e);
            }
            return false;
        }));

        log.info("## 数据库添加记录结果：{}", isSuccess);
        // 若数据库操作成功，更新 Redis 中被关注用户的 ZSet 粉丝列表
        if (isSuccess) {
            // Lua 脚本
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
            script.setResultType(Long.class);

            // 时间戳
            long timestamp = DateUtils.localDateTime2Timestamp(createTime);

            // 构建被关注用户的粉丝列表 Redis Key
            String fansRedisKey = RedisKeyConstants.buildUserFansKey(followUserId);
            // 执行脚本
            redisTemplate.execute(script, Collections.singletonList(fansRedisKey), userId, timestamp);
        }
    }

    /**
     * 取关
     * @param bodyJsonStr
     */
    private void handleUnfollowTagMessage(String bodyJsonStr) {
        UnfollowUserMqDTO unfollowUserMqDTO = JsonUtils.parseObject(bodyJsonStr, UnfollowUserMqDTO.class);
        if (Objects.isNull(unfollowUserMqDTO)) {
            return;
        }

        Long userId = unfollowUserMqDTO.getUserId();
        Long unfollowUserId = unfollowUserMqDTO.getUnfollowUserId();
        LocalDateTime createTime = unfollowUserMqDTO.getCreateTime();

        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                int count = followingDOMapper.deleteByUserIdAndFollowingUserId(userId, unfollowUserId);
                if (count > 0) {
                    fansDOMapper.deleteByUserIdAndFansUserId(unfollowUserId, userId);
                }
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("", e);
            }
            return false;
        }));

        // 若数据库删除成功，更新 Redis，将自己从被取注用户的 ZSet 粉丝列表删除
        if (isSuccess) {
            // 被取关用户的粉丝列表 Redis Key
            String fansRedisKey = RedisKeyConstants.buildUserFansKey(unfollowUserId);
            // 删除指定粉丝
            redisTemplate.opsForZSet().remove(fansRedisKey, userId);
        }

    }
}
