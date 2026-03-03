import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore(
  'app',
  () => {
    // 侧边栏折叠状态：false-展开, true-折叠
    const sidebarOpened = ref(true)

    // 切换侧边栏
    function toggleSidebar() {
      sidebarOpened.value = !sidebarOpened.value
    }

    // 关闭侧边栏
    function closeSidebar() {
      sidebarOpened.value = false
    }

    return { sidebarOpened, toggleSidebar, closeSidebar }
  },
  {
    persist: {
      storage: localStorage,
      paths: ['sidebarOpened'] // 只持久化折叠状态
    }
  }
)