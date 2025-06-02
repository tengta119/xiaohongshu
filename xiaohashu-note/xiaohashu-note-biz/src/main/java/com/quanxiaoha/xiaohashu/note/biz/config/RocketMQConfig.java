package com.quanxiaoha.xiaohashu.note.biz.config;


import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author lbwxxc
 * @date 2025/6/2 09:14
 * @description: RocketMQ 配置
 */
@Configuration
// 这个注解导入了 RocketMQAutoConfiguration 类。这通常意味着它会触发 RocketMQ 的自动配置机制。
// Spring Boot 的自动配置会根据项目的依赖和属性文件，自动创建和配置 RocketMQ 相关的 bean，比如 DefaultMQProducer 和 DefaultMQPushConsumer
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {


}
