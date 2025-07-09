package com.quanxiaoha.xiaohashu.count.api;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.count.constant.ApiConstants;
import com.quanxiaoha.xiaohashu.count.dto.FindNoteCountsByIdRspDTO;
import com.quanxiaoha.xiaohashu.count.dto.FindNoteCountsByIdsReqDTO;
import com.quanxiaoha.xiaohashu.count.dto.FindUserCountsByIdReqDTO;
import com.quanxiaoha.xiaohashu.count.dto.FindUserCountsByIdRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/7/8 14:46
 * @description:
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface CountFeignApi {

    String PREFIX = "/count";

    @PostMapping(value = PREFIX + "/user/data")
    Response<FindUserCountsByIdRspDTO> findUserCount(@RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);

    @PostMapping(value = PREFIX + "/notes/data")
    public Response<List<FindNoteCountsByIdRspDTO>> findNotesCount(@RequestBody FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO);
}
