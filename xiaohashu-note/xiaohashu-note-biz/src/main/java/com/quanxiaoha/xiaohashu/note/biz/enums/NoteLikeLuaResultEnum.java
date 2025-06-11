package com.quanxiaoha.xiaohashu.note.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/10 17:55
 * @description:
 */
@AllArgsConstructor
@Getter
public enum NoteLikeLuaResultEnum {
    // 布隆过滤器不存在
    BLOOM_NOT_EXIST(-1L),
    // 笔记已点赞
    NOTE_LIKED(1L),
    ;

    private final Long code;

    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code
     * @return
     */
    public static NoteLikeLuaResultEnum valueOf(Long code) {
        for (NoteLikeLuaResultEnum noteLikeLuaResultEnum : NoteLikeLuaResultEnum.values()) {
            if (Objects.equals(code, noteLikeLuaResultEnum.getCode())) {
                return noteLikeLuaResultEnum;
            }
        }
        return null;
    }

}
