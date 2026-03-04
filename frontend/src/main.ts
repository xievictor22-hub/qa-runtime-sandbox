import { createApp } from 'vue'

import App from './App.vue'

// 1. 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// --- 引入 FormCreate ---
import formCreate from '@form-create/element-ui'
import FcDesigner from '@form-create/designer'

// 1. 引入 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'


// 2. 引入 Tailwind CSS (创建 assets/main.css 后引入)
import './assets/main.css'
// 4. 引入权限管理模块
import './permission'
// 3. 引入 Pinia
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 4. 引入 Router (暂时创建一个简单的空路由，避免报错)
import router from './router' // 1. 引入路由

// 5. 引入自定义指令
import { hasPerm } from './directive/permission'

const app = createApp(App)

// 6. 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

/**
 * Edge 浏览器在窗口最小化时
 * vue-router beforeUnloadListener 会触发 history.replaceState，
 * 导致窗口异常恢复/最大化。
 *
 * 仅在 hidden 状态下阻止该调用。
 */
(function fixEdgeMinimizeBug() {
  const rawReplaceState = history.replaceState

  history.replaceState = function (...args: any[]) {
    if (document.visibilityState === 'hidden') {
      const stack = new Error().stack || ''
      if (
        stack.includes('beforeUnloadListener') &&
        stack.includes('vue-router')
      ) {
        return
      }
    }

    return rawReplaceState.apply(this, args as any)
  }
})()


const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
// 注册 FormCreate
app.use(formCreate)
// 注册设计器
app.use(FcDesigner)
// 全局注册 DictTag
import DictTag from '@/components/DictTag/index.vue'
import { reportClientError } from './api/monitor/client-error'
app.component('DictTag', DictTag)
app.use(pinia)
app.use(router)
app.use(ElementPlus)
// 全局错误处理
// 捕获 Vue 实例内部的错误
app.config.errorHandler = (err, instance, info) => {
  console.error('[VueError]', err, info)
  void reportClientError({ type: 'vue', message: String(err), info })
}
// 全局 JS 错误处理
window.addEventListener('error', (event) => {
  if (String(event.filename || '').includes('/api/monitor/client-error')) return
  void reportClientError({ type: 'js', message: event.message, stack: event.error?.stack })
})
// 全局 Promise 错误处理
window.addEventListener('unhandledrejection', (event) => {
  const reason: any = event.reason
  const reqUrl = reason?.config?.url || ''
  if (String(reqUrl).includes('/monitor/client-error/report')) return

  void reportClientError({
    type: 'promise',
    message: String(reason?.message || reason),
    stack: reason?.stack,
    extra: {
      name: reason?.name,
      url: reqUrl,
      status: reason?.response?.status
    }
  })
})
// 6. 注册自定义的按钮权限指令,字段名称与v-permission保持一致
app.directive('permission', hasPerm)

app.mount('#app')
