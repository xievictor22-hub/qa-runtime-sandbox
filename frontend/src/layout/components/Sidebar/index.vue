<template>
  <div 
    class="h-screen text-white transition-all duration-300 flex flex-col"
    :style="{ backgroundColor: currentTheme.menuBg }"
    :class="appStore.sidebarOpened ? 'w-[64px]' : 'w-[200px]'"
  >
    <div class="h-[50px] flex items-center justify-center bg-[#2b2f3a] overflow-hidden">
      <img src="@/assets/logo.png" alt="Logo" class="" v-if="appStore.sidebarOpened"/>
      <img src="@/assets/logo_ex.png" alt="Logo" class="" v-if="!appStore.sidebarOpened"/>
       <span v-if="!appStore.sidebarOpened" class="ml-3 font-bold text-sm truncate">MoGo Admin</span> 
    </div>

    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarOpened"
        :background-color="currentTheme.menuBg"
        :text-color="currentTheme.menuText"
        :active-text-color="currentTheme.menuActiveText"
        :collapse-transition="false"
        router
        class="border-none"
      >
        <SidebarItem 
          v-for="route in permissionStore.routes" 
          :key="route.path" 
          :item="route" 
          :base-path="route.path" 
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/system/app'
import { usePermissionStore } from '@/stores/auth/permission' // 新增
import SidebarItem from './SidebarItem.vue' // 新增组件
import { useTheme } from '@/hooks/useTheme' // 引入 Hook

const route = useRoute()
const appStore = useAppStore()
const permissionStore = usePermissionStore() // 获取路由数据
// 引入主题
const { currentTheme } = useTheme()

const activeMenu = computed(() => route.path)


</script>

<style scoped>
/* 去掉 Element Menu 右侧默认边框 */
:deep(.el-menu) {
  border-right: none;
}
</style>