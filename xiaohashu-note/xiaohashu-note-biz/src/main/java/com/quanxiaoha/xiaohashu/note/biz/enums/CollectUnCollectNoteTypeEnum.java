package com.quanxiaoha.xiaohashu.note.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/19 15:11
 * @description:
 */
@AllArgsConstructor
@Getter
public enum CollectUnCollectNoteTypeEnum {

    COLLECT(1),
    UN_COLLECT(0);

    private final Integer code;
}
