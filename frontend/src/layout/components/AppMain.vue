<template>
  <section class="app-main h-[calc(100vh-84px)] w-full relative overflow-hidden bg-gray-50">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive :include="tagsViewStore.cachedViews">
          <component :is="Component" :key="route.path" />
        </keep-alive>
      </transition>
    </router-view>
  </section>
</template>

<style scoped>
/* 简单的淡入淡出动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
<script setup lang="ts">
import { useTagsViewStore } from '@/stores/system/tagsView'

const tagsViewStore = useTagsViewStore()
</script>
