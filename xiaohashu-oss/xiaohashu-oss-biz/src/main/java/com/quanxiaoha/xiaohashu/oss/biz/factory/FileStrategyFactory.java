package com.quanxiaoha.xiaohashu.oss.biz.factory;


import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.xiaohashu.oss.biz.strategy.FileStrategy;
import com.quanxiaoha.xiaohashu.oss.biz.strategy.impl.AliyunOSSFileStrategy;
import com.quanxiaoha.xiaohashu.oss.biz.strategy.impl.MinioFileStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lbwxxc
 * @date 2025/5/30 10:44
 * @description: 存储对象创建工厂
 */
@Configuration
public class FileStrategyFactory {

    @Value("${storage.type}")
    private String storageType;

    @Bean
    public FileStrategy getFileStrategy(){
        if (StringUtils.equals(storageType, "aliyun")){
            return new AliyunOSSFileStrategy();
        } else if(StringUtils.equals(storageType, "minio")){
            return new MinioFileStrategy();
        }

        throw new IllegalArgumentException("不可用的存储类型");
    }
}
