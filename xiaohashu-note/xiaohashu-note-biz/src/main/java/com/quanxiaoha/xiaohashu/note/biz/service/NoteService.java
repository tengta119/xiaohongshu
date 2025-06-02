package com.quanxiaoha.xiaohashu.note.biz.service;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.*;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/1 16:03
 * @description:
 */
public interface NoteService {


    /**
     * 笔记发布
     * @param publishNoteReqVO
     * @return
     */
    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);

    /**
     * 笔记详情
     * @param findNoteDetailReqVO
     * @return
     */
    Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    /**
     * 笔记更新
     * @param updateNoteReqVO
     * @return
     */
    Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO);

    /**
     * 删除本地笔记缓存
     * @param noteId
     */
    void deleteNoteLocalCache(Long noteId);

    /**
     * 删除笔记
     * @param deleteNoteReqVO
     * @return
     */
    Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO);
}
