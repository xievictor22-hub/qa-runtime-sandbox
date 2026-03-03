<template>
  <div class="p-2 bg-gray-50 flex flex-col h-full overflow-hidden formula-page">
    <el-card shadow="never" class="mb-2 flex-shrink-0 !border-none compact-card">
      <div class="flex justify-between items-center h-8">
        <div class="flex items-center gap-2">
          <el-button icon="ArrowLeft" circle size="small" @click="handleBack" />
          <span class="font-bold text-gray-700 text-lg">组价明细</span>
          <el-tag type="info" size="small" v-if="detailId">ID: {{ detailId }}</el-tag>
        </div>
        <div class="flex gap-2">
          <el-button size="small" @click="initData">刷新页面</el-button>
        </div>
      </div>
    </el-card>

    <div class="flex flex-col flex-1 min-h-0 gap-2">
      <div class="flex gap-2 h-[250px] flex-shrink-0">
        <ItemEntryCard 
          v-model="itemForm"
          :project-type="parentInfo.projectType"
          :version="parentInfo.version"
          :is-editing="isEditing"
          ref="entryCardRef"
          @submit="handleSubmitItem"
          @reset="handleResetForm"
          @cancel-edit="cancelEdit"
        />

        <LibraryCard 
          :external-filters="queryParams"
          @pick="handleLibraryPick"
          @pick-db="handleLibraryPickDb"
        />
      </div>

      <ParentInfoCard 
        v-model="parentInfo"
        :loading="parentLoading"
        @save="handleUpdateParent"
      />

      <BomTableCard 
        :data="displayItems"
        :loading="itemsLoading"
        :total-count="filteredItems.length"
        :total-amount="parentInfo.summaryPrice || 0" 
        :search="searchParams"
        :pagination="pagination"
        @edit="handleEditItem"
        @delete="handleRemoveItem"
        @reset-search="resetSearch"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTagsViewStore } from '@/stores/system/tagsView'

import './style/cartStyle.css'

import ItemEntryCard from './components/ItemEntryCard.vue'
import LibraryCard from './components/LibraryCard.vue'
import ParentInfoCard from './components/ParentInfoCard.vue'
import BomTableCard from './components/BomTableCard.vue'

import { useBomItems } from './composables/useBomItems'
import { addDetailItem, updateDetailItem, deleteDetailItem, type QuoteDetailItemFormDto } from '@/api/quote/item'
import { ParentInfo, selectQuoteDetail, updateQuoteDetail } from '@/api/quote/detail'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

// === 核心状态 ===
const detailId = ref('')
const parentLoading = ref(false)
const entryCardRef = ref()

// 父级信息 (Source of Truth 1)
const parentInfo = ref<ParentInfo>({
    version: '',
    projectType: -1,
    id: '',
    quoteId: '',
    summaryPrice: 0, // 后端计算的总价
    distPrice: 0,
    // ... 其他字段初始化
} as any)

// 子项表单 (Source of Truth 2)
const getInitItemForm = (): QuoteDetailItemFormDto => ({
  id: '',
  unit: '',
  quantity: undefined,
  process1: '',
  process2: '',
  process3: '',
  process4: '',
  distPrice: undefined,
  remark: '',
  quoteId: parentInfo.value.quoteId, // 动态获取
  detailId: detailId.value,
  sourceLibraryId: '',
  version: '',
  formulaSnapshot: 1,//损耗
})
const itemForm = reactive(getInitItemForm())
const isEditing = ref(false)

// === 关键：构造筛选参数给 LibraryCard ===
const queryParams = computed(() => ({
  // 来自 ItemEntryCard 的录入数据
  process1: itemForm.process1,
  process2: itemForm.process2,
  process3: itemForm.process3,
  process4: itemForm.process4,
  // 来自 ParentInfo 的环境数据
  version: parentInfo.value.version,
  projectType: parentInfo.value.projectType,
}))

// BOM Hook (移除了前端算费)
const { 
  loading: itemsLoading, searchParams, pagination, 
  displayItems, filteredItems, 
  loadItems, resetSearch 
} = useBomItems()

// === 业务逻辑：数据初始化与刷新 ===

// 刷新父级信息 (获取后端计算的最新价格)
const refreshParentInfo = async () => {
  if (!detailId.value) return
  try {
    const res = await selectQuoteDetail(detailId.value)
    if (res) {
       parentInfo.value = res
       console.log("刷新父级价格成功",parentInfo.value)
    }
  } catch (e) {
    console.error("刷新父级价格失败", e)
  }
}

const initData = async () => {
  const id = route.query.detailId as string
  if (!id) return
  detailId.value = id
  handleResetForm()
  
  try {
    parentLoading.value = true
    // 1. 获取父级
    await refreshParentInfo()
    // 2. 获取子项
    await loadItems(id)
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    parentLoading.value = false
  }
}

const handleUpdateParent = async () => {
  try {
    await updateQuoteDetail(parentInfo.value)
    ElMessage.success('父级信息已保存')
    // 保存后可能导致某些状态变更，建议重新拉取
    await refreshParentInfo()
  } catch(e) {
    ElMessage.error('保存失败')
  }
}

// === 业务逻辑：子项增删改 ===
const handleResetForm = () => {
  Object.assign(itemForm, getInitItemForm())
  // 确保 quoteId 和 detailId 不丢失
  itemForm.quoteId = parentInfo.value.quoteId
  itemForm.detailId = detailId.value
  itemForm.sourceLibraryId = ''
  itemForm.version = ''
  entryCardRef.value?.clearCalculator()
  isEditing.value = false
}

const cancelEdit = () => handleResetForm()

const handleSubmitItem = async () => {
  try {
    // 确保 ID 正确
    itemForm.detailId = detailId.value
    itemForm.quoteId = parentInfo.value.quoteId
    
    if (itemForm.id) {
      await updateDetailItem({ ...itemForm, id: itemForm.id! })
      ElMessage.success('修改成功')
    } else {
      await addDetailItem({ ...itemForm, quoteId: parentInfo.value.quoteId!, detailId: detailId.value })
      ElMessage.success('新增成功')
    }
    
    handleResetForm()
    // 1. 重新加载列表
    await loadItems(detailId.value)
    // 2. 【关键】重新加载父级信息以更新总价
    await refreshParentInfo()
    
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleRemoveItem = async (id: string) => {
  try {
    await deleteDetailItem(id)
    ElMessage.success('删除成功')
    await loadItems(detailId.value)
    // 【关键】删除后也要刷新总价
    await refreshParentInfo()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleEditItem = (row: any) => {
  handleResetForm()
  Object.assign(itemForm, row)
  isEditing.value = true
}

const handleLibraryPick = (row: any) => {
  itemForm.process1 = row.process1
  itemForm.process2 = row.process2
  itemForm.process3 = row.process3
  itemForm.process4 = row.process4 || ''
  itemForm.unit = row.unit
  itemForm.distPrice = row.unitPrice || 0
  itemForm.sourceLibraryId = row.id
  itemForm.version = parentInfo.value.version || ''
  itemForm.formulaSnapshot = row.quoteFormula || 1
  //todo
}

const handleLibraryPickDb = (row: any) => {
  handleLibraryPick(row)
}

const handleBack = () => {
  tagsViewStore.delView(route)
  router.go(-1)
}

watch(() => route.query.detailId, (newId) => {
  if (newId) initData()
}, { immediate: true })
</script>

