package com.quanxiaoha.xiaohashu.comment.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lbwxxc
 * @date 2025/6/22 11:50
 * @description:
 */
@AllArgsConstructor
@Getter
public enum CommentLevelEnum {
    // 一级评论
    ONE(1),
    // 二级评论
    TWO(2),
    ;

    private final Integer code;

}
