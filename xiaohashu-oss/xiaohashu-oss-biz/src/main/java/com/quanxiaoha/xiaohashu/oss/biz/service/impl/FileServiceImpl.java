package com.quanxiaoha.xiaohashu.oss.biz.service.impl;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.oss.biz.service.FileService;
import com.quanxiaoha.xiaohashu.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lbwxxc
 * @date 2025/5/30 10:47
 * @description: 实现类
 */
@Service
public class FileServiceImpl implements FileService {

    @Resource
    FileStrategy fileStrategy;
    private static final String BUCKET_NAME = "xiaohashu";
    @Override
    public Response<?> uploadFile(MultipartFile file) {
        String url = fileStrategy.uploadFile(file, BUCKET_NAME);
        return Response.success(url);
    }
}
