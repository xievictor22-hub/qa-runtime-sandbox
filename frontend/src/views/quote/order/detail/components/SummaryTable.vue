<template>
  <el-card shadow="never" class="h-full">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-bold text-sm">费用合计</span>
        </div>
    </template>

    <el-table :data="info" border size="small" style="width: 100%">
      <el-table-column prop="salesTotal" label="销售总价" align="center" min-width="80" />
      <el-table-column prop="productDiscount" label="折扣" align="center" min-width="60" />
      <el-table-column prop="otherCostTotal" label="其他费用" align="center" min-width="80" />
      <el-table-column prop="systemTotal" label="系统总价" align="center" min-width="90">
          <template #default="{row}"><span class="text-blue-600 font-bold">{{row.systemTotal}}</span></template>
      </el-table-column>
      <el-table-column prop="contractPrice" label="合同总价" align="center" min-width="90">
          <template #default="{row}"><span class="text-orange-600 font-bold">{{row.contractPrice}}</span></template>
      </el-table-column>
      
      <el-table-column label="操作" width="160" align="center">
        <template #default>
          <div class="flex items-center justify-center gap-2">
            <el-link type="primary" :underline="false" style="font-size: 12px">报价单</el-link>
            <el-link type="primary" :underline="false" style="font-size: 12px">分项</el-link>
            <el-link type="primary" :underline="false" style="font-size: 12px">结算</el-link>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { type QuoteSummary } from '@/api/quote/order'
// 接收父组件传来的汇总信息
defineProps<{
  info: QuoteSummary[]
}>()
</script>