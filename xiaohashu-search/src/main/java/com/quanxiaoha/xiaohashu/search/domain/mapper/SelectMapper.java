package com.quanxiaoha.xiaohashu.search.domain.mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/21 10:56
 * @description:
 */
public interface SelectMapper {

    /**
     * 查询笔记文档所需的全字段数据
     */
    List<Map<String, Object>> selectEsNoteIndexData(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 查询用户索引所需的全字段数据
     * @param userId
     * @return
     */
    List<Map<String, Object>> selectEsUserIndexData(@Param("userId") Long userId);
}
