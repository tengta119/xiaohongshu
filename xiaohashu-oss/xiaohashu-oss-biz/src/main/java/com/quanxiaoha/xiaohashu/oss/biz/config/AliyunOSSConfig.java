package com.quanxiaoha.xiaohashu.oss.biz.config;



import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lbwxxc
 * @date 2025/5/30 11:25
 * @description: 阿里云 Client 配置
 */
@Configuration
public class AliyunOSSConfig {

    @Resource
    private AliyunOSSProperties aliyunOSSProperties;

    @Bean
    public OSS aliyunOSSClient() {
        DefaultCredentialProvider defaultCredentialProvider = CredentialsProviderFactory.newDefaultCredentialProvider(
                aliyunOSSProperties.getAccessKey(), aliyunOSSProperties.getSecretKey());
        return new OSSClientBuilder().build(aliyunOSSProperties.getEndpoint(), defaultCredentialProvider);
    }
}
