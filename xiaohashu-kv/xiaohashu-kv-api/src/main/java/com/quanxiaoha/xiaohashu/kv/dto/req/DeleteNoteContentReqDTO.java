package com.quanxiaoha.xiaohashu.kv.dto.req;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/5/31 20:26
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeleteNoteContentReqDTO {

    @NotBlank(message = "笔记 uuid 不能为空")
    private String uuid;
}
