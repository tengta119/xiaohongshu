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
     * 构建完整的笔记详情 KEY
     * @param noteId
     * @return
     */
    public static String buildNoteDetailKey(Long noteId) {
        return NOTE_DETAIL_KEY + noteId;
    }

}
