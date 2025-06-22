package com.quanxiaoha.xiaohashu.comment.biz.domain.dataobject;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLikeDO {
    private Long id;

    private Long userId;

    private Long commentId;

    private LocalDateTime createTime;


}