package com.quanxiaoha.xiaohashu.comment.biz.consumer;


import com.quanxiaoha.xiaohashu.comment.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.comment.biz.service.CommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author lbwxxc
 * @date 2025/7/2 11:37
 * @description:
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE,
        topic = MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE)
@Slf4j
public class DeleteCommentLocalCacheConsumer implements RocketMQListener<String> {

    @Resource
    private CommentService commentService;

    @Override
    public void onMessage(String body) {
        Long commentId = Long.valueOf(body);
        log.info("## 消费者消费成功, commentId: {}", commentId);

        commentService.deleteCommentLocalCache(commentId);
    }
}
