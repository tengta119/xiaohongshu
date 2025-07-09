package com.quanxiaoha.xiaohashu.count.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/7/9 11:41
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindNoteCountsByIdRspDTO {

    /**
     * 笔记 ID
     */
    private Long noteId;

    /**
     * 点赞数
     */
    private Long likeTotal;

    /**
     * 收藏数
     */
    private Long collectTotal;

    /**
     * 评论数
     */
    private Long commentTotal;
}
