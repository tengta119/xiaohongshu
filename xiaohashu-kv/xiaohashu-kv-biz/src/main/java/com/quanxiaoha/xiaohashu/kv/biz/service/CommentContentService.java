package com.quanxiaoha.xiaohashu.kv.biz.service;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.dto.req.BatchAddCommentContentReqDTO;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/22 10:56
 * @description:
 */
public interface CommentContentService {

    /**
     * 批量添加评论内容
     */
    Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);
}
