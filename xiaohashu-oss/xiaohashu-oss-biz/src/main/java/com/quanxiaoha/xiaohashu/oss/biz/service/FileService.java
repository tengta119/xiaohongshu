package com.quanxiaoha.xiaohashu.oss.biz.service;


import com.quanxiaoha.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 1192299468@qq.com
 * @version 1.0
 * @date 2025/5/30 10:46
 * @description: 文件上传
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    Response<?> uploadFile(MultipartFile file);

}
