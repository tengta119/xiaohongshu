package com.quanxiaoha.xiaohashu.user.relation.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/9 15:56
 * @description:
 */
@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {

    FOLLOW(1),
    UNFOLLOW(0);

    private final Integer code;
}
