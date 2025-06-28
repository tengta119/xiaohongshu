package com.quanxiaoha.xiaohashu.comment.biz.constants;


/**
 * @author lbwxxc
 * @date 2025/6/28 11:45
 * @description:
 */
public class RedisKeyConstants {

    /**
     * Key 前缀：一级评论的 first_reply_comment_id 字段值是否更新标识
     */
    private static final String HAVE_FIRST_REPLY_COMMENT_KEY_PREFIX = "comment:havaFirstReplyCommentId:";



    /**
     * 构建完整 KEY
     */
    public static String buildHaveFirstReplyCommentKey(Long commentId) {
        return HAVE_FIRST_REPLY_COMMENT_KEY_PREFIX + commentId;
    }
}
