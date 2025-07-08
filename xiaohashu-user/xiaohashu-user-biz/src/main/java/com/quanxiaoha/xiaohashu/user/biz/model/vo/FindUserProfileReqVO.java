package com.quanxiaoha.xiaohashu.user.biz.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/7/8 14:42
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindUserProfileReqVO {

    /**
     * 用户 ID
     */
    private Long userId;
}
