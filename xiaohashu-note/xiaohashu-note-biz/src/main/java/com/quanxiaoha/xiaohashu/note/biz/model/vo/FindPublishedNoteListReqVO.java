package com.quanxiaoha.xiaohashu.note.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/7/9 11:19
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindPublishedNoteListReqVO {

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 游标，即笔记 ID，用于分页使用
     */
    private Long cursor;
}
