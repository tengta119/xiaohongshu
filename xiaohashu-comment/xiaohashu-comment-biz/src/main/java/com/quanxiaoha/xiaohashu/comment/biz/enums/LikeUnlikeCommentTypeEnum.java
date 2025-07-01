package com.quanxiaoha.xiaohashu.comment.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lbwxxc
 * @date 2025/7/1 09:06
 * @description: 评论点赞、取消点赞 Type
 */
@Getter
@AllArgsConstructor
public enum LikeUnlikeCommentTypeEnum {
    // 点赞
    LIKE(1),
    // 取消点赞
    UNLIKE(0),
    ;

    private final Integer code;

}