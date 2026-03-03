<template>
  <div class="p-2 bg-gray-50 flex flex-col h-full" v-loading="loading">
    <div class="flex-shrink-0">
      <QuoteInfo 
        v-if="orderInfo.id" 
        :info="orderInfo" 
        @refresh="loadOrder" 
        @back="handleBack"
        @toggle-other-cost="showOtherCost = !showOtherCost"
        @toggle-summary="showSummary = !showSummary"
      />
    </div>

    <div class="mt-2 flex gap-2 flex-shrink-0 transition-all duration-300" v-if="(showOtherCost || showSummary) && orderInfo.id">
      <div class="flex-1 min-w-0" v-if="showOtherCost">
        <OtherCostTable 
          :quote-id="quoteId" 
          :permissions="orderInfo.actionPermissions"
        />
      </div>
      
      <div class="flex-1 min-w-0" v-if="showSummary">
        <SummaryTable :info="summaryData" />
      </div>
    </div>

    <div class="mt-2 flex-1 min-h-0 relative" v-if="orderInfo.id">
      <div class="absolute inset-0">
      <DetailTable 
        class="h-full w-full"
        v-if="showDetailTable" 
        :quote-id="quoteId"
        :version="orderInfo.currentQuoteVersion"
        :permissions="orderInfo.actionPermissions"
        @refresh-head="loadOrder"
      />

      <BusinessTable 
        class="h-full w-full"
        v-else
        :quote-id="quoteId"
        :version="orderInfo.currentBusinessVersion"
        :permissions="orderInfo.actionPermissions"
        @refresh="loadOrder"
      />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { getQuoteOrder, type QuoteSummary } from '@/api/quote/order'
import QuoteInfo from './components/QuoteInfo.vue'
import DetailTable from './components/DetailTable.vue'
import BusinessTable from './components/BusinessTable.vue'
import OtherCostTable from './components/OtherCostTable.vue'
import SummaryTable from './components/SummaryTable.vue'

const route = useRoute()
const router = useRouter()
const quoteId = route.params.id as string
const loading = ref(false)
const orderInfo = ref<any>({
  actionPermissions: [],
})
const summaryData = ref<QuoteSummary[]>([
  {
    category: '销售总价',
    salesTotal: 0,
    productDiscount: 0,
    otherCostTotal: 0,
    systemTotal: 0,
    contractPrice: 0,
  }
])

  // 【新增】控制显隐的状态
const showOtherCost = ref(true) 
const showSummary = ref(true) 

  // 计算属性：决定显示哪个表
const showDetailTable = computed(() => {
  const status = orderInfo.value.status
  // 0,1,4 显示成本表; 2,3,5 显示业务表
  return ['0', '1', '4'].includes(status)
})

const loadOrder = async () => {
  loading.value = true
  try {
    const res = await getQuoteOrder(quoteId)
    orderInfo.value = res || {}
    // 确保 actionPermissions 不为 null
    if (!orderInfo.value.actionPermissions) {
      orderInfo.value.actionPermissions = []
    }
  } finally {
    loading.value = false
  }
}

// 【新增】处理返回逻辑
const handleBack = () => {
  // 获取 URL 中的 source 参数，如果没有则默认为 'all'
  // 假设参数名为 sourceTab (例如: ?sourceTab=todo)
  const sourceTab = route.query.sourceTab || 'all'
  
  router.push({
    path: '/quote/order',
    // 将 tab 参数传回列表页，告诉列表页该显示哪个标签
    query: { tab: sourceTab } 
  })
}

onMounted(() => {
  if (quoteId) loadOrder()
})
</script>

<style scoped>
/* === 核心：紧凑布局样式覆写 === */

/* 1. 极度压缩卡片头部内边距 (原 18px -> 6px) */
:deep(.el-card__header) {
  padding: 6px 10px !important;
  min-height: 36px;
  line-height: 24px;
}

/* 2. 压缩卡片内容区内边距 (原 20px -> 8px) */
:deep(.el-card__body) {
  padding: 8px !important;
}

/* 3. 压缩表格单元格高度 (上下 Padding 改为 2px) */
:deep(.el-table--small .el-table__cell) {
  padding: 2px 0 !important;
}

/* 4. 缩小表头字体 */
:deep(.el-table thead) {
  font-size: 12px;
  color: #606266;
}

/* 5. 缩小按钮内部间距 */
:deep(.el-button--small) {
  padding: 5px 8px;
  height: 24px;
}
</style>