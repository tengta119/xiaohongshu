package com.quanxiaoha.xiaohashu.kv.dto.req;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lbwxxc
 * @date 2025/6/22 10:54
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BatchAddCommentContentReqDTO {

    @NotEmpty(message = "评论内容集合不能为空")
    @Valid  // 指定集合内的评论 DTO，也需要进行参数校验
    private List<CommentContentReqDTO> comments;
}
