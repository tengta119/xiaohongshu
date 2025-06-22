package com.quanxiaoha.xiaohashu.comment.biz.config;


import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * @author lbwxxc
 * @date 2025/6/22 10:20
 * @description:
 */
@Configuration
public class RetryConfig {

    @Resource
    private RetryProperties retryProperties;

    @Bean
    public RetryTemplate retryTemplate() {

        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(retryProperties.getMaxAttempts());


        // 定义间隔策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryProperties.getInitInterval()); // 初始间隔 2000ms
        backOffPolicy.setMultiplier(retryProperties.getMultiplier());       // 每次乘以 2

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
