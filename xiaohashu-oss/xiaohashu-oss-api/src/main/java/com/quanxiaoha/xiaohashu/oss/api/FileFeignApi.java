package com.quanxiaoha.xiaohashu.oss.api;


import com.quanxiaoha.framework.common.response.Response;

import com.quanxiaoha.xiaohashu.oss.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/5/31 11:41
 * @description:
 */
// 是用来标记这个接口是一个 Feign 客户端的注解,
// name = ApiConstants.SERVICE_NAME 指定了这个 Feign 客户端所调用的服务名称。这个名称通常是在注册中心（如 Eureka 或 Nacos）中注册的服务名称
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface FileFeignApi {

    String PREFIX = "/file";

    @PostMapping(value = PREFIX + "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<?> uploadFIle(@RequestPart("file") MultipartFile file);

}
