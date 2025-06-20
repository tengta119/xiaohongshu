package com.quanxiaoha.xiaohashu.search.config;


import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lbwxxc
 * @date 2025/6/20 17:30
 * @description: ElasticsearchRestClient 客户端
 */
@Configuration
public class ElasticsearchRestHighLevelClient {

    @Resource
    private ElasticsearchProperties elasticsearchProperties;

    private static final String COLON = ":";
    private static final String HTTP = "http";

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String address = elasticsearchProperties.getAddress();
        String[] addressArr = address.split(COLON);
        String host = addressArr[0];
        String port = addressArr[1];
        HttpHost httpHost = new HttpHost(host, Integer.parseInt(port), HTTP);
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}
