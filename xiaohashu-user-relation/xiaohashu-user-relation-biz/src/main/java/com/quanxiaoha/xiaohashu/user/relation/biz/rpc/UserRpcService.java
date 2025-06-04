package com.quanxiaoha.xiaohashu.user.relation.biz.rpc;


import cn.hutool.core.collection.CollUtil;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.user.api.UserFeignApi;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUserByIdReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUsersByIdsReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author lbwxxc
 * @date 2025/6/2 15:58
 * @description:
 */
@Component
@Slf4j
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    public FindUserByIdRspDTO findById(Long id) {
        Response<FindUserByIdRspDTO> response = userFeignApi.findById(new FindUserByIdReqDTO(id));

        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }
        return response.getData();
    }

    /**
     * 批量查询用户信息
     *
     * @param userIds
     * @return
     */
    public List<FindUserByIdRspDTO> findByIds(List<Long> userIds) {
        Response<List<FindUserByIdRspDTO>> response = userFeignApi.findByIds(new FindUsersByIdsReqDTO(userIds));
        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }
        return response.getData();
    }
}
