package com.quanxiaoha.xiaohashu.user.relation.biz.service;


import com.quanxiaoha.framework.common.response.PageResponse;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.*;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/6/2 15:55
 * @description:
 */
public interface RelationService {


    /**
     * 关注用户
     */
    Response<?> follow(FollowUserReqVO followUserReqVO);

    /**
     * 取关用户
     */
    Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO);

    /**
     * 查询关注列表
     */
    PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO);

    /**
     * 查询关注列表
     */
    PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO);
}
