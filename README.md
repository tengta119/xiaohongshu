# 五、登录


```sql
CREATE TABLE `t_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `xiaohashu_id` varchar(15) NOT NULL COMMENT '小哈书号(唯一凭证)',
  `password` varchar(64) DEFAULT NULL COMMENT '密码',
  `nickname` varchar(24) NOT NULL COMMENT '昵称',
  `avatar` varchar(120) DEFAULT NULL COMMENT '头像',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `background_img` varchar(120) DEFAULT NULL COMMENT '背景图',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `sex` tinyint DEFAULT '0' COMMENT '性别(0：女 1：男)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0：启用 1：禁用)',
  `introduction` varchar(100) DEFAULT NULL COMMENT '个人简介',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_xiaohashu_id` (`xiaohashu_id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```


**Sa-Token** 是一个轻量级 Java 权限认证框架，官网地址：[https://sa-token.cc/](https://sa-token.cc/) ，主要解决：**登录认证**、**权限认证**、**单点登录**、**OAuth2.0**、**分布式Session会话**、**微服务网关鉴权** 等一系列权限相关问题。

![Pasted image 20250527095951](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgPasted%20image%2020250527095951.png)


* **RBAC**

角色表
```sql
CREATE TABLE `t_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名',
  `role_key` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色唯一标识',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0：启用 1：禁用)',
  `sort` int unsigned NOT NULL DEFAULT 0 COMMENT '管理系统中的显示顺序',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后一次更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
```

权限表

```sql
CREATE TABLE `t_permission` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '父ID',
  `name` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `type` tinyint unsigned NOT NULL COMMENT '类型(1：目录 2：菜单 3：按钮)',
  `menu_url` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '菜单路由',
  `menu_icon` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '菜单图标',
  `sort` int unsigned NOT NULL DEFAULT 0 COMMENT '管理系统中的显示顺序',
  `permission_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态(0：启用；1：禁用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

```

用户角色关联表

```sql
CREATE TABLE `t_user_role_rel` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色表';

```

角色权限关联

```sql
CREATE TABLE `t_role_permission_rel` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
  `permission_id` bigint unsigned NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限表';

```


 网关只从 Redis 中获取权限数据；
    
> **优点：**
>
> - **极高性能**：所有请求都从 Redis 中获取数据，极大提升了响应速度和系统性能。
> - **减轻数据库压力**：完全不访问数据库，数据库的负载压力最小。
>
> **缺点：**
>
> - **数据一致性**：必须确保 Redis 中的权限数据及时更新，否则可能出现数据不一致问题。
> - **单点故障风险**：如果 Redis 出现问题（如宕机），整个系统的权限获取都会受到影响，需要考虑高可用和容灾机制。

![Pasted image 20250527192102](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgPasted%20image%2020250527192102.png)

## 可以说一下登录的逻辑吗

登录主要分为手机验证码登录和账户密码登录，当使用手机验证码登录时，后端使用线程池发送验证码提供并发提供接口访问速度，并缓冲在 redis 中，限制请求频率，之后用户携带账户和验证码向后端发送登录请求，后端根据手机号检查用户是否注册，如果没有注册则自动注册（ `@Transactional` 原子操作，编程式事务 ），并将该用户的默认角色缓冲在 redis 中，最后将 token 返回给前端，其中 token 也会缓冲在 redis 中

退出登录时，会将 token 从缓冲中删除，代表用户退出登录
## redis 缓冲过期了怎么办


## 集群部署，Runner 多次同步的问题

* **Redis 分布式锁**

分布式锁是确保在分布式系统中多个节点能够协调一致地访问共享资源的一种机制。Redis 分布式锁通过 Redis 的原子操作，确保在高并发情况下，对共享资源的访问是互斥的。

> **实现思路**：
>
> - 可以使用 Redis 的 `SETNX` 命令来实现。如果键不存在，则设置键值并返回 1（表示加锁成功）；如果键已存在，则返回 0（表示加锁失败）。
> - 多个子服务同时操作 Redis , 第一个加锁成功，则可以同步权限数据；后续的子服务都会加锁失败，若加锁失败，则不同步权限数据；
> - 另外，结合 `EXPIRE` 命令为锁设置一个过期时间，比如 1 天，防止死锁。则在 1 天内，无论启动多少次认证服务，均只会同步一次数据。

## Nacos 注册中心搭建

### Nacos 主要特性

- **服务发现和服务健康监测**
  
    > Nacos 支持基于 DNS 和基于 RPC 的服务发现。服务提供者使用 [原生SDK](https://nacos.io/docs/latest/guide/user/sdk/) 、[OpenAPI](https://nacos.io/docs/latest/guide/user/open-api/) 、或一个[独立的Agent TODO](https://nacos.io/docs/latest/guide/user/other-language/) 注册 Service 后，服务消费者可以使用[DNS TODO](https://nacos.io/docs/latest/ecology/use-nacos-with-coredns/) 或[HTTP&API](https://nacos.io/docs/latest/guide/user/open-api/) 查找和发现服务。
    >
    > Nacos 提供对服务的实时的健康检查，阻止向不健康的主机或服务实例发送请求。Nacos 支持传输层 (PING 或 TCP)和应用层 (如 HTTP、MySQL、用户自定义）的健康检查。 对于复杂的云环境和网络拓扑环境中（如 VPC、边缘网络等）服务的健康检查，Nacos 提供了 agent 上报模式和服务端主动检测2种健康检查模式。Nacos 还提供了统一的健康检查仪表盘，帮助您根据健康状态管理服务的可用性及流量。
    
- **动态配置服务**
  
    > 动态配置服务可以让您以中心化、外部化和动态化的方式管理所有环境的应用配置和服务配置。
    >
    > 动态配置消除了配置变更时重新部署应用和服务的需要，让配置管理变得更加高效和敏捷。
    >
    > 配置中心化管理让实现无状态服务变得更简单，让服务按需弹性扩展变得更容易。
    >
    > Nacos 提供了一个简洁易用的UI ([控制台样例 Demo](http://console.nacos.io/nacos/index.html) ) 帮助您管理所有的服务和应用的配置。Nacos 还提供包括配置版本跟踪、金丝雀发布、一键回滚配置以及客户端配置更新状态跟踪在内的一系列开箱即用的配置管理特性，帮助您更安全地在生产环境中管理配置变更和降低配置变更带来的风险。
    
- **动态 DNS 服务**
  
    > 动态 DNS 服务支持权重路由，让您更容易地实现中间层负载均衡、更灵活的路由策略、流量控制以及数据中心内网的简单DNS解析服务。动态DNS服务还能让您更容易地实现以 DNS 协议为基础的服务发现，以帮助您消除耦合到厂商私有服务发现 API 上的风险。
    >
    > Nacos 提供了一些简单的 [DNS APIs TODO](https://nacos.io/docs/latest/ecology/use-nacos-with-coredns/) 帮助您管理服务的关联域名和可用的 IP 列表.
    
- **服务及其元数据管理**
  
    > Nacos 能让您从微服务平台建设的视角管理数据中心的所有服务及元数据，包括管理服务的描述、生命周期、服务的静态依赖分析、服务的健康状态、服务的流量管理、路由及安全策略、服务的 SLA 以及最首要的 metrics 统计数据。
    

![Pasted image 20250527200530](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgPasted%20image%2020250527200530.png)

# 七、Gateway 网关搭建与接口鉴权


Gateway 网关通常具有以下功能：

- **路由转发**：将路由请求转发到适当的微服务实例。
  
- **负载均衡**：在多个服务实例之间分配请求，以实现负载均衡。
  
- **认证和授权**：对请求进行身份验证和授权，以确保只有授权的客户端才能访问服务。
  
    > PS : 咱们这个项目中，用户认证的工作，是由具体的认证服务来处理的。
    
- **日志和监控**：记录请求和响应的日志，并监控流量和性能指标。
  
- **限流和熔断**：控制流量，以防止服务过载，并提供熔断机制来应对服务故障。


Spring Cloud Gateway 会使用注册中心（如 Nacos ）来解析并负载均衡到具体的服务实例。

## 鉴权

当用户携带 token 发送请求时会经过 Gateway 路由转发，此时后端先根据用户 id 从 redis 获取该用户的权限列表，然后根据此权限列表进行鉴权



# 九、用户服务

OpenFeign 是一个声明式的 HTTP 客户端，它使得我们可以非常方便地调用 HTTP 接口。OpenFeign 是 Netflix 开源的 Feign 项目的扩展，旨在与 Spring Cloud 紧密集成。它通过注解来定义接口，类似于 Spring MVC 的控制器，使得开发者可以专注于业务逻辑，而不需要关注 HTTP 请求的具体实现。

![image-20250531120822979](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250531120822979.png)

![image-20250531120909081](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250531120909081.png)

要进行远程调用，要开启 `@EnableFeignClients(basePackages = "com.quanxiaoha.xiaohashu")` 注解





