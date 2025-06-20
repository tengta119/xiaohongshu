package com.quanxiaoha.xiaohashu.search.model.vo;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/6/20 17:36
 * @description: 搜索用户
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchUserReqVO {

    @NotBlank(message = "搜索关键字不能为空")
    private String keyword;

    @Min(value = 1, message = "页码不能小于 1")
    // 默认值为第一页
    private Integer pageNo = 1;
}
