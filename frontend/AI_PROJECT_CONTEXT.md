# AI_PROJECT_CONTEXT（前端仓库入口）

本文件用于让 AI/Codex/Cursor 在每次任务开始时快速理解前端项目结构与关键约定。

## 关键约定（必须遵守）
- Axios 响应拦截器：`code===200 -> return data`；`code===40001 -> 弹窗并下载报告`；其它业务码 `ElMessage.error + reject`；HTTP 401/403/404/500 按状态提示。
- 动态路由：由 `permissionStore.generateRoutes()` 拉取菜单树并注入到 `name: Layout` 下；logout 必须 `resetRoutes()` removeRoute。
- 开发环境 API 前缀：`VITE_APP_BASE_API=/api`

## 关键文件索引
- `src/api/index.ts`：axios 实例、拦截器、401 无感刷新（单飞+队列）
- `src/stores/auth/user.ts`：token/refreshToken 存储与 logout
- `src/stores/auth/permission.ts`：getRouters → filterAsyncRoutes → addRoute('Layout', ...)
- `src/router/permission.ts`：路由守卫（首次登录加载动态路由）
- `src/layout/components/Sidebar/`：菜单渲染（SidebarItem）

## 常见业务问题的定位路径
- 登录后页面 401：先看 axios 401 刷新逻辑是否触发 refresh；refresh 成功后重放是否正确
- 菜单能看到但点不进去：看 ParentView/路径拼接与动态路由注入点
- 多账号切换越权：看 logout 是否清 token+refreshToken+resetRoutes
