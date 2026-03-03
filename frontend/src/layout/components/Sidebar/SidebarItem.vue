<template>
  <div v-if="!item.meta || !item.meta.hidden">
    <template v-if="hasOneShowingChild(item.children, item) && (!onlyOneChild.children || onlyOneChild.noShowingChildren) && !item.alwaysShow">
      <el-menu-item :index="resolvePath(onlyOneChild.path)" v-if="onlyOneChild.meta">
        <el-icon v-if="onlyOneChild.meta.icon">
          <component :is="onlyOneChild.meta.icon" />
        </el-icon>
        <template #title>
          <span class="ml-2">{{ onlyOneChild.meta.title }}</span>
        </template>
      </el-menu-item>
    </template>

    <el-sub-menu v-else :index="resolvePath(item.path)" teleported :class="{ 'hide-arrow': sidebarOpened }">
      <template #title v-if="item.meta">
        <el-icon v-if="item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span v-if="!sidebarOpened" class="ml-2">{{ item.meta.title }}</span>
      </template>
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
      />
    </el-sub-menu>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// @ts-ignore
import path from 'path-browserify'
import { useAppStore } from '@/stores/system/app'

const {sidebarOpened} = useAppStore()

const props = defineProps({
  item: { type: Object, required: true },
  isNest: { type: Boolean, default: false },
  basePath: { type: String, default: '' }
})

const onlyOneChild = ref<any>(null)

// 检查是否只有一个子项需要展示
function hasOneShowingChild(children: any[] = [], parent: any) {
  const showingChildren = children.filter((item: any) => {
    if (item.hidden || (item.meta && item.meta.hidden)) {
      return false
    }
    return true
  })

  if (showingChildren.length === 1) {
    onlyOneChild.value = showingChildren[0]
    return true
  }

  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    return true
  }

  return false
}

function resolvePath(routePath: string) {
  // // 简单的路径拼接，实际生产可能需要 path-browserify 库处理绝对路径
  // if (routePath.startsWith('http')) return routePath
  // if (props.basePath === '/') return '/' + routePath
  // // 处理本身就是绝对路径的情况
  // if (routePath.startsWith('/')) return routePath
  
  // return props.basePath + (props.basePath.endsWith('/') ? '' : '/') + routePath

  //http需要改成https
  if (/^(http?:|mailto:|tel:)/.test(routePath)) {
    return routePath
  }
  if (/^(http?:|mailto:|tel:)/.test(props.basePath)) {
    return props.basePath
  }
  return path.resolve(props.basePath, routePath)
}
</script>

<style scoped>
/* 核心修复：强制隐藏折叠状态下的子菜单文字和箭头 */
/* 这一步非常重要，因为 Element Plus 的默认行为有时会被 Tailwind 样式覆盖 */
:deep(.el-menu--collapse .el-sub-menu__title span) {
  display: none;
}
:deep(.el-menu--collapse .el-sub-menu__icon-arrow) {
  display: none !important;
}
/* 隐藏箭头的自定义类 */
:deep(.hide-arrow .el-sub-menu__icon-arrow) {
  display: none !important;
}
</style>