package com.quanxiaoha.xiaohashu.kv.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.kenai.jffi.Array;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.biz.domain.dataobject.CommentContentDO;
import com.quanxiaoha.xiaohashu.kv.biz.domain.dataobject.CommentContentPrimaryKey;
import com.quanxiaoha.xiaohashu.kv.biz.domain.repository.CommentContentRepository;
import com.quanxiaoha.xiaohashu.kv.biz.service.CommentContentService;
import com.quanxiaoha.xiaohashu.kv.dto.req.*;
import com.quanxiaoha.xiaohashu.kv.dto.rsp.FindCommentContentRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author lbwxxc
 * @date 2025/6/22 10:57
 * @description:
 */
@Service
@Slf4j
public class CommentContentServiceImpl implements CommentContentService {

    @Resource
    private CassandraTemplate cassandraTemplate;
    @Resource
    private CommentContentRepository commentContentRepository;

    @Override
    public Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO) {

        List<CommentContentReqDTO> comments = batchAddCommentContentReqDTO.getComments();

        List<CommentContentDO> contentDOS = comments.stream().map(commentContentReqDTO -> {
            // 构建主键类
            CommentContentPrimaryKey commentContentPrimaryKey = CommentContentPrimaryKey.builder()
                    .noteId(commentContentReqDTO.getNoteId())
                    .yearMonth(commentContentReqDTO.getYearMonth())
                    .contentId(UUID.fromString(commentContentReqDTO.getContentId()))
                    .build();

            // DO 实体类
            CommentContentDO commentContentDO = CommentContentDO.builder()
                    .primaryKey(commentContentPrimaryKey)
                    .content(commentContentReqDTO.getContent())
                    .build();

            return commentContentDO;

        }).toList();

        cassandraTemplate.batchOps()
                .insert(contentDOS)
                .execute();

        return Response.success();
    }

    @Override
    public Response<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {
        Long noteId = batchFindCommentContentReqDTO.getNoteId();
        List<FindCommentContentReqDTO> commentContentKeys = batchFindCommentContentReqDTO.getCommentContentKeys();
        List<String> yearMonths = commentContentKeys.stream()
                .map(FindCommentContentReqDTO::getYearMonth)
                .distinct()
                .collect(Collectors.toList());
        List<UUID> contentIds = commentContentKeys.stream()
                .map(commentContentKey -> UUID.fromString(commentContentKey.getContentId()))
                .distinct()
                .collect(Collectors.toList());

        // 批量查询 Cassandra
        List<CommentContentDO> commentContentDOS  = commentContentRepository
                .findByPrimaryKeyNoteIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(noteId, yearMonths, contentIds);

        // DO 转 DTO
        List<FindCommentContentRspDTO> findCommentContentRspDTOS = Lists.newArrayList();
        if (CollUtil.isNotEmpty(commentContentDOS)) {
            findCommentContentRspDTOS = commentContentDOS.stream()
                    .map(commentContentDO -> FindCommentContentRspDTO.builder()
                            .contentId(String.valueOf(commentContentDO.getPrimaryKey().getContentId()))
                            .content(commentContentDO.getContent())
                            .build()
                    )
                    .toList();
        }

        return Response.success(findCommentContentRspDTOS);
    }

    @Override
    public Response<?> deleteCommentContent(DeleteCommentContentReqDTO deleteCommentContentReqDTO) {
        Long noteId = deleteCommentContentReqDTO.getNoteId();
        String yearMonth = deleteCommentContentReqDTO.getYearMonth();
        String contentId = deleteCommentContentReqDTO.getContentId();

        // 删除评论正文
        commentContentRepository.deleteByPrimaryKeyNoteIdAndPrimaryKeyYearMonthAndPrimaryKeyContentId(noteId, yearMonth, UUID.fromString(contentId));

        return Response.success();
    }

}
