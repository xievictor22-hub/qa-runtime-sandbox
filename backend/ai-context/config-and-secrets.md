# 配置与敏感信息（后端）

## application.yml / application-*.yml
请避免提交明文：
- 数据库用户名/密码
- Redis 密码
- JWT secret
- MinIO / 钉钉 / 邮件等第三方密钥

## 建议做法
- 使用环境变量占位：`${ENV_VAR_NAME:default}`
- 在仓库提供 `application.example.yml`（不含密钥）与部署文档
