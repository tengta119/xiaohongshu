package com.quanxiaoha.xiaohashu.oss.biz.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lbwxxc
 * @date 2025/5/30 11:25
 * @description: 阿里云 OSS 配置项
 */
@ConfigurationProperties(prefix = "storage.aliyun-oss")
@Component
@Data
public class AliyunOSSProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
}
