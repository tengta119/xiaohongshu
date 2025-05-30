package com.quanxiaoha.xiaohashu.oss.biz.strategy;


import org.springframework.web.multipart.MultipartFile;

/**
 * @author lbwxxc
 * @date 2025/5/30 10:41
 * @description: 文件策略接口
 */
public interface FileStrategy {


    /**
     * 文件上传
     *
     * @param file
     * @param bucketName
     * @return
     */
    String uploadFile(MultipartFile file, String bucketName);
}
