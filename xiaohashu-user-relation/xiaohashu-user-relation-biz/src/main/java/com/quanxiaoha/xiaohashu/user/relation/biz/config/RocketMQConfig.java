package com.quanxiaoha.xiaohashu.user.relation.biz.config;


import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author lbwxxc
 * @date 2025/6/2 17:20
 * @description:
 */
@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
}
