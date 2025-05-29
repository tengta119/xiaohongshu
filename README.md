# 七、Gateway网关搭建与接口鉴权



## 鉴权

当用户发送请求时，sa 会 通过 StpInterface 从 redis 获取用户对应的角色、权限

