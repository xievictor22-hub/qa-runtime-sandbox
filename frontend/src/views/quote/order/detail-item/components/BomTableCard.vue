<template>
  <el-card shadow="never" class="flex-1 min-h-0 flex flex-col compact-card">
    <template #header>
       <div class="flex justify-between items-center h-6">
         <div class="flex items-center gap-2">
           <span class="font-bold">材料明细 (BOM)</span>
           <el-tag type="info" size="small">{{ totalCount }} 项</el-tag>
         </div>
         <div class="text-sm">
           合计成本: <span class="text-red-600 font-bold text-lg font-mono mr-1">{{ totalAmount }}</span> 元
         </div>
       </div>
    </template>
    
    <div class="flex flex-col flex-1 min-h-0 h-full">
      <div class="flex gap-2 p-2 border-b bg-gray-50 items-center flex-shrink-0 flex-wrap">
        <el-input v-model="search.process1" placeholder="项目1" size="small" clearable class="w-24" />
        <el-input v-model="search.process2" placeholder="项目2" size="small" clearable class="w-24" />
        <el-input v-model="search.process3" placeholder="项目3" size="small" clearable class="w-32" />
        <el-input v-model="search.process4" placeholder="项目4" size="small" clearable class="w-24" />
        <el-input v-model="search.quantity" placeholder="数量" size="small" clearable class="w-20" />
        <el-input v-model="search.distPrice" placeholder="价格" size="small" clearable class="w-20" />
        <el-input v-model="search.remark" placeholder="备注" size="small" clearable class="w-32" />
        <el-input v-model="search.totalPrice" placeholder="汇总价" size="small" clearable class="w-20" />
        <el-input v-model="search.unit" placeholder="单位" size="small" clearable class="w-20" />
        <el-button icon="Refresh" size="small" @click="$emit('reset-search')">重置</el-button>
      </div>

      <div class="flex-1 min-h-0">
        <el-table :data="data" border stripe height="100%" size="small" v-loading="loading">
          <el-table-column type="index" width="50" align="center">
            <template #default="scope">
              {{ (pagination.pageNum - 1) * pagination.pageSize + scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="process1" label="项目1" width="100" show-overflow-tooltip />
          <el-table-column prop="process2" label="项目2" width="120" show-overflow-tooltip />
          <el-table-column prop="process3" label="项目3" width="140" show-overflow-tooltip />
          <el-table-column prop="process4" label="项目4" width="100" show-overflow-tooltip />
          <el-table-column prop="distPrice" label="分销价" width="90" align="right"/>
          <el-table-column prop="unit" label="单位" width="60" align="center" />
          <el-table-column prop="quantity" label="数量" width="90" align="center" class-name="bg-gray-50 font-bold" />
          <el-table-column prop="totalPrice" label="汇总价" width="100" align="right"/>
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary"  link icon="Edit" @click="$emit('edit', row)">编辑</el-button>
              <el-button type="danger" link icon="Delete" @click="$emit('delete', row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="flex justify-end p-1 border-t flex-shrink-0 bg-white">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[9, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="totalCount"
          size="small"
          background
        />
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { QuoteDetailItemSearchDto, QuoteDetailItemVO } from '@/api/quote/item';
import { type PageQuery } from '@/api/types';
import '../style/cartStyle.css'

const props = defineProps<{
  data: QuoteDetailItemVO[]
  loading: boolean
  totalCount: number
  totalAmount: number
  search: QuoteDetailItemSearchDto
  pagination: PageQuery
}>()

const emit = defineEmits(['edit', 'delete', 'reset-search'])

</script>