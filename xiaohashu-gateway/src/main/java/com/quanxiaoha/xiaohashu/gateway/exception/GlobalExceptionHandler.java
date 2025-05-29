package com.quanxiaoha.xiaohashu.gateway.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.gateway.enums.ResponseCodeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
/**
 * @author lbwxxc
 * @date 2025/5/29 21:23
 * @description: 全局异常处理
 */
@Component
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 获取响应对象
        ServerHttpResponse response = exchange.getResponse();

        log.error("==> 全局异常捕获: ", ex);
        Response<?> result;
        // 根据捕获的异常类型，设置不同的响应状态码和响应消息

        if (ex instanceof NotLoginException notLoginException) { // 未登录异常
            // 设置 401 状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 构建响应结果
            result = Response.fail(ResponseCodeEnum.UNAUTHORIZED.getErrorCode(), ex.getMessage());
        } else if (ex instanceof NotPermissionException) { // 无权限异常
            // 权限认证失败时，设置 401 状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 构建响应结果
            result = Response.fail(ResponseCodeEnum.UNAUTHORIZED.getErrorCode(), ResponseCodeEnum.UNAUTHORIZED.getErrorMessage());
        } else { // 其他异常，则统一提示 “系统繁忙” 错误
            result = Response.fail(ResponseCodeEnum.SYSTEM_ERROR);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(result));
            } catch (JsonProcessingException e) {
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
