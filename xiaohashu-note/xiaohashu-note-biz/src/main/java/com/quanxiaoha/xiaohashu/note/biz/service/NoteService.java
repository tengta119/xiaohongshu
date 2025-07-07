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
     */
    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);

    /**
     * 笔记详情
     */
    Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    /**
     * 笔记更新
     */
    Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO);

    /**
     * 删除本地笔记缓存
     */
    void deleteNoteLocalCache(Long noteId);

    /**
     * 删除笔记
     */
    Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO);

    /**
     * 笔记置顶 / 取消置顶
     */
    Response<?> topNote(TopNoteReqVO topNoteReqVO);

    /**
     * 点赞笔记
     */
    Response<?> likeNote(LikeNoteReqVO likeNoteReqVO);

    /**
     * 取消点赞笔记
     */
    Response<?> unlikeNote(UnlikeNoteReqVO unlikeNoteReqVO);

    /**
     * 收藏笔记
     */
    Response<?> collectNote(CollectNoteReqVO collectNoteReqVO);

    /**
     * 取消收藏笔记
     */
    Response<?> unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO);

    /**
     * 获取是否点赞、收藏数据
     */
    Response<FindNoteIsLikedAndCollectedRspVO> isLikedAndCollectedData(FindNoteIsLikedAndCollectedReqVO findNoteIsLikedAndCollectedReqVO);
}
