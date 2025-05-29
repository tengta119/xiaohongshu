package com.quanxiaoha.xiaohashu.gateway.auth;


import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.quanxiaoha.xiaohashu.gateway.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lbwxxc
 * @date 2025/5/29 16:30
 * @description: 从 redis 获取用户对应的角色、权限
 */
@Component
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return getUserRole(loginId);
    }

    @SneakyThrows
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        log.info("## 获取用户权限列表, loginId: {}", loginId);
        //1. 构建 用户-角色
        ArrayList<String> userRoleKeys = getUserRole(loginId);

        //2. 构建 角色-权限
        List<String> rolePermissionsKeys = userRoleKeys.stream().map(RedisKeyConstants::buildRolePermissionsKey).toList();
        List<String> rolePermissionsValues = redisTemplate.opsForValue().multiGet(rolePermissionsKeys);
        if (CollUtil.isEmpty(rolePermissionsValues)) {
            return null;
        }

        //3. 构建 用户-权限
        List<String> permissions = Lists.newArrayList();
        try {
            for (String rolePermissionsValue : rolePermissionsValues) {
                if (rolePermissionsValue == null || rolePermissionsValue.isEmpty()) {
                    continue;
                }
                List<String> rolePermissions = objectMapper.readValue(rolePermissionsValue, new TypeReference<>() {});
                permissions.addAll(rolePermissions);
            }
        } catch (JsonProcessingException e) {
            log.error("==> JSON 解析错误: ", e);
        }
        log.info("==> 用户 {}，对应的权限权限，{} ", loginId, permissions);
        return permissions;
    }

    @SneakyThrows
    private ArrayList<String> getUserRole(Object loginId) {
        String userRoleKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));
        String role = redisTemplate.opsForValue().get(userRoleKey);
        if (StringUtils.isBlank(role)) {
            return null;
        }

        // 将 JSON 字符串转换为 List<String> 集合
        ArrayList<String> list = objectMapper.readValue(role, new TypeReference<>() {});
        log.info("==> 用户：{} ，对应的角色：{} ", loginId, list);
        return list;
    }
}
