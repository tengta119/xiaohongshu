package com.quanxiaoha.xiaohashu.kv.dto.req;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/5/31 20:02
 * @description: 新增笔记内容
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AddNoteContentReqDTO {

    @NotNull(message = "笔记ID 不能为空")
    private Long noteId;

    @NotBlank(message = "笔记内容不能为空")
    private String content;
}
