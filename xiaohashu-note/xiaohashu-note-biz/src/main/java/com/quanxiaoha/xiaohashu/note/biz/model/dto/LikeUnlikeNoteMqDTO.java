package com.quanxiaoha.xiaohashu.note.biz.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author lbwxxc
 * @date 2025/6/12 14:20
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LikeUnlikeNoteMqDTO {

    private Long userId;

    private Long noteId;

    /**
     * 0: 取消点赞， 1：点赞
     */
    private Integer type;

    private LocalDateTime createTime;

}
