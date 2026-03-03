# 项目架构概览（后端仓库）

## 技术栈
- Spring Boot 3.x + Spring Security
- Java 17
- JWT（accessToken / refreshToken）+ Redis 会话（`login_tokens:{uuid}`）
- MyBatis（系统菜单/权限等）

## 运行与端口
- 应用端口：以 `application.yml` 为准（你的项目中常见为 29020）
- Redis：用于会话与在线用户/踢人下线等能力

## 关键模块（建议入口）
- `modules/auth/**`：登录、token、refresh、过滤器
  - `TokenService`：签发/解析 JWT、Redis 会话写入
  - `JwtAuthenticationTokenFilter`：解析 token → 从 Redis 取 LoginUser → 注入 SecurityContext
- `modules/system/**`：菜单、权限、用户等
  - `SysMenuMapper`：权限与菜单树查询
- 在线用户/踢人下线：基于 Redis key 管理（如果已有对应 controller/service）

## 常见问题定位
- refresh 失败：确认 refresh endpoint 不依赖 access token（只依赖 refreshToken）
- token 过期判断异常：检查 JWT 的 `iat/exp` 时间与时钟来源是否正确
- 踢人下线不生效：确认删除了 `login_tokens:{uuid}` 并同步清理用户名索引
