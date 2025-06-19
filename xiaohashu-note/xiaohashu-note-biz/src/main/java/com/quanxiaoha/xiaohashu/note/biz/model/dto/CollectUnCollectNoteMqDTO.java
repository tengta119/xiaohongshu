package com.quanxiaoha.xiaohashu.note.biz.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author lbwxxc
 * @date 2025/6/19 15:10
 * @description: 收藏、取消收藏
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CollectUnCollectNoteMqDTO {

    private Long userId;

    private Long noteId;

    /**
     * 0: 取消收藏， 1：收藏
     */
    private Integer type;

    private LocalDateTime createTime;

    private Long creatorId;
}
