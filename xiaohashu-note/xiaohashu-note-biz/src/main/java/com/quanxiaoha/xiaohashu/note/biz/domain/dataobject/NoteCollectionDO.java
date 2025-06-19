package com.quanxiaoha.xiaohashu.note.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NoteCollectionDO {
    private Long id;

    private Long userId;

    private Long noteId;

    private LocalDateTime createTime;

    private Byte status;

}