package com.quanxiaoha.xiaohashu.note.biz.rpc;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.api.KeyValueFeignApi;
import com.quanxiaoha.xiaohashu.kv.dto.req.AddNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.dto.req.DeleteNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.dto.req.FindNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.dto.rsp.FindNoteContentRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author lbwxxc
 * @date 2025/6/1 15:57
 * @description:
 */
@Component
public class KeyValueRpcService {

    @Resource
    private KeyValueFeignApi keyValueFeignApi;

    /**
     * 保存笔记内容
     *
     * @param uuid
     * @param content
     * @return
     */
    public boolean saveNoteContent(String uuid, String content) {
        AddNoteContentReqDTO addNoteContentReqDTO = new AddNoteContentReqDTO(uuid, content);
        Response<?> response = keyValueFeignApi.addNoteContent(addNoteContentReqDTO);
        return Objects.nonNull(response) && response.isSuccess();
    }

    /**
     * 删除笔记内容
     *
     * @param uuid
     * @return
     */
    public boolean deleteNoteContent(String uuid) {
        DeleteNoteContentReqDTO deleteNoteContentReqDTO = new DeleteNoteContentReqDTO(uuid);
        Response<?> response = keyValueFeignApi.deleteNoteContent(deleteNoteContentReqDTO);
        return Objects.nonNull(response) && response.isSuccess();
    }

    public FindNoteContentRspDTO findNoteContent(String uuid) {
        Response<FindNoteContentRspDTO> response = keyValueFeignApi.findNoteContent(new FindNoteContentReqDTO(uuid));

        if (Objects.isNull(response) || !response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }

        return response.getData();
    }
}
