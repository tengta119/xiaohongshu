package com.quanxiaoha.framework.common.util;


import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author lbwxxc
 * @date 2025/6/2 16:24
 * @description: 日期工具类
 */
public class DateUtils {

    public static long localDateTime2Timestamp(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
