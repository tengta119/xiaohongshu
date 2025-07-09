package com.quanxiaoha.xiaohashu.count.biz.service;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.count.dto.FindNoteCountsByIdRspDTO;
import com.quanxiaoha.xiaohashu.count.dto.FindNoteCountsByIdsReqDTO;

import java.util.List;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/7/9 11:41
 * @description:
 */
public interface NoteCountService {

    /**
     * 批量查询笔记计数
     */
    Response<List<FindNoteCountsByIdRspDTO>> findNotesCountData(FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO);
}
