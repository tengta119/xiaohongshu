package com.quanxiaoha.xiaohashu.oss.biz.strategy.impl;


import com.quanxiaoha.xiaohashu.oss.biz.config.MinioProperties;
import com.quanxiaoha.xiaohashu.oss.biz.strategy.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @author lbwxxc
 * @date 2025/5/30 10:42
 * @description: minio 文件上传
 */
@Slf4j
public class MinioFileStrategy implements FileStrategy {

    @Resource
    private MinioProperties minioProperties;
    @Resource
    private MinioClient minioClient;


    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("## 上传文件至 Minio ...");

        // 判断文件是否为空
        if (file == null || file.getSize() == 0) {
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = "jpg";
        if (originalFilename != null) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String contentType = file.getContentType();
        String fileName = UUID.randomUUID().toString().replace("-", "");
        String objectName = String.format("%s%s", fileName, suffix);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType(contentType)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String url = minioProperties.getEndpoint() + "/" + bucketName + "/" + objectName;
        log.info("==> 上传文件至 Minio 成功，访问路径: {}", url);
        return url;
    }
}
