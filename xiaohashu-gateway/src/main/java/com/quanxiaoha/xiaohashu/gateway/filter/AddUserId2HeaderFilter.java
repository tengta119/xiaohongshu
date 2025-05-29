package com.quanxiaoha.xiaohashu.gateway.filter;


import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author lbwxxc
 * @date 2025/5/29 21:49
 * @description: 将用户 id 透传
 */
@Component
@Slf4j
public class AddUserId2HeaderFilter implements GlobalFilter {

    /**
     * 请求头中，用户 ID 的键
     */
    private static final String HEADER_USER_ID = "userId";

    //ServerWebExchange 包含：
    //ServerHttpRequest：封装 HTTP 请求信息（URL、headers、body 等）。
    //ServerHttpResponse：封装 HTTP 响应信息（状态码、headers、body 等）。
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("==================> TokenConvertFilter");
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return chain.filter(exchange);
        }

        Long finalUserId = userId;
        log.info("## 当前登录的用户 ID: {}", userId);
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(HEADER_USER_ID, String.valueOf(finalUserId)))
                .build();
        return chain.filter(newExchange);
    }
}
