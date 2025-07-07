package com.quanxiaoha.xiaohashu.note.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/7/7 09:51
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindNoteIsLikedAndCollectedReqVO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long noteId;
}
