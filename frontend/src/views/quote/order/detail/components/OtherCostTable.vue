<template>
  <el-card shadow="never" class="h-full">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-bold text-sm">其他费用</span>
        <div v-if="!readOnly">
          <el-button type="primary" link icon="Plus" size="small" @click="handleAdd">添加</el-button>
          <el-button type="primary" link icon="Edit" size="small" :disabled="!currentRow" @click="handleEdit">修改</el-button>
          <el-button type="danger" link icon="Delete" size="small" :disabled="!currentRow" @click="handleDelete">删除</el-button>
        </div>
      </div>
    </template>

    <el-table 
      :data="tableData" 
      border 
      size="small" 
      height="140" 
      highlight-current-row
      @current-change="handleCurrentChange"
    >
      <el-table-column type="index" label="#" width="45" align="center" />
      <el-table-column prop="costName" label="费用名称" min-width="90" show-overflow-tooltip />
      <el-table-column prop="isTax" label="含税" width="55" align="center">
        <template #default="{ row }">{{ row.isTax ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="80" align="right" />
      <el-table-column prop="remark" label="备注" min-width="80" show-overflow-tooltip />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  quoteId: string
  permissions?: string[]
}>()

// 模拟数据，实际开发请调用 API
const tableData = ref([
  { id: 1, costName: '蚀刻网版费', isTax: true, amount: 545.00, description: '', remark: '' },
  { id: 2, costName: '维保费', isTax: true, amount: 100.32, description: '', remark: '' },
  { id: 3, costName: '蚀刻网版费', isTax: true, amount: 545.00, description: '', remark: '' },
  { id: 4, costName: '维保费', isTax: true, amount: 100.32, description: '', remark: '' },
  { id: 5, costName: '蚀刻网版费', isTax: true, amount: 545.00, description: '', remark: '' },
  { id: 6, costName: '维保费', isTax: true, amount: 100.32, description: '', remark: '' },
  { id: 7, costName: '蚀刻网版费', isTax: true, amount: 545.00, description: '', remark: '' },
  { id: 8, costName: '维保费', isTax: true, amount: 100.32, description: '', remark: '' }
])

const currentRow = ref(null)
const readOnly = computed(() => !props.permissions?.includes('cost-edit')) // 假设有费用编辑权限控制

const handleCurrentChange = (val: any) => {
  currentRow.value = val
}

const handleAdd = () => { console.log('点击添加') }
const handleEdit = () => { console.log('点击修改') }
const handleDelete = () => { console.log('点击删除') }
</script>