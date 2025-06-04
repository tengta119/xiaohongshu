package com.quanxiaoha.xiaohashu.user.relation.biz.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/4 19:11
 * @description: 查询关注列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFollowingUserRspVO {

    private Long userId;

    private String avatar;

    private String nickname;

    private String introduction;
}
