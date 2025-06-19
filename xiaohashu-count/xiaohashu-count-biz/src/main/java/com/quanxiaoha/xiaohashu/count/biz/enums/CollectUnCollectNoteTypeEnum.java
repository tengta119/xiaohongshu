package com.quanxiaoha.xiaohashu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/19 16:36
 * @description: 笔记收藏、取消收藏 Type
 */
@Getter
@AllArgsConstructor
public enum CollectUnCollectNoteTypeEnum {
    // 收藏
    COLLECT(1),
    // 取消收藏
    UN_COLLECT(0),
    ;

    private final Integer code;

    public static CollectUnCollectNoteTypeEnum valueOf(Integer code) {
        for (CollectUnCollectNoteTypeEnum collectUnCollectNoteTypeEnum : CollectUnCollectNoteTypeEnum.values()) {
            if (Objects.equals(code, collectUnCollectNoteTypeEnum.getCode())) {
                return collectUnCollectNoteTypeEnum;
            }
        }
        return null;
    }

}
