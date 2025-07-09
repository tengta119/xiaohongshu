package com.quanxiaoha.xiaohashu.note.biz.domain.mapper;

import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteDO record);

    int insertSelective(NoteDO record);

    NoteDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteDO record);

    int updateByPrimaryKey(NoteDO record);

    int updateIsTop(NoteDO noteDO);

    // 查询笔记是否存在，如果存在则返回 1
    int selectCountByNoteId(Long noteId);

    /**
     * 查询笔记的发布者用户 ID
     */
    Long selectCreatorIdByNoteId(Long noteId);

    /**
     * 查询个人主页已发布笔记列表
     */
    List<NoteDO> selectPublishedNoteListByUserIdAndCursor(@Param("creatorId") Long creatorId,
                                                          @Param("cursor") Long cursor);

}