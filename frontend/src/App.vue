<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ElConfigProvider } from 'element-plus'
// 引入 Element Plus 中文语言包
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<style>
/* 这里通常不需要写太多样式，全局样式建议放在 src/assets/main.css 或 tailwind 中 */
#app {
  height: 100%;
  width: 100%;
}
</style>