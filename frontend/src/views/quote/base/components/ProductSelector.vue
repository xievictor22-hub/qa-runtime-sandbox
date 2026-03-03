<template>
  <el-dialog v-model="visible" title="选择基础产品" width="800px" append-to-body>
    <div class="flex gap-2 mb-4">
      <el-input 
        v-model="queryParams.name" 
        placeholder="输入名称或规格回车搜索" 
        clearable
        @keyup.enter="loadData"
      >
        <template #append><el-button icon="Search" @click="loadData" /></template>
      </el-input>
    </div>

    <el-table 
      :data="tableData" 
      border 
      stripe 
      height="400" 
      v-loading="loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="spec" label="规格" />
      <el-table-column prop="unit" label="单位" width="60" align="center" />
      <el-table-column prop="costPrice" label="参考价" width="100" align="right" />
    </el-table>
    
    <el-pagination
      class="mt-4"
      small
      layout="prev, pager, next"
      :total="total"
      v-model:current-page="queryParams.pageNum"
      @current-change="loadData"
    />

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSelect" :disabled="!selectedRows.length">
        确认选择 ({{ selectedRows.length }})
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { listBaseProducts } from '@/api/quote/base'

const visible = ref(false)
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])
const emit = defineEmits(['select'])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: ''
})

const open = () => {
  visible.value = true
  // 每次打开重置并查询
  queryParams.pageNum = 1
  queryParams.name = ''
  selectedRows.value = []
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await listBaseProducts(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (val: any) => {
  selectedRows.value = val
}

const confirmSelect = () => {
  // 将选中行抛出给父组件 (ItemDialog)
  emit('select', JSON.parse(JSON.stringify(selectedRows.value)))
  visible.value = false
}

defineExpose({ open })
</script>