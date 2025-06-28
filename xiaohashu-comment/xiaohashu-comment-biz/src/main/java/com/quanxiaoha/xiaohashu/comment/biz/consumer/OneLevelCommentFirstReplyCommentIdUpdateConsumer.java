package com.quanxiaoha.xiaohashu.comment.biz.consumer;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.comment.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.comment.biz.constants.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.comment.biz.domain.dataobject.CommentDO;
import com.quanxiaoha.xiaohashu.comment.biz.domain.mapper.CommentDOMapper;
import com.quanxiaoha.xiaohashu.comment.biz.enums.CommentLevelEnum;
import com.quanxiaoha.xiaohashu.comment.biz.model.dto.CountPublishCommentMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author lbwxxc
 * @date 2025/6/28 11:31
 * @description: 更新一级评论的 first_reply_comment_id 字段值
 */
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_first_reply_comment_id" + MQConstants.TOPIC_COUNT_NOTE_COMMENT,
        topic = MQConstants.TOPIC_COUNT_NOTE_COMMENT)
@Slf4j
@Component
public class OneLevelCommentFirstReplyCommentIdUpdateConsumer implements RocketMQListener<String> {


    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private CommentDOMapper commentDOMapper;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次（1s 一次）
            .setConsumerEx(this::consumeMessage) // 设置消费者方法
            .build();

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【一级评论 first_reply_comment_id 更新】聚合消息, size: {}", bodys.size());
        log.info("==> 【一级评论 first_reply_comment_id 更新】聚合消息, {}", JsonUtils.toJsonString(bodys));

        // 将聚合后的消息体 Json 转 List<CountPublishCommentMqDTO>
        List<CountPublishCommentMqDTO> publishCommentMqDTOS = Lists.newArrayList();
        bodys.forEach(body -> {
            try {
                List<CountPublishCommentMqDTO> list = JsonUtils.parseList(body, CountPublishCommentMqDTO.class);
                publishCommentMqDTOS.addAll(list);
            } catch (Exception e) {
                log.error("", e);
            }
        });

        // 过滤出二级评论的 parent_id（即一级评论 ID），并去重，需要更新对应一级评论的 first_reply_comment_id
        List<Long> parentIds = publishCommentMqDTOS.stream()
                .filter(publishCommentMqDTO -> Objects.equals(publishCommentMqDTO.getLevel(), CommentLevelEnum.TWO.getCode()))
                .map(CountPublishCommentMqDTO::getParentId)
                .distinct() // 去重
                .toList();

        if (CollUtil.isEmpty(parentIds)) {
            return;
        }

        List<String> keys = parentIds.stream().map(RedisKeyConstants::buildHaveFirstReplyCommentKey).toList();
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        List<Long> missingCommentIds = Lists.newArrayList();
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (Objects.isNull(values.get(i))) {
                    missingCommentIds.add(parentIds.get(i));
                }
            }
        }

        if (CollUtil.isNotEmpty(missingCommentIds)) {
            // 批量去数据库中查询
            List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(missingCommentIds);

            // 异步将 first_reply_comment_id 不为 0 的一级评论 ID, 同步到 redis 中
            threadPoolTaskExecutor.execute(() -> {
                List<Long> needSyncCommentIds = commentDOS.stream()
                        .filter(commentDO -> commentDO.getFirstReplyCommentId() != 0)
                        .map(CommentDO::getId)
                        .toList();
                sync2Redis(needSyncCommentIds);
            });

            // 过滤出值为 0 的，都需要更新其 first_reply_comment_id
            List<CommentDO> needUpdateCommentDOS = commentDOS.stream()
                    .filter(commentDO -> commentDO.getFirstReplyCommentId() == 0)
                    .toList();

            needUpdateCommentDOS.forEach(needUpdateCommentDO -> {
                // 一级评论 ID
                Long needUpdateCommentId = needUpdateCommentDO.getId();

                // 查询数据库，拿到一级评论最早回复的那条评论
                CommentDO earliestCommentDO = commentDOMapper.selectEarliestByParentId(needUpdateCommentId);

                if (Objects.nonNull(earliestCommentDO)) {
                    Long earliestCommentDOId = earliestCommentDO.getId();
                    commentDOMapper.updateFirstReplyCommentIdByPrimaryKey(earliestCommentDOId, needUpdateCommentId);
                    threadPoolTaskExecutor.execute(() -> sync2Redis(Lists.newArrayList(needUpdateCommentId)));
                }
            });
        }
    }

    /**
     * 同步到 Redis 中
     */
    private void sync2Redis(List<Long> needSyncCommentIds) {

        // 获取 ValueOperations
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
           needSyncCommentIds.forEach(needSyncCommentId  -> {
               // 构建 Redis Key
               String key = RedisKeyConstants.buildHaveFirstReplyCommentKey(needSyncCommentId);
               valueOperations.set(key, 1, RandomUtil.randomInt(5 * 60 * 60), TimeUnit.SECONDS);
           });
           return null;
        });
    }
}
