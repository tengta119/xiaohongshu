package com.quanxiaoha.xiaohashu.note.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/2 10:23
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeleteNoteReqVO {

    @NotNull(message = "笔记 id 不能为空")
    private Long id;
}
