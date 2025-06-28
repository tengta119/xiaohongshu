package com.quanxiaoha.xiaohashu.comment.biz.constants;


/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/22 09:51
 * @description:
 */
public interface MQConstants {

    /**
     * Topic: 评论发布
     */
    String TOPIC_PUBLISH_COMMENT = "PublishCommentTopic";

    /**
     * Topic: 笔记评论总数计数
     */
    String TOPIC_COUNT_NOTE_COMMENT = "CountNoteCommentTopic";

    /**
     * Topic: 评论热度值更新
     */
    String TOPIC_COMMENT_HEAT_UPDATE = "CommentHeatUpdateTopic";
}
