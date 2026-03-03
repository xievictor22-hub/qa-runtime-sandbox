# 后端运行手册

## 本地启动
1. Java：17
2. Redis：启动并配置连接
3. 启动 Spring Boot：IDE 运行或 `mvn spring-boot:run`
4. 确认端口与前端代理一致（如 29020）

## 常见排障
- 401：检查 Authorization 头、JWT secret、Redis 是否有 `login_tokens:{uuid}`
- refresh 无法调用：确认 refresh endpoint 允许匿名访问（或仅校验 refreshToken）
- 权限不更新：检查 LoginUser permissions 更新逻辑与 Redis 写回
