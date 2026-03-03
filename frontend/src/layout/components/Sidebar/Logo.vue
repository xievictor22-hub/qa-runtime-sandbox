<template>
  <div
    class="sidebar-logo-container relative w-full h-[50px] leading-[50px] bg-[#001529] text-center overflow-hidden transition-all duration-300"
    :class="{ 'collapse': collapse }"
  >
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="h-full w-full flex items-center justify-center" to="/">
        <img v-if="logo" :src="logo" class="w-8 h-8 align-middle" />
        <h1 v-else class="text-white font-bold text-sm">Mogo</h1>
      </router-link>

      
      <router-link v-else key="expand" class="h-full w-full flex items-center justify-center text-none" to="/">
        <img src="@/assets/logo.png" alt="Logo" class="" v-if="collapse"/>
        <img src="@/assets/logo_ex.png" alt="Logo" class="" v-else/>
      </router-link>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import img from '@/assets/logo.png'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const title = ref('Mogo Admin')
const logo = ref(img) // 替换成你自己的 Logo 路径
</script>

<style scoped>
/* 定义淡入淡出动画 */
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter-from,
.sidebarLogoFade-leave-to {
  opacity: 0;
}
</style>