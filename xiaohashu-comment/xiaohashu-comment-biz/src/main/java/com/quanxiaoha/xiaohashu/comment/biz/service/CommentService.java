package com.quanxiaoha.xiaohashu.comment.biz.service;


import com.quanxiaoha.framework.common.response.PageResponse;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.comment.biz.model.vo.FindCommentItemRspVO;
import com.quanxiaoha.xiaohashu.comment.biz.model.vo.FindCommentPageListReqVO;
import com.quanxiaoha.xiaohashu.comment.biz.model.vo.PublishCommentReqVO;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/22 09:40
 * @description: 评论服务
 */
public interface CommentService {

    /**
     * 发布评论
     */
    Response<?> publishComment(PublishCommentReqVO publishCommentReqVO);

    /**
     * 评论列表分页查询
     */
    PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO);
}
