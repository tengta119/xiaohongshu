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







# BUG

如果子项目构建失败，可以先尝试构建父项目，让 Maven 首先需要它来理解 `xiaoha-common` 的项目结构和潜在依赖，然后在构建子项目



