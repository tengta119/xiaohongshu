package com.quanxiaoha.xiaohashu.count.biz.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/9 16:37
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CountFollowUnfollowMqDTO {
    /**
     * 原用户
     */
    private Long userId;

    /**
     * 目标用户
     */
    private Long targetUserId;

    /**
     * 1:关注 0:取关
     */
    private Integer type;

}
