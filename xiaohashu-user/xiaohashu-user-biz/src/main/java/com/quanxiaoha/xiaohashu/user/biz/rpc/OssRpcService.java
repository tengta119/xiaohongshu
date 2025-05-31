package com.quanxiaoha.xiaohashu.user.biz.rpc;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.oss.api.FileFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lbwxxc
 * @date 2025/5/31 12:01
 * @description: 对象存储服务调用
 */
@Component
public class OssRpcService {

    @Resource
    private FileFeignApi fileFeignApi;

    public String upload(MultipartFile file) {
        Response<?> response = fileFeignApi.uploadFIle(file);
        if (!response.isSuccess()) {
            return null;
        } else {
            return response.getData().toString();
        }
    }
}
