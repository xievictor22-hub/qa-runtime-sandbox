<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span v-if="item.redirect === 'noRedirect' || index == levelList.length - 1" class="no-redirect">
          {{ item.meta.title }}
        </span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter, type RouteLocationMatched } from 'vue-router'

const route = useRoute()
const router = useRouter()
const levelList = ref<RouteLocationMatched[]>([])

const getBreadcrumb = () => {
  // 过滤出这就带有 title 的路由
  let matched = route.matched.filter(item => item.meta && item.meta.title)
  
  const first = matched[0]
  // 如果第一个不是首页，加一个首页进去
  if (!isDashboard(first)) {
    matched = [{ path: '/dashboard', meta: { title: '首页' } } as any].concat(matched)
  }

  levelList.value = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
}

const isDashboard = (route: RouteLocationMatched) => {
  const name = route && route.name
  if (!name) return false
  return name.toString().trim().toLocaleLowerCase() === 'Dashboard'.toLocaleLowerCase()
}

const handleLink = (item: any) => {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect)
    return
  }
  router.push(path)
}

watch(
  () => route.path,
  () => getBreadcrumb()
)

onMounted(() => {
  getBreadcrumb()
})
</script>

<style scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;
}

.no-redirect {
  color: #97a8be;
  cursor: text;
}
</style>