package com.quanxiaoha.xiaohashu.note.biz.constant;


/**
 * @author lbwxxc
 * @date 2025/6/2 09:16
 * @description:
 */
public interface MQConstants {

    /**
     * Topic 主题：删除笔记本地缓存
     */
    String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";

    /**
     * Topic 主题：延迟双删 Redis 笔记缓存
     */
    String TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE = "DelayDeleteNoteRedisCacheTopic";

    /**
     * Topic: 点赞、取消点赞共用一个
     */
    String TOPIC_LIKE_OR_UNLIKE = "LikeUnlikeTopic";

    /**
     * 点赞标签
     */
    String TAG_LIKE = "Like";

    /**
     * Tag 标签：取消点赞
     */
    String TAG_UNLIKE = "Unlike";

    /**
     * Topic: 计数 - 笔记点赞数
     */
    String TOPIC_COUNT_NOTE_LIKE = "CountNoteLikeTopic";

    /**
     * Topic: 收藏、取消收藏共用一个
     */
    String TOPIC_COLLECT_OR_UN_COLLECT = "CollectUnCollectTopic";

    /**
     * Tag 标签：收藏
     */
    String TAG_COLLECT = "Collect";

    /**
     * Tag 标签：取消收藏
     */
    String TAG_UN_COLLECT = "UnCollect";
}
