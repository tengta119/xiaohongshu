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
public enum NoteStatusEnum {

    BE_EXAMINE(0), // 待审核
    NORMAL(1), // 正常展示
    DELETED(2), // 被删除
    DOWNED(3), // 被下架
    ;

    private final Integer code;
}
