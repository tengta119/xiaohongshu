package com.quanxiaoha.xiaohashu.user.relation.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/2 15:53
 * @description: 关注用户
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowUserReqVO {

    @NotNull(message = "被关注用户 ID 不能为空")
    private Long followUserId;
}
