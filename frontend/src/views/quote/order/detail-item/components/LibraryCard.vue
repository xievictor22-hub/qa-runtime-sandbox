<template>
  <el-card shadow="never" class="w-1/2 flex flex-col compact-card">
    <template #header>
      <div class="flex justify-between items-center gap-2">
        <span class="font-bold text-gray-700 flex-shrink-0">底价库</span>
        <div class="flex gap-1 flex-1">
           <el-input 
             v-model="localKeyword" 
             placeholder="搜名称/规格..." 
             size="small" 
             clearable 
             :disabled="!isParamsReady" 
             @keyup.enter="handleSearch" 
           />
           <el-select 
             v-model="localVersion" 
             placeholder="请选择版本" 
             class="w-40" 
             clearable 
             filterable
             :disabled="!isParamsReady"
           >
             <el-option v-for="v in versions" :key="v" :label="v" :value="v" />
           </el-select>    
           
           <el-button 
             type="primary" 
             icon="Search" 
             size="small" 
             :disabled="!isParamsReady"
             @click="handleSearch"
           >查</el-button>
        </div>
      </div>
    </template>
    
    <div v-if="!isParamsReady" class="h-full w-full flex items-center justify-center bg-gray-50">
      <el-empty description="等待父级版本与项目类型..." :image-size="80" />
    </div>

    <div v-else-if="loading" class="p-4">
       <el-skeleton :rows="5" animated />
    </div>

    <el-table 
      v-else
      :data="libraryData" 
      border stripe height="100%" size="small" 
      highlight-current-row
      @row-click="$emit('pick', $event)"
      @row-dblclick="$emit('pick-db', $event)"
      class="cursor-pointer select-none"
    >
      <el-table-column prop="process1" label="项目1" width="80" show-overflow-tooltip />
      <el-table-column prop="process2" label="项目2" width="90" show-overflow-tooltip />
      <el-table-column prop="process3" label="项目3" width="90" show-overflow-tooltip />
      <el-table-column prop="process4" label="项目4" min-width="90" show-overflow-tooltip />
      <el-table-column prop="unitPrice" label="分销价" width="70" align="right"/>
      <el-table-column prop="unit" label="单位" width="70" align="right"/>
      </el-table>

    <div class="p-1 border-t flex justify-end" v-if="isParamsReady">
       <el-pagination 
         v-model:current-page="pageNum" 
         v-model:page-size="pageSize"
         :total="total"
         layout="prev, pager, next"
         size="small"
         @current-change="handleSearch"
       />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { listProductLibrary, getBasePriceVersions, ProductLibraryVo } from '@/api/quote/base'
import { ElMessage } from 'element-plus'
import '../style/cartStyle.css'

const props = defineProps<{ 
  externalFilters: {
    process1?: string,
    process2?: string,
    process3?: string,
    process4?: string,
    version?: string,     // 可能为 undefined
    projectType?: number, // 可能为 undefined
  }
}>()

const emit = defineEmits(['pick', 'pick-db'])

// 状态
const loading = ref(false)
const libraryData = ref<ProductLibraryVo[]>([])
const total = ref(0)
const versions = ref<string[]>([])

// 本地搜索条件
const localKeyword = ref('')
const localVersion = ref('') 
const pageNum = ref(1)
const pageSize = ref(10)

// === 核心：严格校验 ===
// 只有当 version 和 projectType 都有真实值时，才允许查询
const isParamsReady = computed(() => {
  const hasType = props.externalFilters.projectType !== undefined && props.externalFilters.projectType !== null;
  // 注意：这里我们校验 localVersion (用户选的) 或者 props.version (父级传的)
  const hasVersion = !!(localVersion.value || props.externalFilters.version);
  return hasType && hasVersion;
})

const handleSearch = async () => {
  // 1. 双重保险：拦截非法调用
  if (!isParamsReady.value) {
    // 只有在用户强制点击按钮时才提示，避免自动加载时骚扰用户
    return 
  }
  
  loading.value = true
  try {
    // 2. 构造参数：此时可以确信 version 和 projectType 是存在的
    const queryParams = {
      process1: props.externalFilters.process1,
      process2: props.externalFilters.process2,
      process3: props.externalFilters.process3,
      process4: props.externalFilters.process4,
      projectType: props.externalFilters.projectType!, // 断言非空
      
      // 版本优先级：本地选择 > 父级传入
      version: localVersion.value || props.externalFilters.version!, 
      
      keyword: localKeyword.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }

    const res = await listProductLibrary(queryParams)
    libraryData.value = res.rows || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
    libraryData.value = []
  } finally {
    loading.value = false
  }
}

// === 监听与同步 ===

// 监听外部筛选条件变化 (Process 变化 -> 自动查)
watch(
  () => [
    props.externalFilters.process1, 
    props.externalFilters.process2,
    props.externalFilters.process3,
    props.externalFilters.process4,
    props.externalFilters.projectType, // 监听类型变化
    props.externalFilters.version      // 监听版本变化
  ], 
  () => {
    // 如果参数就绪，自动触发；否则静默不做任何事
    if (isParamsReady.value) {
        pageNum.value = 1
        handleSearch()
    } else {
        // 参数失效时（比如父级正在重置），清空当前列表，防止误导
        libraryData.value = []
        total.value = 0
    }
  }
)

// 监听父级版本变化，同步给 localVersion (但也允许不同步，取决于交互设计)
// 这里我们选择：如果父级变了，就重置本地选择，跟随父级
watch(() => props.externalFilters.version, (newVal) => {
    if (newVal) {
        localVersion.value = newVal
    }
}, { immediate: true })

onMounted(async () => {
  versions.value = await getBasePriceVersions()
})
</script>