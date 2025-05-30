package com.quanxiaoha.xiaohashu.oss.biz.controller;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.oss.biz.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lbwxxc
 * @date 2025/5/30 10:49
 * @description: 文件
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> uploadFile(@RequestPart("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }
}
