# 双 Token（后端）

## 推荐形态（企业常用）
- accessToken：JWT（短期，如 15~30min），用于请求鉴权
- refreshToken：JWT 或随机串（长期，如 7d），用于换取新的 accessToken
- Redis 会话：`login_tokens:{uuid}` 保存 LoginUser 与权限信息（支持在线用户/踢人下线）

## refresh 端点
- `/auth/refresh` 应仅依赖 refreshToken，不依赖 accessToken
- refresh 成功返回新的 accessToken（是否返回新的 refreshToken取决于是否做 rotation）

## 过滤器链路（常见）
- 解析 Authorization Bearer token
- 从 JWT claim 取 uuid（login_user_key）
- 查 Redis `login_tokens:{uuid}` 得到 LoginUser，注入 SecurityContext
- accessToken 过期：返回 401（前端触发 refresh 流程）

## 安全与运维建议
- refreshToken 可撤销（Redis 存 refresh 关联）
- 支持按 uuid 踢下线：删除 `login_tokens:{uuid}`（并同步清理索引）
- 生产环境 secret 与密码使用环境变量，不写入 yml
