package com.quanxiaoha.xiaohashu.comment.biz.consumer;


import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.comment.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.comment.biz.domain.dataobject.CommentDO;
import com.quanxiaoha.xiaohashu.comment.biz.domain.mapper.CommentDOMapper;
import com.quanxiaoha.xiaohashu.comment.biz.model.bo.CommentHeatBO;
import com.quanxiaoha.xiaohashu.comment.biz.util.HeatCalculator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lbwxxc
 * @date 2025/6/28 10:22
 * @description: 热度值计算
 */
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COMMENT_HEAT_UPDATE, topic = MQConstants.TOPIC_COMMENT_HEAT_UPDATE)
@Slf4j
@Component
public class CommentHeatUpdateConsumer implements RocketMQListener<String> {

    @Resource
    CommentDOMapper commentDOMapper;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
                    .bufferSize(5000)
                    .batchSize(1000)
                    .linger(Duration.ofSeconds(2))
                    .setConsumerEx(this::consumeMessage)
                    .build();



    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【评论热度值计算】聚合消息, size: {}", bodys.size());
        log.info("==> 【评论热度值计算】聚合消息, {}", JsonUtils.toJsonString(bodys));

        Set<Long> commentIds = Sets.newHashSet();
        bodys.forEach(body -> {
            try {
                commentIds.addAll(JsonUtils.parseSet(body, Long.class));
            } catch (Exception e) {
                log.error("", e);
            }
        });
        log.info("==> 去重后的评论 ID: {}", commentIds);

        List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(commentIds.stream().toList());

        // 评论 ID
        List<Long> ids = Lists.newArrayList();
        // 热度值 BO
        List<CommentHeatBO> commentBOS = Lists.newArrayList();
        commentDOS.forEach(commentDO -> {
            Long commentId = commentDO.getId();
            // 被点赞数
            Long likeTotal = commentDO.getLikeTotal();
            // 被回复数
            Long childCommentTotal = commentDO.getChildCommentTotal();

            // 计算热度值
            BigDecimal heatNum = HeatCalculator.calculateHeat(likeTotal, childCommentTotal);
            ids.add(commentId);
            commentBOS.add(CommentHeatBO.builder()
                    .id(commentId)
                    .heat(heatNum.doubleValue())
                    .build());
        });

        commentDOMapper.batchUpdateHeatByCommentIds(ids, commentBOS);
    }

}
