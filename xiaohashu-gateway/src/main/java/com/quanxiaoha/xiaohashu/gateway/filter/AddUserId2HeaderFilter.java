package com.quanxiaoha.xiaohashu.gateway.filter;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.quanxiaoha.xiaohashu.gateway.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author lbwxxc
 * @date 2025/5/29 21:49
 * @description: 将用户 id 透传
 */
@Component
@Slf4j
public class AddUserId2HeaderFilter implements GlobalFilter {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 请求头中，用户 ID 的键
     */
    private static final String HEADER_USER_ID = "userId";

    /**
     * Header 头中 Token 的 Key
     */
    private static final String TOKEN_HEADER_KEY = "Authorization";

    /**
     * Token 前缀
     */
    private static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";

    //ServerWebExchange 包含：
    //ServerHttpRequest：封装 HTTP 请求信息（URL、headers、body 等）。
    //ServerHttpResponse：封装 HTTP 响应信息（状态码、headers、body 等）。
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("==================> TokenConvertFilter");
        Integer userId = null;
        try {
            userId = Math.toIntExact(StpUtil.getLoginIdAsLong());
        } catch (Exception e) {
            return chain.filter(exchange);
        }
        // 从请求头中获取 Token 数据
        List<String> tokenList = exchange.getRequest().getHeaders().get(TOKEN_HEADER_KEY);

        if (CollUtil.isEmpty(tokenList)) {
            // 若请求头中未携带 Token，则直接放行
            return chain.filter(exchange);
        }
        // 获取 Token 值
        String tokenValue = tokenList.get(0);
        // 将 Token 前缀去除
        String token = tokenValue.replace(TOKEN_HEADER_VALUE_PREFIX, "");
        // 构建 Redis Key
        String tokenRedisKey = RedisKeyConstants.SA_TOKEN_TOKEN_KEY_PREFIX + token;
        // 查询 Redis, 获取用户 ID
        userId = (Integer) redisTemplate.opsForValue().get(tokenRedisKey);


        log.info("## 当前登录的用户 ID: {}", userId);
        Integer finalUserId = userId;
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(HEADER_USER_ID, String.valueOf(finalUserId)))
                .build();
        return chain.filter(newExchange);
    }
}
