package com.quanxiaoha.xiaohashu.note.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/12 15:30
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UnlikeNoteReqVO {

    @NotNull(message = "笔记 id 不能为空")
    private Long id;
}
