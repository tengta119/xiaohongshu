package com.quanxiaoha.xiaohashu.user.relation.biz.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/9 15:54
 * @description: 通知计数服务：关注、取关
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CountFollowUnfollowMqDTO {

    private Long userId;

    private Long targetUserId;

    /**
     * 1：关注、0：取关
     */
    private Integer type;
}
