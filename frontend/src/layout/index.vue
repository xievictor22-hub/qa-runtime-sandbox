<template>
  <div class="flex h-screen w-full overflow-hidden">
    <Sidebar class="h-full shadow-lg z-10 flex-shrink-0" />

    <div class="main-container flex-1 flex flex-col min-w-0 bg-gray-50 transition-all duration-300">
      <Navbar />
      <TagsView /> 
      <AppMain />
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './components/Sidebar/index.vue'
import Navbar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'
import TagsView from './components/TagsView/index.vue'
</script>