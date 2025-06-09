package com.quanxiaoha.xiaohashu.count.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/9 16:38
 * @description:
 */
@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {
    FOLLOW(1),
    UNFOLLOW(0);

    private final Integer type;

    public static FollowUnfollowTypeEnum valueOf(Integer type) {
        for (FollowUnfollowTypeEnum followUnfollowTypeEnum : FollowUnfollowTypeEnum.values()) {
            if (followUnfollowTypeEnum.getType().equals(type)) {

                return followUnfollowTypeEnum;
            }
        }
        return null;
    }
}
