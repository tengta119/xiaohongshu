package com.quanxiaoha.xiaohashu.oss.biz.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lbwxxc
 * @date 2025/5/30 10:57
 * @description:
 */
@ConfigurationProperties(value = "storage.minio")
@Component
@Data
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
}
