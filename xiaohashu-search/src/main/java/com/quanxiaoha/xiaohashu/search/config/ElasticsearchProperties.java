package com.quanxiaoha.xiaohashu.search.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lbwxxc
 * @date 2025/6/20 17:29
 * @description: Elasticsearch 配置项
 */
@ConfigurationProperties(prefix = "elasticsearch")
@Component
@Data
public class ElasticsearchProperties {

    // es 地址
    private String address;

}

