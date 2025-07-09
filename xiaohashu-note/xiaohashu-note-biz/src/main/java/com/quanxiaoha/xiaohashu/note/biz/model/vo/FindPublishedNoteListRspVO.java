package com.quanxiaoha.xiaohashu.note.biz.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lbwxxc
 * @date 2025/7/9 11:21
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindPublishedNoteListRspVO {

    /**
     * 笔记分页数据
     */
    private List<NoteItemRspVO> notes;

    /**
     * 下一页的游标
     */
    private Long nextCursor;
}
