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



**什么是 DTO 实体类？和 VO 实体类有什么区别？**

- `DTO` 是一个用于数据传输的对象，通常服务之间传递数据时，会定义一个 `DTO` 实体类。
- `VO` 是一个用于表现层的对象，通常用来封装页面数据或前端显示的数据。它用于控制层和视图层之间的数据传递。

好的，在 Spring Boot 项目中，`@RequestPart`、`@RequestBody` 和 `@RequestParam` 都是用来从 HTTP 请求中提取数据的注解，但它们适用的场景和处理的数据类型有所不同。

## @RequestParam 、@RequestBody、@RequestPart

**1. `@RequestParam`**

- **作用**：用于从 HTTP 请求的 **查询参数 (query parameters)** 或 **表单参数 (form parameters)** 中提取值。

- **请求类型**：通常用于 `GET` 请求的查询参数（URL 中 `?` 之后的部分，如 `?name=John&age=30`），或者 `POST` 请求中 `application/x-www-form-urlencoded` 或 `multipart/form-data` 类型的表单字段值。

- **数据类型**：通常是简单的类型，如 `String`, `int`, `boolean` 等，也可以是这些类型的数组或列表。

- **使用方式**：标记在控制器方法的参数上。

- **常见属性**：

    - `value` 或 `name`：指定要绑定的请求参数的名称。如果方法参数名与请求参数名一致，则可以省略。
    - `required`：布尔值，表示该参数是否必需，默认为 `true`。如果为 `true` 但请求中未提供该参数，会抛出异常。
    - `defaultValue`：如果请求中未提供该参数，则使用此默认值。设置了 `defaultValue` 后，`required` 属性会自动被认为是 `false`。

- **示例**：

    ```java
    @GetMapping("/api/users")
    public ResponseEntity<String> getUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy) {
        // 处理分页和排序逻辑
        // URL 示例: /api/users?page=1&size=20&sortBy=name
        // sortBy 是可选的
        return ResponseEntity.ok("Fetching users with page=" + page + ", size=" + size + ", sortBy=" + sortBy);
    }
    
    @PostMapping("/api/form")
    public ResponseEntity<String> submitForm(@RequestParam String username, @RequestParam String password) {
        // 处理 application/x-www-form-urlencoded 表单提交
        // username 和 password 会从表单字段中获取
        return ResponseEntity.ok("Form submitted by: " + username);
    }
    ```

**2. `@RequestBody`**

- **作用**：用于将 HTTP 请求的 **请求体 (Request Body)** 的内容（通常是 JSON、XML 或其他自定义媒体类型）**反序列化**并绑定到一个 Java 对象上。

- **请求类型**：主要用于 `POST`, `PUT`, `PATCH` 等需要携带复杂数据的请求。

- **数据类型**：通常是复杂的 Java 对象 (POJO)。Spring MVC 会使用配置的 `HttpMessageConverter` 来进行反序列化。

- **使用方式**：标记在控制器方法的参数上。一个控制器方法通常**最多只能有一个** `@RequestBody` 注解的参数。

- **核心功能**：将整个请求体的内容映射到一个对象。

- **示例**： 假设有一个 `User` 类：

    ```java
    public class User {
        private String name;
        private int age;
        // getters and setters
    }
    ```

    控制器方法：

    ```java
    @PostMapping("/api/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Spring 会尝试将请求体中的 JSON (如 {"name":"Alice", "age":25})
        // 自动转换为 User 对象
        userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    ```

- **. `@RequestPart`**

    - **作用**：专门用于处理 `multipart/form-data` 类型的请求。这种请求通常用于**文件上传**，但也可以包含其他表单字段。`@RequestPart` 可以将请求中的特定 "部分 (part)" 绑定到方法参数。
    - **请求类型**：必须是 `multipart/form-data`。
    - 数据类型：
        - 对于文件部分，通常是 `org.springframework.web.multipart.MultipartFile` 或 Java EE 的 `javax.servlet.http.Part`。
        - 对于非文件部分（表单字段），可以是简单类型 (如 `String`)，也可以是复杂的 Java 对象 (POJO)。如果是非文件部分且内容是 JSON/XML，Spring 也可以尝试进行反序列化。
    - **使用方式**：标记在控制器方法的参数上。
    - 与 `@RequestParam` 在 `multipart/form-data` 中的区别：
        - `@RequestParam` 也可以用来获取 `multipart/form-data` 中的简单表单字段值 (非文件部分) 或者文件本身 (`MultipartFile` 类型)。
        - `@RequestPart` 提供了更强的灵活性，特别是当一个 "part" 本身的内容是复杂类型（如 JSON）需要反序列化时。`@RequestParam` 主要用于获取字符串形式的字段值或文件，而 `@RequestPart` 结合 `HttpMessageConverter` 可以将一个 part 的内容直接转换成 POJO。
    - **示例**：

    ```java
    @PostMapping(value = "/api/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFileAndMetadata(
            @RequestPart("file") MultipartFile file, // "file" 是 multipart 请求中的一个 part 名称
            @RequestPart("metadata") String metadataJson, // "metadata" 是另一个 part，其内容是 JSON 字符串
            @RequestPart(name = "user", required = false) User userObject // "user" 是另一个 part，其内容是 JSON，并自动转为 User 对象
            ) {
    
        // 处理上传的文件
        String fileName = file.getOriginalFilename();
    
        // 处理 metadataJson (可能需要手动解析 JSON 字符串)
        // ObjectMapper objectMapper = new ObjectMapper();
        // MyMetadata metadata = objectMapper.readValue(metadataJson, MyMetadata.class);
    
        // userObject 已经被 Spring 自动转换 (如果 'Content-Type' of the part is 'application/json')
        if (userObject != null) {
            System.out.println("User from part: " + userObject.getName());
        }
    
        return ResponseEntity.ok("File '" + fileName + "' uploaded successfully with metadata.");
    }
    ```

    在上面的 `@RequestPart("user") User userObject` 中，如果客户端在名为 "user" 的 part 中发送了 JSON 数据，并且该 part 的 `Content-Type` 头部指定为 `application/json`，Spring 会自动使用消息转换器将其反序列化为 `User` 对象。这是 `@RequestPart` 相对于 `@RequestParam` 在处理复杂类型 part 时的优势。

**总结与对比：**

| **特性**     | **@RequestParam**                                            | **@RequestBody**                                 | **@RequestPart**                                             |
| ------------ | ------------------------------------------------------------ | ------------------------------------------------ | ------------------------------------------------------------ |
| **主要用途** | 获取查询参数或表单字段 (简单类型)                            | 获取整个请求体并反序列化为对象 (通常是 JSON/XML) | 处理 `multipart/form-data` 请求的各个部分，特别是文件上传和需要反序列化的非文件部分 |
| **数据来源** | URL 查询字符串、`application/x-www-form-urlencoded` 表单数据、`multipart/form-data` 表单字段 | HTTP 请求体                                      | `multipart/form-data` 请求中的一个或多个 "part"              |
| **数据类型** | 通常是简单类型 (String, int 等)，或 `MultipartFile` (用于文件) | 复杂的 Java 对象 (POJO)                          | `MultipartFile` (用于文件 part)，简单类型 (String 等) 或复杂 Java 对象 (POJO，如果 part 内容是 JSON/XML 并有相应 Content-Type) |
| **请求体**   | 不直接处理整个请求体，而是其中的特定参数                     | 消耗整个请求体                                   | 处理请求体的各个命名部分                                     |
| **典型场景** | 分页参数、搜索条件、简单表单提交                             | 创建或更新资源时，接收 JSON/XML 数据             | 文件上传、同时上传文件和元数据 (元数据可以是 JSON 对象)      |
| **数量限制** | 方法中可以有多个                                             | 方法中通常只有一个                               | 方法中可以有多个，对应 `multipart` 请求中的不同部分          |

选择哪个注解取决于你要从请求的哪个部分获取数据以及数据的格式。

## 表单 (form)” 和 “请求体 (request body)

在 HTTP 通信中，“表单 (form)” 和 “请求体 (request body)” 是相关但不完全相同的概念。理解它们的区别对于 Web 开发非常重要。

简单来说：

- **请求体 (Request Body)**：是 HTTP 请求中用于**承载实际数据**的部分。它可以包含各种格式的数据，例如 JSON、XML、纯文本、图片数据，当然也包括表单数据。请求体主要用于 `POST`、`PUT`、`PATCH` 等方法，这些方法通常需要向服务器发送数据。`GET` 请求通常没有请求体，或者请求体会被忽略，其参数通过 URL 的查询字符串传递。
- **表单数据 (Form Data)**：特指通过 HTML `<form>` 元素提交的数据，或者以类似表单形式构造的数据。这些数据通常是键值对。

以下是更详细的解释和区别：

**1. 请求体 (Request Body)**

- **通用性**：请求体是 HTTP 消息（请求或响应）的一部分，用于传输除请求头/响应头之外的数据。

- 数据格式

    ：请求体中的数据格式由 `Content-Type`请求头指定

    常见的 `Content-Type` 包括：

    - `application/json`：表示请求体是 JSON 格式的数据。
    - `application/xml`：表示请求体是 XML 格式的数据。
    - `text/plain`：表示请求体是纯文本。
    - `image/jpeg`：表示请求体是 JPEG 图片数据。
    - `application/x-www-form-urlencoded`：这是一种常见的表单数据提交方式，数据被编码为键值对字符串，键和值都经过 URL 编码。
    - `multipart/form-data`：这也是一种表单数据提交方式，常用于上传文件，可以将表单数据分割成多个部分 (part) 发送，每个部分可以有自己的 `Content-Type`。

- 存在性：

    - `POST`, `PUT`, `PATCH` 等请求通常包含请求体，用于发送数据给服务器进行处理（如创建、更新资源）。
    - `GET`, `HEAD`, `DELETE`, `OPTIONS` 等请求通常**不包含**请求体，或者服务器会忽略它们。`GET` 请求的数据通过 URL 中的查询参数 (query parameters) 传递。

**2. 表单数据 (Form Data)**

- **来源**：主要来源于 HTML 的 `<form>` 标签。当用户填写表单并提交时，浏览器会根据 `<form>` 标签的 `method` (通常是 `GET` 或 `POST`) 和 `enctype` 属性来构造 HTTP 请求。
- 编码方式 (`enctype`)：
    - `application/x-www-form-urlencoded` (默认)：
        - 将表单数据转换成键值对，例如 `name1=value1&name2=value2`。
        - 特殊字符会被 URL 编码 (百分号编码)。
        - 如果表单的 `method` 是 `POST`，这些编码后的数据会放在**请求体**中。
        - 如果表单的 `method` 是 `GET`，这些编码后的数据会附加到 URL 的末尾，作为查询字符串 (query string)，**不使用请求体**。
    - `multipart/form-data`：
        - 当表单包含文件上传 (`<input type="file">`) 时必须使用此类型。
        - 它将表单数据分割成多个部分，每个部分都有自己的描述信息 (包括 `Content-Disposition` 和可能的 `Content-Type`)。
        - 这些数据总是通过 `POST` 方法放在**请求体**中。
    - `text/plain`：
        - 将表单数据以纯文本形式发送，不做任何编码（除了行尾符转换）。
        - 不常用，通常也通过 `POST` 方法放在**请求体**中。
- 与请求体的关系：
    - 当使用 `POST` 方法提交表单时，经过编码的表单数据会成为**请求体的内容**。
    - 当使用 `GET` 方法提交表单时，表单数据会附加到 URL 上，此时**请求体为空**或被忽略。

**主要区别总结：**

| 特性          | 请求体 (Request Body)                                      | 表单数据 (Form Data)                                         |
| ------------- | ---------------------------------------------------------- | ------------------------------------------------------------ |
| **定义**      | HTTP 请求中承载实际数据的主体部分。                        | 通常指源自 HTML `<form>` 或以类似键值对形式构造的数据。      |
| **内容**      | 可以是任何类型的数据 (JSON, XML, 文件, 文本, 表单数据等)。 | 主要是键值对，根据 `enctype` 编码。                          |
| **格式指定**  | 由 `Content-Type` 请求头指定。                             | 对于 POST 请求，其编码格式 (`application/x-www-form-urlencoded` 或 `multipart/form-data`) 会成为请求体的 `Content-Type`。 |
| **HTTP 方法** | 主要用于 `POST`, `PUT`, `PATCH` 等。                       | `GET` 方法提交时在 URL 中，`POST` 方法提交时在请求体中。     |
| **普遍性**    | 更通用的概念，所有需要发送主体数据的请求都会用到。         | 特指一种常见的数据组织和提交方式。                           |



**可以这样理解：**

- 请求体就像一个包裹，可以装各种东西。
- 表单数据是这个包裹里可能装的一种特定类型的“货物”（当使用 POST 提交时）。
- 当表单使用 GET 方法提交时，表单数据就像是写在包裹外面的“地址标签和备注”，而不是放在包裹里面。

因此，**表单数据可以通过请求体发送（通常是 POST 请求），但请求体并不总是只包含表单数据，它也可以包含 JSON、XML 等其他格式的数据。** “表单数据” (Form Data) 和“请求体” (Request Body) 在 HTTP 通信中是相关但不完全相同的概念。理解它们的区别对于 Web 开发至关重要。

简单来说：

- **请求体 (Request Body)**：是 HTTP 请求中用于**承载数据**的部分。并非所有类型的 HTTP 请求都有请求体（例如，GET、HEAD、DELETE 请求通常没有请求体），但像 POST、PUT、PATCH 这类需要向服务器发送数据的请求，其数据内容就放在请求体中。请求体可以携带各种格式的数据，通过 `Content-Type` 头部来指明其格式。
- **表单数据 (Form Data)**：是一种**特定类型的请求体数据格式**，通常源自 HTML `<form>` 元素的提交。它本身也是放在请求体中进行传输的。

下面更详细地解释它们之间的关系和区别：

**请求体 (Request Body)**

1. **通用性**：
    - 请求体是 HTTP 协议中一个通用的概念，用于在客户端和服务器之间传输数据。
    - 它可以包含任何类型的数据，例如：
        - **JSON (`application/json`)**：现代 API 中最常见的数据格式，用于传输结构化的对象数据。
        - **XML (`application/xml` 或 `text/xml`)**：另一种结构化数据格式，常见于早期的 Web 服务。
        - **纯文本 (`text/plain`)**：简单的文本数据。
        - **二进制数据 (`application/octet-stream`)**：如图片、音频、视频文件等。
        - **表单数据 (`application/x-www-form-urlencoded` 或 `multipart/form-data`)**：这是表单数据作为请求体内容的一种情况。
2. **`Content-Type` 头部**：
    - HTTP 请求头中的 `Content-Type` 字段至关重要，它告诉服务器请求体中数据的实际格式是什么，以便服务器能够正确解析这些数据。
    - 例如，如果 `Content-Type` 是 `application/json`，服务器会期望请求体是一个 JSON 字符串。
3. **使用场景**：
    - 创建新资源 (POST)
    - 更新现有资源 (PUT, PATCH)
    - 发送复杂查询 (虽然不常见，但 POST 有时也用于发送 GET 请求难以承载的复杂参数)

**表单数据 (Form Data)**

表单数据特指通过 HTML 表单提交时，数据在请求体中的组织和编码方式。主要有两种编码类型，由 `<form>` 标签的 `enctype` 属性指定，并且也通过 `Content-Type` 请求头告知服务器：

1. **`application/x-www-form-urlencoded`**：
    - 这是 HTML 表单提交的**默认**编码类型 (如果未指定 `enctype`)。
    - 数据被编码为**键值对 (key-value pairs)**，类似于 URL 的查询字符串 (query string)。
    - 格式：`key1=value1&key2=value2...`
    - 键和值都会进行 URL 编码 (例如，空格会被替换为 `+` 或 `%20`，特殊字符会被转义)。
    - **不适合传输文件**，主要用于传输简单的文本数据。
2. **`multipart/form-data`**：
    - 当表单中包含文件上传 (`<input type="file">`) 时，**必须使用**这种编码类型。
    - 请求体被分割成多个 "部分 (parts)"，每个部分可以有自己的 `Content-Disposition` 头部 (通常包含字段名 `name`，对于文件还会有 `filename`)，甚至可以有自己的 `Content-Type` (例如，文本字段可以是 `text/plain`，上传的图片可以是 `image/jpeg`)。
    - 每个部分由一个在 `Content-Type` 头部中定义的**边界字符串 (boundary string)** 分隔开。
    - 这种格式既可以传输文件，也可以传输普通的文本字段值。

**核心区别总结**

| 特性               | 请求体 (Request Body)                                        | 表单数据 (Form Data)                                         |
| ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **概念层级**       | 更通用的概念，是 HTTP 请求的一部分，用于携带数据。           | 是请求体内容的一种特定格式，通常源自 HTML 表单提交。         |
| **内容格式**       | 多样，可以是 JSON, XML, 纯文本, 二进制数据, 表单数据等。     | 主要有 `application/x-www-form-urlencoded` 和 `multipart/form-data` 两种标准格式。 |
| **`Content-Type`** | 由实际发送的数据类型决定。                                   | 通常是 `application/x-www-form-urlencoded` 或 `multipart/form-data`。 |
| **与表单的关系**   | 不一定与 HTML 表单相关。API 间的直接调用也常用请求体（如 JSON）。 | 强烈依赖于 HTML 表单的提交机制，或者在程序中模拟表单提交。   |
| **文件上传**       | 可以通过 `multipart/form-data` 格式的请求体实现。            | `multipart/form-data` 是为文件上传设计的标准表单数据格式。   |

**可以这样理解：**

- **请求体**就像一个“包裹”，可以装各种各样的“货物”。
- **表单数据**是这个“包裹”里的一种特定类型的“货物”，并且这种“货物”有两种常见的“包装方式” (`x-www-form-urlencoded` 和 `multipart/form-data`)。

当你提交一个 HTML 表单 (通常是 POST 方法) 时，表单中的数据会被编码成上述两种表单数据格式之一，然后放入 HTTP 请求的**请求体**中发送给服务器。

而当你使用 JavaScript (例如 `fetch` 或 `axios`) 发送 POST 请求并传递一个 JSON 对象时，这个 JSON 对象也会被序列化后放入**请求体**中，此时的 `Content-Type` 通常是 `application/json`，这就不属于“表单数据”的范畴了，尽管它也是请求体的一部分。



# 十、K-V 短文本存储服务搭建与开发



![image-20250531193741079](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250531193741079.png)

在学习 CQL 命令之前，先理解一下 Cassandra 中几个基本概念：

- **节点（Node）**：Cassandra 集群中的每个服务器称为一个节点。每个节点都存储数据，并且相互之间没有主从关系，所有节点都是对等的。
- **集群（Cluster）**：由多个节点组成的分布式系统称为集群。集群中的节点共同工作，处理读写请求并存储数据。
- **数据中心（Data Center）**：集群中的节点可以分布在多个数据中心，每个数据中心包含若干个节点。数据中心的划分有助于实现跨地域的高可用性。
- **键空间（Keyspace）**：键空间是一个逻辑容器，用于管理多个表，可以理解为 MySQL 中的库。另外，键空间定义了数据复制的策略。
- **表（Table）**：表是数据存储的基本单位，由行和列组成。每张表都有一个唯一的名称和定义。
- **主键（Primary Key）**：每行数据都有一个唯一的主键。主键由分区键和可选的列组成，用于唯一标识数据行。
- **分区键（Partition Key）**：Cassandra 使用分区键的哈希值将数据分布到不同的节点上，从而实现负载均衡和数据的水平扩展。分区键可以是单个列或多列的组合（复合分区键）。

![image-20250531194220890](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250531194220890.png)





# 十二、分布式 ID 生成服务搭建与开发

## 单库单表

在了解什么是分库分表之前，先来说说单库单表的弊端：

- **性能瓶颈**：所有读写操作都需要通过单个数据库实例处理，这可能导致 I/O 和 CPU 成为瓶颈，限制了系统的处理能力。

    > 一般来说，单表的数据量达到数百万到数千万行时，查询性能就可能会受到影响了，具体数据受限于硬件配置、索引设计、查询复杂度、并发访问量、数据分布以及数据库配置等。

- **可用性风险**：如果依赖于单一数据库实例，一旦该实例发生故障，可能会导致整个系统不可用，增加了数据丢失的风险。

- **备份和恢复复杂**：大数据库的备份和恢复过程可能非常耗时且资源密集，影响正常运营。

- **高并发场景下的锁竞争**：在高并发场景下，同一数据库上的多个事务可能因为锁定机制而产生竞争，降低效率。

## 什么是分库分表？

了解了单库单表的弊端后，再理解为什么需要分库分表，就轻松多了：

> **分布分表（Distributed Sharding）是数据库设计中的一种策略，主要用于处理大规模数据和高并发访问场景。** 在传统的单体数据库中，所有的数据都存储在一个数据库实例上。然而，随着数据量的增长和业务需求的提升，单一数据库实例可能无法满足性能、扩展性和可用性的要求，这就导致了对分布分表的需求。

就拿项目中的 `t_user` 用户表来举例，分库分表策略下，如下图所示，我们会创建多个数据库（图中只画了3个，实际业务中会更多），每个数据库中，又将用户表拆分为多张表：

![img](https://img.quanxiaoha.com/quanxiaoha/172267372478310)

海量的用户数据（小红书2亿用户）打散，均匀的存储到每张表中，以提升查询性能。

## 分库分表的优势

使用分库分表策略后，优势如下：

- **提高性能**：通过将数据分散到多个数据库节点上，可以实现负载均衡，减少单个数据库的压力，从而提高查询和写入操作的速度。
- **水平扩展**：当一个数据库的资源达到极限时，可以通过增加更多的数据库节点来线性地扩展系统的处理能力，而无需升级单个节点的硬件。
- **提高可用性**：分布分表可以实现数据的冗余存储，即使某个节点出现故障，其他节点仍然可以提供服务，提高了系统的整体可用性。
- **简化数据管理**：对于某些应用，将数据按逻辑或地理区域进行分割，可以更有效地管理和访问数据，例如基于用户位置的数据访问优化。

## 为什么需要分布式 ID?

在单库单表中，主键 ID 通常由数据库自增特性 `AUTO_INCREMENT` 来生成，如下图：

![img](https://img.quanxiaoha.com/quanxiaoha/172266947535035)

而在分库分表的场景下，拿用户表来说，就会导致一个问题，如果用户 ID 使用自增主键 ID, 就会存在大量重复的用户 ID , 如下图所示：

![img](https://img.quanxiaoha.com/quanxiaoha/172267581091885)



## 什么是分布式ID？

**分布式 ID 是指在一个分布式系统中，为每一个数据项或事件生成一个全局唯一标识符的过程。** 这个标识符通常是一个长整型数字或字符串，能够跨多个服务实例和数据库集群唯一识别每一个实体，是实现数据关联和跟踪的基础。

在传统的单体应用中，ID 生成相对简单，可以通过数据库的自增字段来实现。但在微服务架构下，每个服务可能运行在不同的服务器上，甚至可能有多个实例，这就意味着每个服务都需要独立生成 ID，并且保证全局唯一性。此外，分布式 ID 还需要解决以下几个关键问题：

- **一致性**：所有生成的 ID 必须在分布式环境中保持一致，避免重复和冲突。
- **高性能**：在高并发场景下，ID 生成机制不能成为系统的瓶颈。
- **可扩展性**：随着业务的增长，ID 生成策略应该易于扩展，适应更大的负载。
- **容错性**：即使部分服务出现故障，ID 生成也不能中断。



## 分布式 ID 生成方案



- 目前，业界已经发展出了多种分布式 ID 生成算法和技术，以下是常见的几种方案：

    - **UUID** ： UUID (Universally Unique Identifier) 是一种常用的分布式 ID 生成方式, 它的标准型式包含 32 个 16 进制数字，以连字号分为五段，形式为 8-4-4-4-12 的 36 个字符，示例：`550e8400-e29b-41d4-a716-446655440000`。

        > - 优点:
        >
        >     - 生成性能非常高：直接本地生成，不依赖其他中间件，无网络 / 磁盘 IO 消耗；
        >
        > - 缺点：
        >
        >     - 不易于存储：UUID 太长，16 字节 128 位，通常以 36 长度的字符串表示。在海量数据场景下，会消耗较大的存储空间。
        >
        >     - 信息不安全：基于 MAC 地址生成 UUID 的算法可能会造成 MAC 地址泄露，这个漏洞曾被用于寻找梅丽莎病毒的制作者位置。
        >
        >     - 充当主键时，在特定场景下，会存在问题。如作为 MySQL 数据库的主键时，UUID 就非常不合适。
        >
        >         - [MySQL 官方](https://dev.mysql.com/doc/refman/5.7/en/innodb-index-types.html) 就明确建议主键尽量越短越好，36 个字符长度的 UUID 不符合要求，官方文档如下图所示：
        >
        >             ![img](https://img.quanxiaoha.com/quanxiaoha/172278126657189)
        >
        >         - 对 MySQL 索引不利：如果作为数据库主键，以 InnoDB 引擎为例，InnoDB 使用基于磁盘的 B+ 树表示数据页，并以主键作为索引，即 B+ 树按照主键从小到大的顺序排列数据，每次写入新数据时，数据都会被顺序添加到对应数据页的尾部。一个数据页写满后， B+ 树会自动开辟一个新的数据页。但是 UUID 是无序性的，它的随机性导致每次有新数据写入时，可能会被插入数据页中间某个位置，为了腾出位置给新数据，B+ 树不得不将已有的数据向后移动，数据位置频繁变动，会严重影响性能。

    - **基于数据库（DB）的自增 ID** ：可以单独创建一张共享的 ID 生成表，使用自增字段来生成 ID，再存到业务表主键字段中。

        > - 优点：
        >     - 实现非常简单，利用现有的数据库即可搞定；
        >     - ID 单调递增；
        > - 缺点：
        >     - 强依赖 DB，当 DB 异常时整个系统不可用，属于致命问题。配置主从复制可以尽可能的增加可用性，但是数据一致性在特殊情况下难以保证。主从切换时的不一致可能会导致重复发号。
        >     - ID 发号性能瓶颈限制在单台 MySQL 的读写性能。

    - **基于分布式协调服务：** 利用 Zookeeper、Etcd 等分布式协调服务，可以实现 ID 的有序分配。虽然这种方法可以保证 ID 的顺序性，但引入了外部依赖，增加了系统的复杂度。

    - **基于分布式缓存**：使用 Redis 的 `INCRBY` 命令，可以为键 （Key）的数字增加指定增量。如果键不存在，则数值会被初始化为 0，然后再执行增量操作。

    - **基于 Snowflake 算法（雪花算法）**： Snowflake 算法由 Twitter 开发，它结合了时间戳、机器 ID 和序列号，生成 64 位的 ID，如下图所示：

        > ![img](https://img.quanxiaoha.com/quanxiaoha/172282552139353)
        >
        > - **1bit**: 符号位（标识正负），不作使用，始终为 0，代表生成的 ID 为正数。
        > - **41-bit 时间戳**: 一共 41 位，用来表示时间戳，单位是毫秒，可以支撑 2 ^41 毫秒（约 69 年）
        > - **datacenter id + worker id (10 bits)**: 一般来说，前 5 位表示机房 ID，后 5 位表示机器 ID（项目中可以根据实际需求来调整）。这样就可以区分不同集群/机房的节点。
        > - **12-bit 序列号**: 一共 12 位，用来表示序列号。 序列号为自增值，代表单台机器每毫秒能够产生的最大 ID 数(2^12 = 4096),也就是说单台机器每毫秒最多可以生成 4096 个 唯一 ID。理论上 snowflake 方案的QPS约为 409.6w /s，这种分配方式可以保证在任何一个 IDC 的任何一台机器在任意毫秒内生成的 ID 都是不同的。
        >
        > snowflake 雪花算法优缺点如下：
        >
        > - 优点：
        >     - 毫秒数在高位，自增序列在低位，整个ID都是趋势递增的。
        >     - 不依赖数据库等第三方系统，以服务的方式部署，稳定性更高，生成ID的性能也是非常高的。
        >     - 可以根据自身业务特性分配bit位，非常灵活。
        > - 缺点：
        >     - 强依赖机器时钟，如果机器上时钟回拨，会导致发号重复或者服务会处于不可用状态。

    

    

## Zookeeper 介绍

Apache ZooKeeper 是一个开源的分布式协调服务，用于大型分布式系统的开发和管理。它提供了一种简单而统一的方法来解决分布式应用中常见的协调问题，如命名服务、配置管理、集群管理、组服务、分布式锁、队列管理等。ZooKeeper 通过提供一种类似文件系统的结构来存储数据，并允许客户端通过简单的 API 进行读写操作，从而简化了分布式系统的复杂度。

Zookeeper 的核心特性如下：

1. **一致性**：对于任何更新，所有客户端都将看到相同的数据视图。这是通过 ZooKeeper 的原子性保证的，意味着所有更新要么完全成功，要么完全失败。
2. **可靠性**：一旦数据被提交，它将被持久化存储，即使在某些服务器出现故障的情况下，数据也不会丢失。
3. **实时性**：ZooKeeper 支持事件通知机制，允许客户端实时接收到数据变化的通知。
4. **高可用性**：ZooKeeper 通常以集群形式部署，可以容忍部分节点的故障，只要集群中超过半数的节点是可用的，ZooKeeper 就能继续提供服务。

ZooKeeper 的数据模型：

> ZooKeeper 使用一个层次化的命名空间来组织数据，类似于文件系统中的目录树。每个节点（称为 znode）都可以有子节点，形成树状结构。每个 znode 可以存储一定量的数据，并且可以设置访问控制列表（ACL）来控制谁可以读取或修改数据。

ZooKeeper 的应用场景：

- **配置管理**：ZooKeeper 可以用来集中存储和管理分布式系统中的配置信息，当配置发生变化时，可以实时通知到所有客户端。
- **命名服务**：ZooKeeper 可以作为服务发现的注册中心，帮助客户端查找和定位服务。
- **集群管理**：ZooKeeper 可以用于选举主节点、检测集群成员的变化、以及监控集群的健康状况。
- **分布式锁**：ZooKeeper 提供了一种机制来实现分布式环境下的互斥访问，保证多个进程之间数据操作的正确性。
- **队列管理**：ZooKeeper 可以用来实现分布式队列，如任务调度队列或消息队列。

# 十三、 笔记服务搭建与开发



- **笔记详情浏览**：可查看用户发布的笔记详情，如图片、视频、标题、正文等；

    ![img](https://img.quanxiaoha.com/quanxiaoha/172344556031228)

- **发布笔记**：允许用户上传文本、图片、视频等内容，并添加标题、话题和位置信息；

    ![img](https://img.quanxiaoha.com/quanxiaoha/172344918202730)

- **编辑笔记**：用户可以对已发布的笔记进行修改，再发布；

- **仅对自己可见**：针对发布成功的笔记，可以进行权限设置 —— 仅对自己可见，其他人则无法查看该笔记；

    ![img](https://img.quanxiaoha.com/quanxiaoha/172344627480751)

- **笔记置顶**：置顶某篇笔记，访问用户主页时，被置顶的笔记处于最前面；

    ![img](https://img.quanxiaoha.com/quanxiaoha/172344609322812)

- **笔记删除**：可以对已发布的笔记进行删除处理；

    ![img](https://img.quanxiaoha.com/quanxiaoha/172344614726819)

* 频道与话题

在笔记发布页中，可点击**参与话题**。为笔记添加话题，可以或得平台更高的曝光量，让更多人看到。如下图所示，话题归属于频道之下，每个频道下都有一些常用的话题，可以被选择：

![img](https://img.quanxiaoha.com/quanxiaoha/172344655471973)

频道表

```sql
CREATE TABLE `t_channel` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '频道名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道表';

```

话题表

```sql
CREATE TABLE `t_topic` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '话题名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题表';

```

频道-话题关联表

```sql
CREATE TABLE `t_channel_topic_rel` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_id` bigint(11) unsigned NOT NULL COMMENT '频道ID',
  `topic_id` bigint(11) unsigned NOT NULL COMMENT '话题ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道-话题关联表';

```

笔记表

```sql
CREATE TABLE `t_note` (
  `id` bigint(11) unsigned NOT NULL COMMENT '主键ID',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `is_content_empty` bit(1) NOT NULL DEFAULT b'0' COMMENT '内容是否为空(0：不为空 1：空)',
  `creator_id` bigint(11) unsigned NOT NULL COMMENT '发布者ID',
  `topic_id` bigint(11) unsigned DEFAULT NULL COMMENT '话题ID',
  `topic_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '话题名称',
  `is_top` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否置顶(0：未置顶 1：置顶)',
  `type` tinyint(2) DEFAULT '0' COMMENT '类型(0：图文 1：视频)',
  `img_uris` varchar(660) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '笔记图片链接(逗号隔开)',
  `video_uri` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频链接',
  `visible` tinyint(2) DEFAULT '0' COMMENT '可见范围(0：公开,所有人可见 1：仅对自己可见)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(2) NOT NULL DEFAULT '0' COMMENT '状态(0：待审核 1：正常展示 2：被删除(逻辑删除) 3：被下架)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

```



## 流程图

![img](https://img.quanxiaoha.com/quanxiaoha/172370521354350)

- 请求笔记发布接口前，用户填写笔记内容时，先将笔记图片或视频上传至 OSS 对象存储服务，拿到资源的访问直链；
- 用户填写好笔记标题、内容、话题等等后，点击发布按钮，请求由网关转发给笔记服务；
- 笔记服务请求分布式 ID 生成服务，为该条笔记生成一个全局唯一 ID;
- 判断笔记内容是否为空，若不为空，则调用 KV 键值服务，将笔记内容存到 Cassandra 数据库中；
- 确认 KV 键值服务保存成功后，再将笔记元数据存储到 MySQL 数据库中，并提示用户发布成功；



与访问磁盘上的数据相比，访问内存中的数据要快得多的多，因此**内存级缓存可以极大地提高系统的响应速度和效率**。接下来，我们将为用户信息查询接口添加 Redis 缓存，处理流程图如下：

![img](https://img.quanxiaoha.com/quanxiaoha/172395375836735)

> 流程如下：
>
> - 当调用用户信息查询接口时，比如，想要获取用户 ID 为 1 的用户信息；
> - 笔记服务会先从 Redis 缓存中，查询是否有该用户的数据；
> - 如果有，直接返回数据，无需再查询数据库；
> - 如果缓存中无该用户的数据，则走数据查询，查询成功后响应数据，同时，将该用户的数据写入 Redis 中；
> - 那么，后续再次访问该用户的数据时，都可以直接从 Redis 缓存中拿了；



## 缓存常见问题

缓存技术固然好，但是也会带来一些问题。

### 缓存雪崩

> 解释： **当大量的缓存数据在同一时间失效，导致大量的请求直接打到数据库上，这种现象称为缓存雪崩。**

如下图所示，假设 Redis 中存储了大量用户信息缓存同时失效，导致大量请求落到数据库上，可能导致数据库雪崩，直接导致整个服务不可用。

![img](https://img.quanxiaoha.com/quanxiaoha/172395489310115)

#### 解决方案

> 为了避免以上情况的发生，比较常见的做法是，在存入缓存时，为它们**设置不同的失效时间**，从而避免同一时间，大量缓存同时失效的情况发生。

### 缓存穿透

> 解释：**缓存穿透是指查询一个缓存中不存在的数据，同时，数据库中同样不存在该条数据。**

举个栗子，假设系统被恶意攻击，查询一个不存在的用户，如用户 ID 为 -1， 按照之前的逻辑，会先从 Redis 中查询，缓存中不存在，然后会走数据库查询，依然不存在，那么此类请求永远都会打到数据库。如果被恶意发起大量此类请求，缓存形同虚设，依然会带来数据库雪崩，直接导致整个服务不可用。

![img](https://img.quanxiaoha.com/quanxiaoha/172396220066676)

#### 解决方案

> 为了应对上述情况，可以选择如下两种策略：
>
> - **当数据库中找不到某条数据时，可以在 Redis 中缓存一个空值**，表示该条数据不存在。这样后续的此类请求，都会命中缓存，从而拦截非法请求对数据库的压力；
>
>     > 这样也会带来另一个问题，如果被恶意请求大量不同的非法数据，如用户 ID 为 -1，-2，-3 ...，此方案会使 Redis 中存储大量无用的空数据，极端情况下，甚至会驱逐出合法的缓存数据，导致缓存命中率大大的降低，数据库再次面临访问量激增的风险。
>     >
>     > 为了解决此问题，可以对不存在的空缓存，**设置一个较短的过期时间**，防止 Redis 内存占用持续增加。
>
> - 引入**布隆过滤器**；
>
>     > 布隆过滤器（Bloom Filter）是一种数据结构，用于判断一个元素是否在一个集合中。它的特点是空间利用高且查询速度快，但是会有一定的误判率，即对于 “数据不存在” 的判定是准确的，但是对 “数据存在” 有一定的误判。
>     >
>     > 布隆过滤器非常适合用来防止内存穿透，它的处理流程大致如下：
>     >
>     > - 将数据库中的所有用户数据加入布隆过滤器中；
>     > - 当查询某条用户数据，且 Redis 中不存在时，开始检查布隆过滤器是否记录了此数据；
>     > - 若布隆过滤器认为 “不存在”，由于布隆过滤器对此类判断绝对准确，故而则认为数据库中也不存在该数据，直接响应该用户不存在；
>     > - 若布隆过滤器认为 “存在”，则走数据库查询，并加入 Redis 缓存中。虽然有一定误判，但是误判率较低，仅会存缓存少许空值。不会存储大量无用空数据，导致合法数据被驱逐，缓存命中率降低的情况。

### 缓存击穿

> 解释：**缓存击穿是指某个热点数据在失效的瞬间，大量的并发请求直接打到了数据库上，导致数据库压力过大。**

举个栗子，如下图所示，假设某一篇笔记爆火，存储在 Redis 中的该条笔记缓存突然失效，一瞬间涌入大量请求，此时新的缓存还没有更新到 Redis 中，所有查询全部落到数据库，可能导致数据库崩溃。

![img](https://img.quanxiaoha.com/quanxiaoha/172396268397646)

#### 解决方案

> 为了应对上述情况，可以选择如下方案：
>
> - 缓存不设置过期时间：缓存不会过期被删除，自然不会有缓存击穿的风险；
>
> - 加分布式锁；
>
>     > 当一个热点数据在缓存中失效时，可以使用分布式锁来确保只有一个服务实例去数据库中获取数据并更新缓存。这样可以避免大量请求同时访问数据库。
>
> - 引入本地缓存，和 Redis 形成二级缓存，互成犄角之势，并且两种缓存过期时间都会设置的不一样:
>
>     > 如下图所示，笔记服务引入本地缓存，搭配 Redis 分布式缓存，同时，3 个笔记服务实例的本地缓存，和 Redis 缓存的过期时间都不一样。
>     >
>     > 即使 Redis 缓存过期了，有本地缓存顶着，随着时间的流逝，必定只有一个实例的本地缓存先失效，假设是实例 3 本地缓存先失效，这个时候，只有部分流量会到达实例 3，最终查询数据库，将查询出来的结果再次存入 Redis 中，同时更新实例 3 的本地缓存；
>     >
>     > ![img](https://img.quanxiaoha.com/quanxiaoha/172397728455065)
>
> - 热点 Key 探测，延长缓存失效时间；
>
>     > 在第三种方案的基础上，还可以再引入热点 Key 探测机制，针对访问频率较高的 Redis 缓存，且快要过期的 Key，自动为其延长到期时间，从而防止缓存击穿。



# 十四、消息中间件 RocketMQ

## 什么是 MQ

消息中间件（Message Queue, MQ）是一种软件基础设施，**用于在分布式系统中实现异步通信和解耦合**。它允许应用程序通过消息队列发送和接收数据，从而实现不同组件之间的通信。

## 基本概念

- **消息**：数据的最小单位，通常是应用程序之间传递的信息。
- **队列**：一种数据结构，用于存储消息。消息按顺序进入队列，并按顺序被处理。
- **生产者（Producer）**：发送消息的一方。
- **消费者（Consumer）**：接收和处理消息的一方。

## 通信模型

消息中间件通常遵循两种通信模型：

- **点对点模型（Point-to-Point, P2P）**：在P2P模型中，每条消息只能被一个消费者消费。一旦消息被某个消费者处理后，就不再可用。这种模型适用于一对一的情况，比如发送任务给工作者节点。类似于单独给某人发送手机短信，只有指定的人才能收到：

    ![img](https://img.quanxiaoha.com/quanxiaoha/172474415592758)

- **发布/订阅模型（广播）（Publish/Subscribe, Pub/Sub）**：在Pub/Sub模型中，生产者发布消息到主题，任何订阅了该主题的消费者都会接收到消息。这种模型允许一对多的通信，即多个消费者可以监听同一个主题。类似于学校广播通知，所有同学都能接收到：

![img](https://img.quanxiaoha.com/quanxiaoha/172474399780343)

## 适用场景

- **异步处理**：当任务需要较长时间处理时，MQ 允许应用程序将任务放入队列，并立即返回响应。任务将由消费者异步处理，避免阻塞主线程。

- **解耦应用程序**：在一个复杂的系统中，不同的模块或服务可能需要进行通信。MQ 可以帮助解耦这些模块，使它们独立运行，彼此之间通过消息传递进行通信。典型的场景就是用户下单，下单后需要扣减库存、增加用户积分、通知发货等等，订单系统只需要发送一条 MQ, 即可通知对应系统处理相关逻辑。

    ![img](https://img.quanxiaoha.com/quanxiaoha/172474908150924)

- **流量削峰**：在高并发场景下，瞬时请求量可能会超过系统的处理能力。MQ 可以暂时缓存这些请求，平滑处理压力。比较典型的场景就是短视频点赞，某条短视频爆火，短时间内引起大量用户点赞，流量巨大，直接操作数据，会打垮数据库。可以通过引入 MQ, 用户点赞后，直接发送一条消息，消费者按一定速率慢慢处理（削峰），防止数据库压力过大。

    ![img](https://img.quanxiaoha.com/quanxiaoha/172474945289210)

- **日志处理**：MQ 常用于收集和处理日志信息，例如分布式系统中的日志数据集中处理。

- **事件驱动系统**：MQ 可以在事件驱动架构中作为事件触发的中介，使系统能够对事件做出实时响应。

## 流行的 MQ 对比

| **维度**           | **ActiveMQ**                        | **RabbitMQ**                        | **RocketMQ**                         | **Kafka**                              |
| ------------------ | ----------------------------------- | ----------------------------------- | ------------------------------------ | -------------------------------------- |
| **协议支持**       | AMQP, MQTT, STOMP, OpenWire, etc.   | AMQP, MQTT, STOMP, HTTP, WebSockets | RocketMQ native protocol, HTTP, etc. | Kafka native protocol                  |
| **消息模型**       | Queue, Topic, Virtual Topic, etc.   | Queue, Topic                        | Queue, Topic                         | Topic, Partition, Consumer Groups      |
| **持久化机制**     | 支持多种持久化方案，如 KahaDB, JDBC | 支持持久化，通过插件可支持多种方式  | 文件存储，支持高效的顺序写           | 基于磁盘的日志存储，高效顺序写         |
| **吞吐量**         | 中等，适合中小规模业务              | 中等偏高，适合中小规模业务          | 高吞吐量，适合大规模业务             | 极高，适合超大规模实时流处理           |
| **消息延迟**       | 低至中，视配置而定                  | 低至中，视配置而定                  | 低延迟，适合实时消息传输             | 极低，适合实时数据流                   |
| **消息顺序**       | 支持，但需配置                      | 支持，需配置                        | 支持严格顺序                         | 支持分区内严格顺序                     |
| **消息保序性**     | 支持，按 Topic 或 Queue 保序        | 支持，按 Queue 保序                 | 支持，按 Topic 保序                  | 支持，按 Partition 保序                |
| **事务支持**       | 支持分布式事务 (XA)                 | 支持，带内嵌事务                    | 原生支持分布式事务                   | 部分支持，通过幂等实现                 |
| **扩展性**         | 中等，支持集群扩展                  | 中等，支持集群扩展                  | 高扩展性，支持水平扩展               | 极高，天然支持水平扩展                 |
| **易用性**         | 简单，文档丰富                      | 简单，文档丰富                      | 需要一定的学习成本                   | 学习成本高                             |
| **社区支持与生态** | 社区活跃不太高                      | 社区非常活跃，插件极多              | 国内社区活跃，阿里巴巴开发维护       | 社区活跃，生态极为丰富                 |
| **可靠性**         | 高，支持多种持久化及备份策略        | 高，具备多种消息确认机制            | 高，企业级消息中间件，支持消息堆积   | 非常高，分布式架构，支持冗余和故障恢复 |
| **消息丢失风险**   | 较低，可配置保证                    | 较低，支持消息持久化及确认机制      | 非常低，支持事务消息                 | 非常低，支持副本机制                   |
| **管理工具**       | 提供 Web 管理界面                   | 提供 Web 管理界面                   | 提供命令行工具和 Web 界面            | 提供 CLI 工具及第三方 Web 界面         |
| **典型应用场景**   | 适合中小企业的应用集成及轻量级任务  | 广泛用于微服务架构与实时数据处理    | 适合大规模分布式系统及企业级应用     | 适合大数据、日志收集、实时分析         |
| **开发语言**       | Java                                | Erlang                              | Java                                 | Scala, Java                            |
| **商业支持**       | 提供商业支持                        | 提供商业支持                        | 提供商业支持                         | 提供商业支持，Confluent 提供支持       |

### 选择建议：

- **ActiveMQ** 适合中小企业的应用集成和轻量级任务，支持多种协议，配置和使用相对简单。
- **RabbitMQ** 适合微服务架构和实时数据处理，具有高可用性和丰富的插件支持，社区非常活跃。
- **RocketMQ** 适合大规模分布式系统，支持事务和消息堆积，阿里巴巴主导，适合需要高性能和低延迟的场景。
- **Kafka** 适合超大规模的实时流数据处理，大数据处理的首选，具有极高的吞吐量和低延迟，但学习成本较高。



## 什么是双写一致性？

> 双写一致性是指在数据更新时，确保多个存储系统（如数据库和缓存）中的数据保持一致的状态，避免脏读。

结合小哈书的实际场景来讲解，如下图所示：

![img](https://img.quanxiaoha.com/quanxiaoha/174332426228074)

> 上图模拟了这么一个并发场景，系统同时接收到了笔记详情查询接口，以及笔记更新接口的请求。并且此时， Redis 中的笔记详情缓存已经失效，极端情况下，服务端的处理流程可能如下：
>
> - 笔记查询接口先被处理，查询 Redis 缓存，未命中，走数据库查询（老数据），还没来得及同步到 Redis 中；
> - 另一边笔记更新接口，正常执行逻辑，更新了数据库，删除了 Redis 缓存；
> - 此时，笔记查询接口才将之前查出的老笔记数据，同步到了 Redis 中，导致发生了数据不一致，用户后续读取到的，并不是更新后的笔记，还是老数据；



## 延迟双删

延迟双删策略（**Delayed Double Deletion**）是一种用于解决 **数据库与缓存双写场景下数据不一致问题** 的优化方案。其核心思想是通过 **两次删除缓存** 并结合 **延迟时间**，**尽可能减少**因并发操作或主从同步延迟导致的脏数据残留问题。

具体处理如下图：

![img](https://img.quanxiaoha.com/quanxiaoha/174332451603840)

> - 笔记更新接口中，先删除 Redis 缓存，再更新数据库；
> - 若此时笔记查询接口，将老数据同步到了 Redis 中；
> - 笔记更新接口，会再删除一次 Redis 缓存，防止老数据回填；



# （十五、十六）、用户关系服务搭建与开发



## 职责说明

用户关系服务主要负责的职责如下：

- **关注与取关接口**：用户 A 可以关注用户 B ，关注成功后，也可以取关用户 B。

    ![img](https://img.quanxiaoha.com/quanxiaoha/172534725879358)

- **查询某个用户的关注列表接口**：当用户 A 关注了用户 B 后，用户 A 的关注列表中就会出现用户 B。

    ![img](https://img.quanxiaoha.com/quanxiaoha/172534770375426)

    ![img](https://img.quanxiaoha.com/quanxiaoha/172534775422889)

- **查询某个用户的粉丝列表接口**：当用户 A 关注了用户 B 后，用户 B 的粉丝列表中就会出现用户 A。

    ![img](https://img.quanxiaoha.com/quanxiaoha/172534784412344)

- **查询用户关系接口**：即查询用户 A 是否已经关注了用户 B。如下图所示，当用户 A 关注了用户 B 后，再次进入用户 B 的主页后，原本展示的关注按钮，会变成已关注的按钮，这个数据需要告诉给前端，以便展示不同的 UI：

    ![img](https://img.quanxiaoha.com/quanxiaoha/172534794422315)

## 表设计

了解了用户关系服务的职责后，我们设计一下相关表结构。

### 关注表

首先是用户关注表，建表语句如下：

```sql
CREATE TABLE `t_following` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `following_user_id` bigint unsigned NOT NULL COMMENT '关注的用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

```

```sql
CREATE TABLE `t_fans` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `fans_user_id` bigint unsigned NOT NULL COMMENT '粉丝的用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户粉丝表';

```



![img](https://img.quanxiaoha.com/quanxiaoha/172576981276338)



- **校验关注的目标用户是否是自己**，自己无法关注自己；

    > 直接从上下文中获取当前登录的用户 ID, 与入参中的关注目标用户 ID 做比较，若相同，则抛出业务异常；

- **校验关注是否达到上限**；

    > 查询 Redis ，获取当前用户的 ZSET 关注列表，通过 ZCARD 命令获取列表大小，若大于等于 1000 则提示已经达到关注上限；

- **校验关注的用户是否存在**；

    > RPC 调用用户服务，查询关注的目标用户是否存在，真实存在才允许关注；

- **将关注的用户写入 Redis ZSET 关注列表中**；

- **发送MQ , 直接响应结果**；

    - **由消费者异步将数据落库**；

        > 如用户 A 关注了用户 B, 则在用户 A 的关注表中写入一条记录，并在用户 B 的粉丝表中写入一条记录：
        >
        > - 关注表写入一条记录；
        > - 粉丝表写入一条记录；

此套方案，左边的关注主流程不用与数据库交互，只依赖于 Redis，能够大大提升关注接口的抗并发能力。

### MQ 消费者

再来说说 MQ 消费者这边要做的工作，流程如下：

![img](https://img.quanxiaoha.com/quanxiaoha/172577923844727)

- **流量削峰**：可以通过令牌桶策略，控制消费者的消费速度，以数据库可承受的频率访问来访问，防止流量太大，将数据库打垮；

- **幂等判断**；

    > 幂等性意味着即使同样的消息被重复发送给消费者多次，消费者也应该能够确保业务逻辑的一致性和正确性。

- **数据落库**；

    - 关注表写入一条记录；
    - 粉丝表写入一条记录；

- **更新 Redis ZSET 粉丝列表缓存**；

- **发送一条计数 MQ, 通知计数服务重新统计用户关注总数、粉丝总数**



![img](https://img.quanxiaoha.com/quanxiaoha/172602650035827)



## 引入 Lua 脚本

![img](https://img.quanxiaoha.com/quanxiaoha/172601975371614)

Lua 脚本是一种轻量级、嵌入式脚本语言，用于在 Redis 中实现更复杂的原子操作。通过 Redis 内置的 Lua 解释器，你可以在 Redis 上运行 Lua 脚本，使一组命令被打包为一个原子操作执行，而不是逐个命令执行。它的优势如下：

- **原子性**：Redis 在执行 Lua 脚本时, 会确保脚本中所有命令作为一个整体，按顺序执行，执行过程中不会被其他客户端命令打断，直到脚本执行结束;
- **减少网络延迟**：在没有 Lua 脚本的情况下，客户端需要向 Redis 发送多个命令，每次通信都会有网络延迟。通过 Lua 脚本，多个命令可以被组合为一个脚本，从而减少网络交互的次数，提升性能。
- **实现复杂逻辑**：有时单个 Redis 命令无法完成复杂的逻辑。Lua 脚本可以编写更复杂的业务逻辑，比如批量处理数据、实现事务控制、数据校验等。
- **提高效率**：当你需要进行条件判断、循环或批量操作时，用 Lua 脚本可以提高效率，而不是在客户端侧进行复杂的逻辑判断后再发回 Redis 操作。

我们可以为之前的逻辑引入 Lua 脚本，如下图框中标注所示，可引入 3 个 Lua 脚本：

![img](https://img.quanxiaoha.com/quanxiaoha/172603636686882)

- **Lua 脚本1**：此脚本主要保证框中 Redis 操作的原子性，并减少网络 IO 的交互次数，提升性能;
- **Lua 脚本2**：此脚本对应框中的逻辑无需保证原子性，主要是减少网络 IO 的交互次数，提升性能;
- **Lua 脚本3**：此脚本对应框中的逻辑无需保证原子性，主要是减少网络 IO 的交互次数，提升性能;



## 什么是 Tag ?

> 以下概念介绍，摘取自 RocketMQ 官网：https://rocketmq.apache.org/zh/docs/4.x/producer/01concept1 。

Topic 与 Tag 都是业务上用来归类的标识，区别在于 Topic 是一级分类，而 Tag 可以理解为是二级分类。使用 Tag 可以实现对 Topic 中的消息进行过滤。

> 提示
>
> - Topic：消息主题，通过 Topic 对不同的业务消息进行分类。
> - Tag：消息标签，用来进一步区分某个 Topic 下的消息分类，消息从生产者发出即带上的属性。

Topic 和 Tag 的关系如下图所示:

![img](https://img.quanxiaoha.com/quanxiaoha/172615010703931)

## 什么时候该用 Topic，什么时候该用 Tag？

可以从以下几个方面进行判断：

- 消息类型是否一致：如普通消息、事务消息、定时（延时）消息、顺序消息，不同的消息类型使用不同的 Topic，无法通过 Tag 进行区分。
- 业务是否相关联：没有直接关联的消息，如淘宝交易消息，京东物流消息使用不同的 Topic 进行区分；而同样是天猫交易消息，电器类订单、女装类订单、化妆品类订单的消息可以用 Tag 进行区分。
- 消息优先级是否一致：如同样是物流消息，盒马必须小时内送达，天猫超市 24 小时内送达，淘宝物流则相对会慢一些，不同优先级的消息用不同的 Topic 进行区分。
- 消息量级是否相当：有些业务消息虽然量小但是实时性要求高，如果跟某些万亿量级的消息使用同一个 Topic，则有可能会因为过长的等待时间而“饿死”，此时需要将不同量级的消息进行拆分，使用不同的 Topic。

总的来说，针对消息分类，您可以选择创建多个 Topic，或者在同一个 Topic 下创建多个 Tag。但通常情况下，不同的 Topic 之间的消息没有必然的联系，而 Tag 则用来区分同一个 Topic 下相互关联的消息，例如全集和子集的关系、流程先后的关系。

> 在小哈书项目中，用户关注、取关操作也可以划分为同一类，公用同一个 Topic， 并通过打上不同 Tag 标签，来区分不同的操作类型。

### 什么是幂等性？

幂等性是指在分布式系统中，对同一操作执行多次，最终的结果是一样的，不论这个操作被执行了一次还是多次，系统状态应保持一致。

### 为什么消费者需要保证幂等性？

在分布式消息队列（如 RocketMQ）中，消费者消费消息时有可能会遇到重复消费的问题，这种重复消费可能是由于以下原因导致的：

1. **消息重发**：在消息队列系统中，如果消费者消费完消息后没有及时反馈给消息队列（即没有发送确认 `ACK`），消息队列会认为消息没有被成功消费，于是会重新投递这条消息。这种情况下，消费者可能会收到同一条消息多次。
2. **网络问题或超时**：在网络不稳定或超时的情况下，消息的确认状态可能未及时传递给消息队列，导致消息被多次投递。
3. **消息处理失败重试**：如果消费者在处理某条消息时失败，为了保证消息不丢失，系统可能会重新消费这条消息，也就是触发重试机制。

在这些情况下，消费者有可能多次收到相同的消息，因此为了避免重复执行可能会影响业务数据的操作，**必须保证幂等性**，即不论消费者重复消费多少次相同的消息，结果应该始终一致。

### 如何实现关注操作的幂等性

我们可以为 `t_following` 表添加**联合唯一索引**，索引中包含用户 ID 和被关注用户的 ID, 执行 SQL 语句如下：

```sql
ALTER TABLE t_following ADD UNIQUE uk_user_id_following_user_id(user_id, following_user_id);
```

这样，不论重复消费消息多少次，最终存到关注表中的关系数据只会有一条。



### 何时同步粉丝列表到 Redis？

初次同步粉丝列表数据到 Redis ， 这个步骤可以放到**粉丝列表接口**中做（目前这个接口还没开发），如下图所示：

![img](https://img.quanxiaoha.com/quanxiaoha/172674846362122)

> - 当请求粉丝列表接口时；
> - 查询 Redis 中，是否有目标用户的 `ZSET` 粉丝列表；
> - 若有，直接返回分页数据；
> - 若无，则查询数据库，返回分页数据，并异步将粉丝列表数据同步到 Redis 中；

### MQ 消费者更新粉丝列表

![img](https://img.quanxiaoha.com/quanxiaoha/172675081555426)

而关注操作的 MQ 消费者中，当将关注表、粉丝表 2 条记录写入数据库后，其逻辑如下：

- 若编程式事务提交成功；
- 查询 Redis, 判断该被关注用户的 `ZSET` 粉丝列表是否存在；
- 若不存在，说明粉丝列表还没有被请求，`ZSET` 粉丝列表还没被初始化，只需要将关注表、粉丝表两条数据落库即可；
- 若存在，则获取 `ZSET` 粉丝列表大小；
    - 若小于 5000， 则直接 `ZADD` 添加粉丝关系；
    - 若超过 5000 个粉丝，则使用 `ZPOPMIN` 命令移除最早关注的粉丝，再将最新关注的用户 `ZADD` 添加到粉丝列表中;



## 取消关注接口



### Redis

- 先判断 Redis 是否有数据，有则通过缓存来判断；
- 否则，从数据库查询当前用户的关注关系记录：
    - 若记录为空，则表示还未关注任何人，抛出业务异常，提示用户 “你未关注对方，无法取关”；
    - 若记录不为空，则将关注关系数据全量同步到 Redis 中，并设置过期时间，再次判断用户是否关注 “取关的目标用户”；



## 批量查询用户





# （十七、十八）、计数服务搭建与开发



## 何时触发计数？

触发场景如下，比如用户 A 关注了用户 B:

![img](https://img.quanxiaoha.com/quanxiaoha/172828381020834)

当用户关系服务将相关数据落库后，可以发送 MQ 通知到计数服务，开始进行计数统计：

![img](https://img.quanxiaoha.com/quanxiaoha/172828732981970)

> 拿用户 A 关注了用户 B来说，计数服务需要执行如下操作：
>
> - **用户 A**: 关注数加一；
> - **用户 B**: 粉丝数加一；

反之，当用户 A 取关了用户 B:

![img](https://img.quanxiaoha.com/quanxiaoha/172828384299878)

> 计数服务需要执行如下操作：
>
> - **用户 A**: 关注数减一；
> - **用户 B**: 粉丝数减一；

## 计数场景分析

再来看看两个维度的计数，是否涉及高并发：

- **关注数**：每个用户的关注数有上限，并且单个用户手动操作，不能短时间内关注大量用户，所以，此处的计数业务场景**并发数较少**；
- **粉丝数**：若某个用户突然爆火，会引来大量关注，属于**高并发写场景**；

## 直接操作数据库合适吗？

在 [《17.1节》](https://www.quanxiaoha.com/column/10378.html) 中，我们已经将用户维度的 `t_user_count` 计数表创建完成了：

![img](https://img.quanxiaoha.com/quanxiaoha/172828467251361)

那么，计数时直接操作数据库，进行更新合适吗？**肯定是不合适的**，即使是针对关注数计数，并发较少的场景，但是考虑到平台用户数庞大，直接操作数据库，数据库也是压力非常大！

**我们可以先在 Redis 中进行计数，然后异步将计数数据写入数据库中**：

![img](https://img.quanxiaoha.com/quanxiaoha/172828593240642)

## 如何操作 Redis？

我们可以采用 Redis 中的 Hash 数据结构来存储用户维度的计数数据。

### 什么是 Redis Hash？

> Redis 的哈希（Hash）是一种数据结构，它存储的是字段和值之间的映射关系。在 Redis 中，哈希可以看作是一个字符串字段和字符串值的字典，非常适合用于表示对象。例如，你可以用哈希来表示一个用户信息，其中键是用户的唯一标识符，而字段可能包括用户名、电子邮件地址等。
>
> 哈希在 Redis 中是非常高效的数据结构，尤其是当你的哈希中包含的字段数量不多时（通常少于几百个字段）。**如果你需要在一个键下面存储多个相关的字段，那么使用哈希会比创建多个独立的键更加节省内存，并且访问起来也更快。**

拿粉丝总数来说，如果要执行加一操作，执行 Redis 命令大致如下：

```php-template
HINCRBY count:user:<用户ID> fansTotal 1
```

> - `HINCRBY key field increment`：这个命令将哈希表 `key` 中的字段 `field` 的值加上增量 `increment`。增量可以是负数，此时就是减法操作。如果哈希表不存在，一个新的哈希表会被创建并执行 HINCRBY 操作。如果字段不存在，那么在执行操作之前它的值被看作 0 。字段的值和增量都必须是整数。

比如，想对用户 ID 为 1 的粉丝总数进行加一操作，具体命令如下：

```sql
HINCRBY count:user:1 fansTotal 1
```

![img](https://img.quanxiaoha.com/quanxiaoha/172828500000752)

通过 Redis 客户端查看，效果如下：

![img](https://img.quanxiaoha.com/quanxiaoha/172828506041551)

如果是粉丝数减一操作，命令大致如下：

```php-template
HINCRBY count:user:<用户ID> fansTotal -1
```

比如，想对用户 ID 为 1 的粉丝总数进行减一，具体命令如下：

```sql
HINCRBY count:user:1 fansTotal -1
```

![img](https://img.quanxiaoha.com/quanxiaoha/172828519373055)

效果如下：

![img](https://img.quanxiaoha.com/quanxiaoha/172828524723330)

## 高并发写优化

虽然目前设计中，计数处理都放在了 Redis 中，并且对于写操作，单机 Redis 的性能大约能达到 80,000 到 100,000 次/秒。但是，如果每次计数操作，都是加减一操作，写入频率也是非常高的，Redis 的压力也是非常大的！

此时，我们可以再优化一下，引入另一种高并发写策略 —— **聚合写**。

![img](https://img.quanxiaoha.com/quanxiaoha/172830132771081)

如上图所示，聚合写流程如下：

- 计数操作达到 1000 条，聚合一次；

    > 针对关注、取关的计数业务，要么是 `+1` 操作, 要么是 `-1` 操作 , 聚合时无需关注顺序问题，数据也能达到最终一致性。比如下面这样：
    >
    > ```diff
    > +1
    > +1
    > -1
    > +1
    > ```
    >
    > 无论顺序如何颠倒，聚合到一起后，最终还是执行 `+2` 操作，这样只需要操作一次 Redis 与数据库。

- 聚合还应该有时间窗口，假设 1s 内未达到 1000 条，也不能一直等着，为了数据的及时性，达到时间也需要执行聚合；

使用聚合写策略后，优势如下：

> - **减少写操作次数**：通过聚合写，可以将多个单独的操作合并成一个操作。例如，当很多用户同时关注或取消关注某个用户时，可以将这些操作累积起来，并定期或者达到一定数量后一次性更新 Redis、数据库中的计数器。这样就减少了对 Redis、数据库的写入次数，从而减轻了访问压力。
> - **提高系统吞吐量**：由于减少了单个写请求的数量，整体上系统的吞吐量会得到提升。对于高并发场景下的应用来说，这是一个非常重要的考虑因素。



## 点赞列表设计

用户点赞笔记通常没有数量限制，虽然说，点赞列表只有个人才能查看，但是平台用户数众多，如果全都走数据库查询，肯定是不合适的！缓存还是得加的！

查看点赞列表有如下特点：

- 点赞列表中，最近被点赞的笔记，被再次查看的几率较高；
- 而点赞时间过久的笔记，曝光率则非常低；

所以，我们可以将最近被点赞的笔记，比如前 10 页，以 ZSET 存在 Redis 中，超过第 10 页的点赞笔记，则需要查询数据库获取。

## 笔记点赞流程设计

![img](https://img.quanxiaoha.com/quanxiaoha/172888339361855)

笔记点赞接口的处理流程如下：

> - 判断被点赞的笔记是否真实存在，若不存在，则返回提示信息：笔记不存在；
> - 判断笔记是否被点赞过，若已点赞，则返回提示信息：该篇笔记已点赞；
> - 判断用户的 ZSET 点赞列表是否存在，若存在，再判断 ZSET 的大小已经达到 100（每页10条，共10页的数据）：
>     - 若达到，则向 ZSET 中添加最新的点赞笔记，并将点赞时间最久的那篇，移除出 ZSET;
>     - 若未达到，则直接向 ZSET 中添加最新的点赞笔记；
> - 发送 MQ, 让消费者去将点赞记录存入 `t_note_like` 表中；

### 如何判断笔记是否已被点赞？

为了判断笔记是否已被点赞，有以下实现方案：

### **Redis Set 存储点赞记录**

- **实现原理**：可以为每个用户单独维护一个 `Set`，其中存储所有点赞笔记的 ID。在用户点赞时，首先查询这个 `Set` 是否已经包含目标笔记的 `noteId`。

- 操作步骤:

    > 1. 使用 Redis `SISMEMBER` 命令判断用户是否已经点赞过该笔记。
    > 2. 如果笔记没有点赞过，使用 `SADD` 命令将 `noteId` 添加到对应用户的 `Set` 中。
    > 3. 如果笔记已经点赞过，则返回相应的提示。

- **优点**：操作简单、查询速度快。

- **缺点**：`Set` 的存储开销较大。

`Set` 存储对内存占用较大，故此方案不做考虑。

### **Redis Bitmap**

- **实现原理**：Bitmap 是一种基于位操作的结构，可以用来高效存储某一集合内元素是否存在的信息。在这种场景下，可以为每位用户维护一个 Bitmap，每个位对应一篇笔记 ID, 标记笔记是否已经点赞过。

- 操作步骤

    > 1. 使用 `GETBIT` 命令判断指定位置的笔记是否已经点赞。
    > 2. 如果笔记没有点赞过，使用 `SETBIT` 命令将该笔记的位标记为 1，表示该笔记已经点赞。

- **优点**：内存占用非常小，查询效率高。

- **缺点**：需要设计合理的笔记 ID 和位图偏移策略，Bitmap 操作的颗粒度限制在二进制位，无法进行复杂的多维数据处理。

由于咱们的笔记 ID ，使用的是雪花算法生成，如下图所示，由于太长，Bitmap 存储不下，故此方案也不合适。

![img](https://img.quanxiaoha.com/quanxiaoha/172889293560150)

### **查询数据库**

- **实现原理**：将用户的点赞记录直接存储在数据库中。每当用户发起点赞请求时，查询数据库以确定该笔记是否已经点赞。

- 操作步骤

    > 1. 查询 `t_note_like` 表，检查是否存在该用户对目标笔记的点赞记录。
    > 2. 如果没有记录，插入新的点赞记录；如果已有记录，返回提示信息。

- **优点**：存储稳定，适合小规模的点赞操作。

- **缺点**：在高并发下，数据库压力较大，查询和写入性能较低。

数据库查询无法应对高并发，不做考虑。

### **Redis Bloom Filter** 布隆过滤器

- **实现原理**：由于 Redis `Set` 在存储大量用户数据时可能导致较高的内存消耗，使用 **布隆过滤器**（Bloom Filter）可以有效地减少存储空间。布隆过滤器可以用来快速判断一个用户是否可能已经点赞过该笔记，但可能会存在少量误报（false positive）。

- 操作步骤

    > 1. 为每个用户点赞列表初始化一个布隆过滤器，并在每次点赞笔记时，将笔记 ID 入到该布隆过滤器中；
    > 2. 使用 `Bloom Filter` 的 `BF.EXISTS` 命令判断笔记是否已经点赞过；
    > 3. 如果返回未点赞（不存在误报），则继续执行后续点赞逻辑；若返回已点赞（可能有误报），则需要进一步确认是否已点赞。

- **优点**：显著减少内存消耗，适合海量用户场景。

- **缺点**：对于已点赞笔记，存在一定的误判（对未点赞的笔记判断，则绝对正确）。


## 布隆过滤器误判问题

为了笔记能够正常被点赞，我们可以采用 **`Bloom 过滤器校验 + ZSet 校验 + 数据库校验`** 方案。

![img](https://img.quanxiaoha.com/quanxiaoha/172916117035736)

> 处理流程如下：
>
> - 当查询布隆过滤器，校验笔记是否被点赞时，若返回已点赞，由于存在误判，需要进一步校验；
> - 查询当前用户的 ZSET 笔记点赞列表（最多只有 100 条），是否包含目标笔记，若包含，说明目标笔记已经被点赞；
> - 若不存在，还需查询数据库，进一步确认笔记是否被点赞，若数据库中存在点赞记录，说明目标笔记已经被点赞；
> - 若数据库中也没有点赞记录，才能继续执行后续的点赞流程；



## 取消点赞

![img](https://img.quanxiaoha.com/quanxiaoha/172949974852719)

详细逻辑如下：

- 首先，判断想要取消点赞的笔记是否真实存在，若不存在，抛出业务异常，提示用户 “笔记不存在”；

- 判断笔记点赞布隆过滤器是否存在：

    - 若不存在，查询数据库，校验是否有点赞过目标笔记，若未点赞，则抛出业务异常，提示用户 “您未点赞该篇笔记，无法取消点赞”；并异步初始化布隆过滤器；

    - 若存在，通过布隆过滤器来校验目标笔记是否点赞：

        - 若返回未点赞，则判断绝对正确，抛出业务异常，提示用户对应提示信息；

        - 若返回已点赞，可能存在很小几率的误判的情况；

            > **误判是否能够容忍？**
            >
            > - 分析一波业务场景，大多数情况下，用户不会对刚刚点赞的笔记进行取消点赞，反而是以前点赞的笔记，没有价值了，进行了取消点赞。
            > - 另一方面，ZSET 只会缓存最新点赞的部分笔记，而为了校验这些小几率事件，当 ZSET 中不存在时，就不得不查数据库来校验，这就导致大部分流量都会打到数据库，导致数据库压力太大，反而得不偿失了！
            > - **相比较笔记点赞的场景，误判会影响用户正常的操作，必须得校验，这里的误判是可以容忍的！只需要在 MQ 异步数据落库的时候，再次校验一下即可，那么，接口中就无需操作数据库，保证取消点赞接口支持高并发写。**

- 若笔记已点赞，删除 `ZSET` 笔记点赞列表中对应的笔记 ID;

- 发送 MQ, 异步对数据进行更新落库；

# BUG

---

如果子项目构建失败，可以先尝试构建父项目，让 Maven 首先需要它来理解 `xiaoha-common` 的项目结构和潜在依赖，然后在构建子项目

---



这个错误信息 "Accessing invalid virtual file: file://D:/project/new/xiaohashu/xiaohashu-distributed-id-generator/xiaohashu-distributed-id-generator-api; original:678698; found:679127; File.exists()=true" 通常与 **IDE (集成开发环境，如 IntelliJ IDEA, Eclipse) 或构建工具的文件系统缓存与实际文件系统状态不一致** 有关。

让我们分解这个错误信息的各个部分：

- **`Accessing invalid virtual file`**:
    - "Virtual file" (虚拟文件) 是 IDE 或某些工具用来在内存中表示文件系统中的文件或目录的一种抽象概念。这有助于提高性能，避免频繁地直接访问物理磁盘。
    - "Invalid" (无效的) 表明 IDE 或工具认为它当前持有的关于这个虚拟文件的信息（元数据、时间戳、内容等）与实际文件系统中的状态不匹配或已过期。
- **`file://D:/project/new/xiaohashu/xiaohashu-distributed-id-generator/xiaohashu-distributed-id-generator-api`**:
    - 这是指向出问题的具体文件或目录的路径。在这个例子中，它指向你的项目中的一个 API 模块目录。
- **`original:678698; found:679127`**:
    - 这很可能是某种内部时间戳或版本号的比较。
        - `original`: IDE 虚拟文件系统中记录的该文件的“原始”时间戳或版本。
        - `found`: IDE 在尝试访问或同步时从实际文件系统中检测到的“新”时间戳或版本。
    - 因为 `found` (679127) 与 `original` (678698) 不同，IDE 认为它缓存的虚拟文件信息已经过时或不正确。
- **`File.exists()=true`**:
    - 这部分确认了尽管虚拟文件被标记为“无效”，但实际物理文件或目录在文件系统上是**存在**的。这意味着问题不在于文件丢失，而在于 IDE 内部状态与外部文件系统状态的同步问题。

**这个错误通常意味着什么？**

1. **文件系统同步问题**：IDE 可能未能正确地检测到或处理文件系统中的更改。这可能发生在以下情况：
    - 文件在 IDE 外部被修改 (例如，通过命令行 git 操作、在另一个编辑器中修改、构建脚本生成或修改文件)。
    - 文件系统事件通知未能及时或正确地传递给 IDE。
    - 网络驱动器或虚拟化环境中的文件系统延迟。
2. **缓存不一致**：IDE 内部的缓存（包括虚拟文件系统、索引等）可能由于某种原因损坏或变得与实际情况不一致。
3. **IDE Bug 或插件冲突**：在某些情况下，这可能是 IDE 本身的 bug，或者是某个已安装插件与 IDE 的文件系统交互方式存在冲突。
4. **构建过程的副作用**：某些构建过程或工具可能会以一种 IDE 难以跟踪的方式修改文件，导致这种不一致。
5. **时间戳问题**：如果系统时钟发生大的跳变，或者文件的时间戳被异常修改，也可能导致这类问题。

**如何尝试解决这个问题？**

1. **刷新/同步项目**：
    - 在 IntelliJ IDEA 中：`File -> Synchronize` 或点击工具栏上的同步按钮。
    - 在 Eclipse 中：右键点击项目 -> `Refresh`。
2. **使缓存无效并重启 (Invalidate Caches / Restart)**：
    - 这是 IDE 中解决此类问题的常用方法。它会清除 IDE 的缓存并重建索引。
    - 在 IntelliJ IDEA 中：`File -> Invalidate Caches / Restart...` -> 选择 "Invalidate and Restart"。
3. **重启 IDE**：有时简单地重启 IDE 就能解决临时的同步问题。
4. **检查文件权限和磁盘空间**：虽然 `File.exists()=true` 表明文件存在，但确保 IDE 对该路径有足够的读写权限，并且磁盘空间充足。
5. **检查外部工具或脚本**：如果你有在项目目录中运行的外部工具、脚本或文件监视器，确保它们不会与 IDE 的文件操作产生冲突。
6. **更新 IDE 和插件**：确保你的 IDE 和所有插件都更新到最新版本，这可能修复已知的 bug。
7. **检查版本控制**：如果你在使用 Git 等版本控制系统，确保你的工作目录是干净的，没有未解决的冲突或异常状态。尝试从版本控制系统更新项目。
8. **删除 IDE 的特定项目配置**：
    - 对于 IntelliJ IDEA，可以尝试关闭项目，然后删除项目根目录下的 `.idea` 文件夹 (请注意，这会删除你的项目特定配置，如运行配置、模块设置等，IDE 会在下次打开时重新生成)。
    - 对于 Eclipse，是 `.project` 和 `.classpath` 文件以及 `.settings` 目录，但删除这些文件风险较高，通常不作为首选。

如果上述方法都不能解决问题，你可能需要查看 IDE 的日志文件以获取更详细的错误信息，或者考虑是否是特定插件引起的问题（可以尝试禁用最近安装或更新的插件）。



---





![image-20250601104545441](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250601104545441.png)

![image-20250601164948114](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250601164948114.png)



---

![image-20250602171006464](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250602171006464.png)

大小写问题



---



构造方法里尽量不要有参数，不知道什么时候实体类会增加字段



---

![image-20250603175714819](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250603175714819.png)

![image-20250603175829137](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250603175829137.png)

---

![image-20250604205633758](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250604205633758.png)

数据库里的字段没有与 Java 实体类里的字段对应上



---



![image-20250619155454274](https://map-bed-lbwxxc.oss-cn-beijing.aliyuncs.com/imgimage-20250619155454274.png)

类型 BUG
