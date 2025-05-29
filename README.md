# 七、Gateway网关搭建与接口鉴权



## 鉴权

当用户发送请求时，sa 会 通过 StpInterface 从 redis 获取用户对应的角色、权限

```java
 .setError(e -> {
                    // return SaResult.error(e.getMessage());
                    // 手动抛出异常，抛给全局异常处理器
                    if (e instanceof NotLoginException) { // 未登录异常
                        throw new NotLoginException(e.getMessage(), null, null);
                    } else if (e instanceof NotPermissionException || e instanceof NotRoleException) { // 权限不足，或不具备角色，统一抛出权限不足异常
                        throw new NotPermissionException(e.getMessage());
                    } else { // 其他异常，则抛出一个运行时异常
                        throw new RuntimeException(e.getMessage());
                    }
                })
```

当不设置这段代码时，会统一抛出 `SaTokenException` 移除，再由 handle 捕获该异常，为了能达到更细腻的返回信息，应设置上述代码，抛出更细腻的异常，再由 handle 捕获

