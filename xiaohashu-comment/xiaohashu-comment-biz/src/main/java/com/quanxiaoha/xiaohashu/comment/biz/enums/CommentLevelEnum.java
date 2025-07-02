package com.quanxiaoha.xiaohashu.comment.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

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

    /**
     * 根据类型 code 获取对应的枚举
     */
    public static CommentLevelEnum valueOf(Integer code) {
        for (CommentLevelEnum commentLevelEnum : CommentLevelEnum.values()) {
            if (Objects.equals(code, commentLevelEnum.getCode())) {
                return commentLevelEnum;
            }
        }
        return null;
    }
}
