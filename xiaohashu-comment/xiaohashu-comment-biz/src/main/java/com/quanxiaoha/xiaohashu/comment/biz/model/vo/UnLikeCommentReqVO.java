package com.quanxiaoha.xiaohashu.comment.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @author lbwxxc
 * @date 2025/7/2 10:14
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UnLikeCommentReqVO {

    @NotNull(message = "评论 id 不能为空")
    private Long commentId;
}
