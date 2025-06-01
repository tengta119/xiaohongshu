package com.quanxiaoha.xiaohashu.note.biz.service;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.PublishNoteReqVO;

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
}
