import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 定义静态路由 (无需权限即可访问的页面，如登录、404) 
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'), 
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'), // 仪表盘页面
        meta: { title: '首页', icon: 'HomeFilled' }
      },
       {
        path: 'user/profile', // 注意：前端访问路径 /user/profile
        name: 'UserProfile',
        component: () => import('@/views/system/user/profile/index.vue'),
        meta: { title: '个人中心', hidden: true } // hidden: true 侧边栏不显示
      }
    ]
  },
  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error/404.vue'), 
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
  scrollBehavior: () => ({ left: 0, top: 0 })
})

export default router