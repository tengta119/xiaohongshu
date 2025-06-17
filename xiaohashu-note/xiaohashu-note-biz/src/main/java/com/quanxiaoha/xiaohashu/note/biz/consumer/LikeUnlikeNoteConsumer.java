package com.quanxiaoha.xiaohashu.note.biz.consumer;


import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.note.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteLikeDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.NoteLikeDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.note.biz.model.dto.LikeUnlikeNoteMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * @author lbwxxc
 * @date 2025/6/12 14:30
 * @description:
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_LIKE_OR_UNLIKE,
        topic = MQConstants.TOPIC_LIKE_OR_UNLIKE,
        consumeMode = ConsumeMode.ORDERLY
)
public class LikeUnlikeNoteConsumer implements RocketMQListener<Message> {

    @Resource
    NoteLikeDOMapper noteLikeDOMapper;
    @Resource
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {

        String bodyJsonStr = new String(message.getBody());
        String tags = message.getTags();
        log.info("==> LikeUnlikeNoteConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        if (Objects.equals(tags, MQConstants.TAG_LIKE)) {
            handleLikeNoteTagMessage(bodyJsonStr);
        } else if (Objects.equals(tags, MQConstants.TAG_UNLIKE)) {
            handleUnlikeNoteTagMessage(bodyJsonStr);
        } else {
            throw new BizException(ResponseCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 笔记点赞
     * @param bodyJsonStr
     */
    private void handleLikeNoteTagMessage(String bodyJsonStr) {
        // 消息体 JSON 字符串转 DTO
        LikeUnlikeNoteMqDTO likeNoteMqDTO = JsonUtils.parseObject(bodyJsonStr, LikeUnlikeNoteMqDTO.class);

        if (Objects.isNull(likeNoteMqDTO)) return;

        // 用户ID
        Long userId = likeNoteMqDTO.getUserId();
        // 点赞的笔记ID
        Long noteId = likeNoteMqDTO.getNoteId();
        // 操作类型
        Integer type = likeNoteMqDTO.getType();
        // 点赞时间
        LocalDateTime createTime = likeNoteMqDTO.getCreateTime();

        // 构建 DO 对象
        NoteLikeDO noteLikeDO = NoteLikeDO.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();

        // 添加或更新笔记点赞记录
        int count = noteLikeDOMapper.insertOrUpdate(noteLikeDO);

        //发送计数 MQ
        if (count == 0) {
            return;
        }
        sendMQLikeUnLikeCount(bodyJsonStr, true);
    }

    /**
     * 笔记取消点赞
     * @param bodyJsonStr
     */
    private void handleUnlikeNoteTagMessage(String bodyJsonStr) {
        // 消息体 JSON 字符串转 DTO
        LikeUnlikeNoteMqDTO unlikeNoteMqDTO = JsonUtils.parseObject(bodyJsonStr, LikeUnlikeNoteMqDTO.class);

        if (Objects.isNull(unlikeNoteMqDTO)) return;

        // 用户ID
        Long userId = unlikeNoteMqDTO.getUserId();
        // 点赞的笔记ID
        Long noteId = unlikeNoteMqDTO.getNoteId();
        // 操作类型
        Integer type = unlikeNoteMqDTO.getType();
        // 点赞时间
        LocalDateTime createTime = unlikeNoteMqDTO.getCreateTime();

        // 构建 DO 对象
        NoteLikeDO noteLikeDO = NoteLikeDO.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();

        // 取消点赞：记录更新
        int count = noteLikeDOMapper.update2UnlikeByUserIdAndNoteId(noteLikeDO);

        // 发送计数 MQ
        if (count == 0) {
            return;
        }
        sendMQLikeUnLikeCount(bodyJsonStr, false);
    }

    public void sendMQLikeUnLikeCount(String bodyJsonStr, boolean isLike) {
        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(bodyJsonStr).build();

        if (isLike) {
            rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("==> 【计数: 笔记点赞】MQ 发送成功，SendResult: {}", sendResult);
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("==> 【计数: 笔记点赞】MQ 发送异常: ", throwable);
                }
            });
        } else {
            // 更新数据库成功后，发送计数 MQ

            // 异步发送 MQ 消息
            rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("==> 【计数: 笔记取消点赞】MQ 发送成功，SendResult: {}", sendResult);
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("==> 【计数: 笔记取消点赞】MQ 发送异常: ", throwable);
                }
            });
        }

    }

}
