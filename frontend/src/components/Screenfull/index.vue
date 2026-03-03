<template>
  <div @click="toggle" class="cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700 px-3 h-full flex items-center">
    <el-icon :size="20">
      <FullScreen v-if="!isFullscreen" />
      <Aim v-else />
    </el-icon>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import screenfull from 'screenfull'
import { ElMessage } from 'element-plus'
import { FullScreen, Aim } from '@element-plus/icons-vue'

const isFullscreen = ref(false)

const change = () => {
  isFullscreen.value = screenfull.isFullscreen
}

const toggle = () => {
  if (!screenfull.isEnabled) {
    ElMessage.warning('您的浏览器不支持全屏')
    return
  }
  screenfull.toggle()
}

onMounted(() => {
  if (screenfull.isEnabled) {
    screenfull.on('change', change)
  }
})

onUnmounted(() => {
  if (screenfull.isEnabled) {
    screenfull.off('change', change)
  }
})
</script>