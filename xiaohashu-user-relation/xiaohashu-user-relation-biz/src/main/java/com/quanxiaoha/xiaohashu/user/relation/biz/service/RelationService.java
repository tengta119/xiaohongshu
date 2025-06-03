package com.quanxiaoha.xiaohashu.user.relation.biz.service;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.FollowUserReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.UnfollowUserReqVO;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/2 15:55
 * @description:
 */
public interface RelationService {


    /**
     * 关注用户
     * @param followUserReqVO
     * @return
     */
    Response<?> follow(FollowUserReqVO followUserReqVO);

    /**
     * 取关用户
     * @param unfollowUserReqVO
     * @return
     */
    Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO);
}
