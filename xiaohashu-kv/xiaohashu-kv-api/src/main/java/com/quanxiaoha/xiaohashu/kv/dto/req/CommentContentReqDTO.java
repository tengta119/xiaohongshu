package com.quanxiaoha.xiaohashu.kv.dto.req;


import cn.hutool.crypto.digest.mac.MacEngine;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/22 10:52
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentContentReqDTO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long noteId;

    @NotBlank(message = "发布年月不能为空")
    private String yearMonth;

    @NotBlank(message = "评论正文 ID 不能为空")
    private String contentId;

    @NotBlank(message = "评论正文不能为空")
    private String content;

}
