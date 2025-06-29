package com.quanxiaoha.xiaohashu.kv.dto.req;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lbwxxc
 * @date 2025/6/29 09:49
 * @description: 批量查询评论内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchFindCommentContentReqDTO {

    @NotNull(message = "笔记 id 不能为空")
    private Long noteId;

    @NotEmpty(message = "评论内容 Key 集合")
    @Valid  // 指定集合中的 DTO 也需要进行参数校验
    private List<FindCommentContentReqDTO> commentContentKeys;
}
