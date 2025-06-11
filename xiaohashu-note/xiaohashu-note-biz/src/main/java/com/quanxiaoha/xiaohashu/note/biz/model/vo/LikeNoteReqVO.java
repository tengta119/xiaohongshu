package com.quanxiaoha.xiaohashu.note.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/10 17:42
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LikeNoteReqVO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long id;
}
