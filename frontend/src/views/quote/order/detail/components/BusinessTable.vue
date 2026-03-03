<template>
  <el-card shadow="never" class="h-full flex-card border-none">
    <template #header>
      <div class="flex justify-between items-center">
        <span>业务价格调整 (Biz Ver: {{ version }})</span>
        <div v-if="canEdit">
           <el-button type="primary" icon="Save" @click="handleBatchSave">保存价格</el-button>
        </div>
      </div>
    </template>
    
    <el-table :data="tableData" v-table-drag border stripe height="100%" size="small">
      <el-table-column prop="productName" label="产品名称" min-width="150" />
      <el-table-column prop="spec" label="规格" width="180" show-overflow-tooltip />
      <el-table-column prop="originalTotal" label="出厂成本" width="120" align="right" />
      
      <el-table-column label="折扣率 (%)" width="150" align="center">
        <template #default="{ row }">
           <el-input-number 
             v-model="row.discountRate" 
             :min="0" :max="200" :controls="false"
             :disabled="!canEdit"
             @change="calcPrice(row)"
             class="w-full"
           />
        </template>
      </el-table-column>
      
      <el-table-column label="折扣价" width="120" align="right">
         <template #default="{ row }">{{ row.discountTotal }}</template>
      </el-table-column>

      <el-table-column label="最终成交价" width="150" align="center">
        <template #default="{ row }">
           <el-input-number 
             v-model="row.finalTotal" 
             :min="0" :controls="false"
             :disabled="!canEdit"
             class="w-full font-bold text-blue-600"
           />
        </template>
      </el-table-column>
      
      <el-table-column prop="remark" label="备注" min-width="150">
        <template #default="{ row }">
           <el-input v-model="row.remark" :disabled="!canEdit" placeholder="业务备注" />
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { listBusinessItems, saveBusinessItems } from '@/api/quote/business'
import { ElMessage } from 'element-plus'

const props = defineProps<{ 
  quoteId: string, 
  version: number, 
  permissions: string[] 
}>()
const emit = defineEmits(['refresh'])
const tableData = ref([])

// 状态后端返回
const canEdit = computed(() => props.permissions?.includes('business-edit'))

const loadData = async () => {
  const res = await listBusinessItems(props.quoteId)
  tableData.value = res.data || []
}

const calcPrice = (row: any) => {
  if (row.originalTotal != null) {
    const rate = row.discountRate || 100
    // 简单计算：折扣价 = 成本 * (rate/100)
    const val = (row.originalTotal * (rate / 100)).toFixed(2)
    row.discountTotal = val
    // 默认最终价跟随折扣价，用户可手动改最终价
    row.finalTotal = val 
  }
}

const handleBatchSave = async () => {
  await saveBusinessItems(tableData.value)
  ElMessage.success('价格保存成功')
  // 不一定要 emit refresh，除非有状态变动
}

watch(() => [props.quoteId, props.version], loadData, { immediate: true })
</script>
<style scoped>
/* 修改 3: 强制改造 el-card 为 Flex 布局，让 body 自动撑满剩余高度 */
.flex-card {
  display: flex;
  flex-direction: column;
}

:deep(.el-card__header) {
  flex-shrink: 0; /* 头部固定 */
}

.flex-card :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  padding: 0 !important; /* 关键 */
  position: relative;
}
</style>