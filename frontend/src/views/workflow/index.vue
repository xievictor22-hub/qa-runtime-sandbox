<script setup lang="ts">
// 引入之前的 Hook
import { useDict } from '@/hooks/useDict'

// 加载字典
const { sys_order_status } = useDict('sys_order_status')

// 模拟表格数据
const tableData = [
  { id: 101, status: 1 },  // 已支付
  { id: 102, status: 0 },  // 待支付
  { id: 103, status: 2 },  // 已取消
  { id: 104, status: -1 },  // 已完成
]
</script>

<template>
  <el-table :data="tableData">
    <el-table-column label="订单ID" prop="id" />
    
    <el-table-column label="状态" align="center">
      <template #default="scope">
        <dict-tag :options="sys_order_status" :value="scope.row.status" />
      </template>
    </el-table-column>
  </el-table>
</template>