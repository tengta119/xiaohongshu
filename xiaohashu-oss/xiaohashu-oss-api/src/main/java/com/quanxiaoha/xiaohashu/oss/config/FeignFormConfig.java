package com.quanxiaoha.xiaohashu.oss.config;


import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lbwxxc
 * @date 2025/5/31 11:57
 * @description: 表单
 */
@Configuration
public class FeignFormConfig {

    @Bean
    public Encoder encoder(){
        return new SpringFormEncoder();
    }
}
