package com.quanxiaoha.xiaohashu.comment.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommentDO {
    private Long id;

    private Long noteId;

    private Long userId;

    private String contentUuid;

    private Boolean isContentEmpty;

    private String imageUrl;

    private Byte level;

    private Long replyTotal;

    private Long likeTotal;

    private Long parentId;

    private Long replyCommentId;

    private Long replyUserId;

    private Byte isTop;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long childCommentTotal;

    private BigDecimal heat;

    private Long firstReplyCommentId;

}