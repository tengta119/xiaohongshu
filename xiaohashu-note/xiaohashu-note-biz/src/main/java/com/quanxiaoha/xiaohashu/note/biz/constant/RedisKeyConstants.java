package com.quanxiaoha.xiaohashu.note.biz.constant;


/**
 * @author lbwxxc
 * @date 2025/6/1 20:19
 * @description:
 */
public class RedisKeyConstants {

    /**
     * 笔记详情 KEY 前缀
     */
    public static final String NOTE_DETAIL_KEY = "note:detail:";

    /**
     * 布隆过滤器：用户笔记点赞
     */
    public static final String BLOOM_USER_NOTE_LIKE_LIST_KEY = "bloom:note:likes:";

    /**
     * 用户笔记点赞列表 ZSet 前缀
     */
    public static final String USER_NOTE_LIKE_ZSET_KEY = "user:note:likes:";

    /**
     * 布隆过滤器：用户笔记收藏 前缀
     */
    public static final String BLOOM_USER_NOTE_COLLECT_LIST_KEY = "bloom:note:collects:";

    /**
     * 用户笔记收藏列表 ZSet 前缀
     */
    public static final String USER_NOTE_COLLECT_ZSET_KEY = "user:note:collects:";

    /**
     * Roaring Bitmap：用户笔记点赞 前缀
     */
    public static final String R_BITMAP_USER_NOTE_LIKE_LIST_KEY = "rbitmap:note:likes:";


    /**
     * Roaring Bitmap：用户笔记收藏 前缀
     */
    public static final String R_BITMAP_USER_NOTE_COLLECT_LIST_KEY = "rbitmap:note:collects:";

    /**
     * 已发布笔记列表 KEY 前缀
     */
    private static final String PUBLISHED_NOTE_LIST_KEY = "note:published:list:";

    /**
     * 构建完整的已发布笔记列表 KEY
     */
    public static String buildPublishedNoteListKey(Long userId) {
        return PUBLISHED_NOTE_LIST_KEY + userId;
    }

    /**
     * 构建完整的 Roaring Bitmap：用户笔记收藏 KEY
     */
    public static String buildRBitmapUserNoteCollectListKey(Long userId) {
        return R_BITMAP_USER_NOTE_COLLECT_LIST_KEY + userId;
    }

    /**
     * 构建完整的 Roaring Bitmap：用户笔记点赞 KEY
     */
    public static String buildRBitmapUserNoteLikeListKey(Long userId) {
        return R_BITMAP_USER_NOTE_LIKE_LIST_KEY + userId;
    }

    /**
     * 构建完整的用户笔记收藏列表 ZSet KEY
     */
    public static String buildUserNoteCollectZSetKey(Long userId) {
        return USER_NOTE_COLLECT_ZSET_KEY + userId;
    }

    /**
     * 构建完整的布隆过滤器：用户笔记收藏 KEY
     */
    public static String buildBloomUserNoteCollectListKey(Long userId) {
        return BLOOM_USER_NOTE_COLLECT_LIST_KEY + userId;
    }

    /**
     * 构建完整的用户笔记点赞列表 ZSet KEY
     */
    public static String buildUserNoteLikeZSetKey(Long userId) {
        return USER_NOTE_LIKE_ZSET_KEY + userId;
    }

    /**
     * 构建完整的笔记详情 KEY
     */
    public static String buildNoteDetailKey(Long noteId) {
        return NOTE_DETAIL_KEY + noteId;
    }

    /**
     * 构建完整的布隆过滤器：用户笔记点赞 KEY
     */
    public static String buildBloomUserNoteLikeListKey(Long userId) {
        return BLOOM_USER_NOTE_LIKE_LIST_KEY + userId;
    }

}
