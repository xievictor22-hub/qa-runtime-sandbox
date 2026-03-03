<template>
  <div class="h-[34px] w-full bg-white border-b border-gray-200 shadow-sm flex items-center px-4">
    <el-scrollbar class="w-full">
      <div class="flex items-center h-[34px]">
        <router-link
          v-for="tag in tagsViewStore.visitedViews"
          :key="tag.path"
          :to="{ path: tag.path, query: tag.query }"
          class="tags-view-item"
          :class="{ active: isActive(tag) }"
        >
          {{ tag.title }}
          <el-icon 
            v-if="tag.path !== '/dashboard'" 
            class="ml-2 hover:bg-gray-300 rounded-full p-0.5 transition-colors"
            @click.prevent.stop="closeSelectedTag(tag)"
          >
            <Close />
          </el-icon>
        </router-link>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTagsViewStore } from '@/stores/system/tagsView'
import { Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

const isActive = (tag: any) => {
  return tag.path === route.path
}

const addTags = () => {
  if (route.name) {
    tagsViewStore.addView(route)
  }
}

const closeSelectedTag = (view: any) => {
  tagsViewStore.delView(view).then((visitedViews: any) => {
    if (isActive(view)) {
      // 如果关闭的是当前显示的页面，则跳转到最后一个标签，否则跳到首页
      const latestView = visitedViews.slice(-1)[0]
      if (latestView) {
        router.push(latestView.path)
      } else {
        router.push('/')
      }
    }
  })
}

// 监听路由变化，自动添加标签
watch(() => route.path, () => {
  addTags()
})

onMounted(() => {
  addTags()
})
</script>

<style scoped>
.tags-view-item {
  @apply inline-flex items-center px-3 py-1 text-xs text-gray-600 bg-white border border-gray-200 rounded-sm mx-1 cursor-pointer transition-all decoration-auto;
  text-decoration: none; /* 强制去除下划线 */
}

.tags-view-item.active {
  @apply bg-[#42b983] text-white border-[#42b983];
}

.tags-view-item:hover {
  @apply text-[#42b983];
}
.tags-view-item.active:hover {
  @apply text-white;
}
</style>