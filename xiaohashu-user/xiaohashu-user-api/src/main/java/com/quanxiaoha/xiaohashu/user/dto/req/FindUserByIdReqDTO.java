package com.quanxiaoha.xiaohashu.user.dto.req;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/1 19:19
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindUserByIdReqDTO {

    @NotNull(message = "用户 id 不能为空")
    Long id;
}
