package com.quanxiaoha.xiaohashu.comment.biz.service.impl;


import com.google.common.base.Preconditions;
import com.quanxiaoha.framework.biz.context.holder.LoginUserContextHolder;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.comment.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.comment.biz.model.dto.PublishCommentMqDTO;
import com.quanxiaoha.xiaohashu.comment.biz.model.vo.PublishCommentReqVO;
import com.quanxiaoha.xiaohashu.comment.biz.retry.SendMqRetryHelper;
import com.quanxiaoha.xiaohashu.comment.biz.service.CommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
/**
 * @author lbwxxc
 * @date 2025/6/22 09:41
 * @description:
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private SendMqRetryHelper sendMqRetryHelper;

    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {

        String content = publishCommentReqVO.getContent();
        String imageUrl = publishCommentReqVO.getImageUrl();
        // content 和 imageUrl 不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl), "评论正文和图片不能同时为空");


        // 发布者 ID
        Long creatorId = LoginUserContextHolder.getUserId();
        // 发送 MQ
        // 构建消息体 DTO
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .noteId(publishCommentReqVO.getNoteId())
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();


        // 发送 MQ (包含重试机制)
        sendMqRetryHelper.asyncSend(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));

        return Response.success();
    }

}
