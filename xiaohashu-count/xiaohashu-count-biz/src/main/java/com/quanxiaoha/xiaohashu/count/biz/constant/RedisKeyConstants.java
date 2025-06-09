package com.quanxiaoha.xiaohashu.count.biz.constant;


/**
 * @author lbwxxc
 * @date 2025/6/9 16:40
 * @description:
 */
public class RedisKeyConstants {

    /**
     * 用户维度计数 Key 前缀
     */
    private static final String COUNT_USER_KEY_PREFIX = "count:user:";

    /**
     * Hash Field: 粉丝总数
     */
    public static final String FIELD_FANS_TOTAL = "fansTotal";

    /**
     * Hash Field: 关注总数
     */
    public static final String FIELD_FOLLOWING_TOTAL = "followingTotal";


    /**
     * 构建用户维度计数 Key
     * @param userId
     * @return
     */
    public static String buildCountUserKey(Long userId) {
        return COUNT_USER_KEY_PREFIX + userId;
    }
}
