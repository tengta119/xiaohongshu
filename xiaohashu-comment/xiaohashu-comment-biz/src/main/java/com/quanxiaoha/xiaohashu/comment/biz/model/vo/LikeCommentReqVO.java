package com.quanxiaoha.xiaohashu.comment.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/7/1 08:36
 * @description: 请求点赞评论
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LikeCommentReqVO {

    @NotNull(message = "评论 Id 不能为空")
    private Long commentId;
}
