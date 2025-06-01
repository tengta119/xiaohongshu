package com.quanxiaoha.xiaohashu.note.biz.rpc;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.user.api.UserFeignApi;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUserByIdReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author lbwxxc
 * @date 2025/6/1 20:00
 * @description: 用户服务远程调用
 */
@Component
@Slf4j
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    public FindUserByIdRspDTO findById(Long id) {
        Response<FindUserByIdRspDTO> byId = userFeignApi.findById(new FindUserByIdReqDTO(id));
        FindUserByIdRspDTO findUserByIdRspDTO = byId.getData();
        if (Objects.nonNull(findUserByIdRspDTO)) {
            return findUserByIdRspDTO;
        }
        return null;
    }

}
