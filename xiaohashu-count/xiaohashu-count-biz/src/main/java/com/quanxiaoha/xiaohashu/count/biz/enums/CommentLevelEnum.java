package com.quanxiaoha.xiaohashu.count.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/27 11:22
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
