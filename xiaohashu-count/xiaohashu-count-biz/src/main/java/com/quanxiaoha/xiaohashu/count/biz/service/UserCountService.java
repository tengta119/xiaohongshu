package com.quanxiaoha.xiaohashu.count.biz.service;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.count.dto.FindUserCountsByIdReqDTO;
import com.quanxiaoha.xiaohashu.count.dto.FindUserCountsByIdRspDTO;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/7/7 10:42
 * @description:
 */
public interface UserCountService {

    /**
     * 查询用户相关计数
     */
    Response<FindUserCountsByIdRspDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);
}
