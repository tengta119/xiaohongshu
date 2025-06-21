package com.quanxiaoha.xiaohashu.search.canal;


import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author lbwxxc
 * @date 2025/6/21 10:36
 * @description:
 */
@Component
@Slf4j
public class CanalClient implements DisposableBean {

    @Resource
    private CanalProperties canalProperties;

    private CanalConnector canalConnector;

    @Bean
    public CanalConnector getCanalConnector() {
        String address = canalProperties.getAddress();
        String[] addressArr = address.split(":");
        String host = addressArr[0];
        int port = Integer.parseInt(addressArr[1]);

        // 创建一个 CanalConnector 实例，连接到指定的 Canal 服务端
        canalConnector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(host, port),
                canalProperties.getDestination(),
                canalProperties.getUsername(),
                canalProperties.getPassword()
        );

        canalConnector.connect();
        canalConnector.subscribe(canalProperties.getSubscribe());
        canalConnector.rollback();
        return canalConnector;
    }


    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(canalConnector)) {
            canalConnector.disconnect();
        }
    }
}
