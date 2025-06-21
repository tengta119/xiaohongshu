package com.quanxiaoha.xiaohashu.search.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/21 11:02
 * @description:
 */
@AllArgsConstructor
@Getter
public enum NoteVisibleEnum {

    PUBLIC(0), // 公开，所有人可见
    PRIVATE(1); // 仅自己可见

    private final Integer code;
}
