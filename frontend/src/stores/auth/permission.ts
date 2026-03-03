import { defineStore } from 'pinia'
import { h, ref } from 'vue'
import router, { constantRoutes } from '@/router'
import { getRouters } from '@/api/system/menu'
import Layout from '@/layout/index.vue'
import { RouterView } from 'vue-router'


const ParentView = {
  name: 'ParentView',
  render: () => h(RouterView),
}
// 匹配 views 里面所有的 .vue 文件
const modules = import.meta.glob('../../views/**/*.vue')
export const usePermissionStore = defineStore('permission', () => {
  const isRoutesLoaded = ref(false)
  const routes = ref<any[]>([])
  const addRoutes = ref<any[]>([])
  const addedRouteNames = ref<string[]>([])//记录已添加的路由名称方便删除


  // 生成路由 action
  function generateRoutes() {
    return new Promise<any[]>((resolve, reject) => {
      getRouters().then((res: any) => {
        // 后端返回的是平铺的树或嵌套树，根据你后端 buildTree 的逻辑
        // 假设后端返回的是标准树形结构 children
        
        const sdata = JSON.parse(JSON.stringify(res)) // 深拷贝
        const rdata = JSON.parse(JSON.stringify(res))
        
        const sidebarRoutes = filterAsyncRoutes(sdata)  //获取侧边栏菜单树
        const rewriteRoutes = filterAsyncRoutes(rdata, true) //获取动态路由
        resetRoutes()//清空再添加
        const normalized:any[] = []

        rewriteRoutes.forEach((r: any) => {
          const isLayoutWrapper =
            r.path === '/' ||
            r.name === 'Layout' ||
            r.component === Layout || // filterAsyncRoutes 已经把 Layout 字符串替换成组件对象
            r.component?.name === 'Layout'

          if (isLayoutWrapper && Array.isArray(r.children) && r.children.length > 0) {
            normalized.push(...r.children)
          } else {
            normalized.push(r)
          }
        })
        // ✅ 2) 只把真正的业务路由挂到现有 Layout 下
        normalized.forEach((r: any) => {
          if (!r.name) {
            r.name = `dyn_${String(r.path).replace(/[^\w]/g, '_')}`
          }
          router.addRoute('Layout', r)
          addedRouteNames.value.push(String(r.name))
        })
        addRoutes.value = rewriteRoutes
        routes.value = constantRoutes.concat(sidebarRoutes) //初始路由 + 侧边栏菜单树
        isRoutesLoaded.value = true
        console.log(routes.value)
        resolve(rewriteRoutes)
      }).catch(error => {
        isRoutesLoaded.value = false
        reject(error)
      })
    })
  }

  function resetRoutes() {
    for (const name of addedRouteNames.value) {
      if (router.hasRoute(name)) {
        router.removeRoute(name)
      }
    }
    addedRouteNames.value = []
    routes.value = [...constantRoutes]
    addRoutes.value = []
    isRoutesLoaded.value = false
  }

  return { routes, addRoutes, isRoutesLoaded, generateRoutes, resetRoutes }
})

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRoutes(routes: any[], isRewrite = false) {
  return routes.filter((route: any) => {
    
    if (isRewrite && route.children) {
      route.children = filterChildren(route.children)
    }

    // 2. 重点修改这里：使用 component 路径来生成 Name (比 path 更准)
    if (!route.name) {
       if (route.component) {
         // 例如 component="system/user/index" -> name="SystemUserIndex"
          route.name = toCamelCase(route.component)
       } else {
         route.name = toCamelCase(route.path)
       }
    }
    
    // 1. 处理 Layout 组件 (目录)
    if (route.component === 'Layout' ) {
      route.component = Layout
    }else if( !route.component){
      route.component = ParentView
    } else {
      // 2. 处理实际页面组件
      // 这里的逻辑是将 'system/user/index' -> () => import('@/views/system/user/index.vue')
      const componentPath = `../../views/${route.component}.vue`
      if (modules[componentPath]) {
        route.component = modules[componentPath]
      } else {
        // 找不到文件则给个 404 占位，防止报错
        route.component = () => import('@/views/error/404.vue')
      }
    }
    

    // 3. 处理 path (如果是根目录的子菜单，需要处理 /)
    if (route.path.startsWith('/') && !isRewrite) {
       // 保持原样
      
    } 
   
    // 4. 处理 meta
    route.meta = {
      title: route.menuName,
      icon: route.icon,
      noCache: false,
      hidden: route.visible === 0,
    }
    
    // 5. 递归处理子菜单
    if (route.children && route.children.length) {
      route.children = filterAsyncRoutes(route.children, isRewrite)
    }
    return true
  })
}

function filterChildren(childrenMap: any[]) {
  // 这里可以处理一些特殊的子路由重写逻辑，目前保持简单
  return childrenMap
}

// 1. 修改辅助函数：根据 "system/user/index" -> "SystemUserIndex"
function toCamelCase(str: string) {
  if (!str || typeof str !== 'string') {
    return ''
  }
  // 移除开头的 /
  str = str.replace(/^[\/]/, '') 
  // 移除 .vue 后缀 (如果有)
  str = str.replace(/\.vue$/, '') 
  // 将 / 或 - 转为大驼峰
  str = str.replace(/(?:^|[\/\-])(\w)/g, (_, c) => c ? c.toUpperCase() : '')
  return str
}