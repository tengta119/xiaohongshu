package com.quanxiaoha.xiaohashu.count.biz.constant;


/**
 * @author lbwxxc
 * @date 2025/6/9 16:06
 * @description:
 */
public interface MQConstants {

    /**
     * Topic: 关注数计数
     */
    String TOPIC_COUNT_FOLLOWING = "CountFollowingTopic";

    /**
     * Topic: 粉丝数计数
     */
    String TOPIC_COUNT_FANS = "CountFansTopic";

    /**
     * Topic: 粉丝数计数入库
     */
    String TOPIC_COUNT_FANS_2_DB = "CountFans2DBTopic";

    /**
     * Topic: 粉丝数计数入库
     */
    String TOPIC_COUNT_FOLLOWING_2_DB = "CountFollowing2DBTopic";
}
