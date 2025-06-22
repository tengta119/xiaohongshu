package com.quanxiaoha.xiaohashu.kv.biz.service.impl;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.biz.domain.dataobject.CommentContentDO;
import com.quanxiaoha.xiaohashu.kv.biz.domain.dataobject.CommentContentPrimaryKey;
import com.quanxiaoha.xiaohashu.kv.biz.service.CommentContentService;
import com.quanxiaoha.xiaohashu.kv.dto.req.BatchAddCommentContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.dto.req.CommentContentReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
}
