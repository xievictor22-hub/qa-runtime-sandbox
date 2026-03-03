import router from "@/router";
import { useUserStore } from "@/stores/auth/user";
import { usePermissionStore } from "@/stores/auth/permission"; // 新增
import NProgress from "nprogress";
import "nprogress/nprogress.css";
//路由守卫

NProgress.configure({ showSpinner: false });
const whiteList = ["/login"];

router.beforeEach(async (to, from, next) => {
  NProgress.start();
  const userStore = useUserStore();
  const permissionStore = usePermissionStore(); // 新增
  if(!userStore.token) {
     if (whiteList.includes(to.path)) return next()
    return next(`/login?redirect=${to.fullPath}`)
  }
    if (to.path === "/login") {
      NProgress.done();
      return next({ path: "/" });;
    }
    // 判断是否已经加载过路由 (用 permissionStore.routes 的长度判断，或者用 roles 判断)
    
      if (!permissionStore.isRoutesLoaded) {
        try {
        // 1. 获取用户信息 (如果还没获取)
        // await userStore.getInfo()
        // 2. 生成动态路由
        await permissionStore.generateRoutes();
        // 4. 确保添加完路由后再跳转 (hack 方法)
        // replace: true 确保不会留下历史记录
        return  next({ ...to, replace: true });;
        }catch (error) {
          // 出错需重置 token 并跳转登录页
          userStore.logout();
          NProgress.done();
          return;
        }
      }
      NProgress.done()
  return next()
});

router.afterEach(() => {
  NProgress.done();
});
