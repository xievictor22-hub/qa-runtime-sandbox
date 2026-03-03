import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTagsViewStore = defineStore('tagsView', () => {
  // 访问过的视图 (用于渲染标签)
  const visitedViews = ref<any[]>([])
  // 缓存的视图 (用于 KeepAlive，需要存组件 name)
  const cachedViews = ref<string[]>([])

  // 添加视图
  function addView(view: any) {
    // 1. 添加 visitedViews
    if (!visitedViews.value.some(v => v.path === view.path)) {
      visitedViews.value.push(Object.assign({}, view, {
        title: view.meta.title || 'no-name'
      }))
    }
    
    // 2. 添加 cachedViews (如果 meta.noCache 不为 true)
    if (!view.meta.noCache) {
      if (cachedViews.value.includes(view.name)) return
      if (view.name) cachedViews.value.push(view.name)
    }
  }

  // 删除视图
  function delView(view: any) {
    return new Promise(resolve => {
      // 删 visited
      const i = visitedViews.value.findIndex(v => v.path === view.path)
      if (i > -1) visitedViews.value.splice(i, 1)
      
      // 删 cached
      const index = cachedViews.value.indexOf(view.name)
      if (index > -1) cachedViews.value.splice(index, 1)
      
      resolve([...visitedViews.value])
    })
  }

  return { visitedViews, cachedViews, addView, delView }
}, {
  persist: true // 记得开启持久化，刷新页面标签还在
})