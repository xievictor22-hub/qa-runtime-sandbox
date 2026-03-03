<template>
  <div class="h-[50px] bg-white shadow-sm flex items-center justify-between px-4">
    <div class="flex items-center">
      <el-icon 
        class="cursor-pointer text-xl mr-4 hover:text-blue-500 transition-colors"
        @click="appStore.toggleSidebar"
      >
        <component :is="appStore.sidebarOpened ? Fold : Expand" />
      </el-icon>
      <Breadcrumb />
    </div>

    <div class="flex items-center h-full">
      <el-dropdown trigger="click" class="mr-2 cursor-pointer h-full flex items-center px-2 hover:bg-gray-100 transition-colors">
        <span class="el-dropdown-link flex items-center" title="切换主题">
          <el-icon :size="18"><Brush /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item 
              v-for="item in themeList" 
              :key="item.id" 
              @click="setTheme(item.id)"
            >
              <div class="flex items-center border border-gray-200 rounded overflow-hidden mr-2">
                <span :style="{ background: item.menuBg }" class="w-2 h-4 inline-block"></span>
                <span :style="{ background: item.primary }" class="w-2 h-4 inline-block"></span>
              </div>
              {{ item.name }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <Screenfull class="mr-4 hover:bg-gray-100 transition-colors h-full px-2" />

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="flex items-center cursor-pointer">
          <el-avatar :size="30" :src="userStore.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
          <span class="ml-2 text-sm text-gray-700 select-none">{{ userStore.nickname || 'Admin' }}</span>
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/system/app'
import { useUserStore } from '@/stores/auth/user'
import { useRouter } from 'vue-router'
// 1. 引入新增加的图标 Brush
import { Expand, Fold, ArrowDown, Brush } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb/index.vue'

// 2. 引入新增加的组件和 Hook
import Screenfull from '@/components/Screenfull/index.vue'
import { useTheme } from '@/hooks/useTheme'

const appStore = useAppStore()
const userStore = useUserStore()
const router = useRouter()

// 3. 初始化主题 Hook
const { themeList, setTheme } = useTheme()



const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    }).then(() => {
      userStore.logout()
      location.reload()
      ElMessage.success('已退出登录')
    })
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}
</script>