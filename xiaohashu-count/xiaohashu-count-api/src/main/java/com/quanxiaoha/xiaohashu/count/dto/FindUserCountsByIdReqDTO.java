package com.quanxiaoha.xiaohashu.count.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @author lbwxxc
 * @date 2025/7/7 10:40
 * @description:
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class FindUserCountsByIdReqDTO {

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;
}
