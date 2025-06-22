package com.quanxiaoha.xiaohashu.comment.biz.config;


import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author lbwxxc
 * @date 2025/6/22 09:46
 * @description: RocketMQ 配置
 */
@Configuration
@Import({RocketMQAutoConfiguration.class})
public class RocketMQConfig {
}
