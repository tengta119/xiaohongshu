package com.quanxiaoha.xiaohashu.user.relation.biz.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author lbwxxc
 * @date 2025/6/3 14:33
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UnfollowUserMqDTO {

    private Long userId;

    private Long unfollowUserId;

    private LocalDateTime createTime;
}
