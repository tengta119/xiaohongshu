package com.quanxiaoha.xiaohashu.kv.biz.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

/**
 * @author lbwxxc
 * @date 2025/5/31 19:52
 * @description:
 */
@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name}")
    private String keySpace;

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;


    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected String getKeyspaceName() {
        return keySpace;
    }

    @Override
    protected int getPort() {
        return port;
    }
}
