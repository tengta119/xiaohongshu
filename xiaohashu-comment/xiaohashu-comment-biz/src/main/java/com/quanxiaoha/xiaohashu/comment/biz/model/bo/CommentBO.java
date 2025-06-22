package com.quanxiaoha.xiaohashu.comment.biz.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * @author lbwxxc
 * @date 2025/6/22 11:50
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentBO {
    private Long id;

    private Long noteId;

    private Long userId;

    private String contentUuid;

    private String content;

    private Boolean isContentEmpty;

    private String imageUrl;

    private Integer level;

    private Long replyTotal;

    private Long likeTotal;

    private Long parentId;

    private Long replyCommentId;

    private Long replyUserId;

    private Boolean isTop;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}