<template>
  <div class="p-2 bg-gray-50 flex flex-col h-full business-adjust-page" v-loading="loading">
    <div class="flex-shrink-0" v-if="orderInfo.id">
      <QuoteInfo
        :info="orderInfo"
        :hide-summary-toggle="true"
        :hide-other-cost-toggle="true"
        :hide-business-adjust-button="true"
        @refresh="loadPageData"
        @back="handleBack"
      />
    </div>

    <el-card shadow="never" class="mt-2 flex-shrink-0 compact-card" v-if="orderInfo.id">
      <template #header>
        <div class="font-bold text-sm">基础数据</div>
      </template>
      <div class="grid grid-cols-4 gap-x-3 gap-y-1 text-xs leading-6">
        <div><span class="label">报价单号</span>{{ orderInfo.id }}</div>
        <div><span class="label">项目名称</span>{{ orderInfo.projectName || '-' }}</div>
        <div><span class="label">客户名称</span>{{ orderInfo.customerName || '-' }}</div>
        <div><span class="label">状态</span>{{ statusText(orderInfo.status) }}</div>
        <div><span class="label">报价版本</span>v{{ orderInfo.currentQuoteVersion }}</div>
        <div><span class="label">业务版本</span>v{{ orderInfo.currentBusinessVersion }}</div>
        <div><span class="label">创建时间</span>{{ orderInfo.createTime || '-' }}</div>
        <div><span class="label">更新时间</span>{{ orderInfo.updateTime || '-' }}</div>
      </div>

      <el-divider class="!my-2" />

      <div class="text-xs">
        <div class="font-semibold mb-1 text-gray-700">QuoteDetail 当前行数据</div>
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="行号">{{ activeDetail?.rowNum ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ activeDetail?.productName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ activeDetail?.spec || '-' }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ activeDetail?.quantity ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="出厂总价">{{ activeDetail?.summaryPrice ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="安装总价">{{ activeDetail?.installTotal ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户单价">{{ activeDetail?.custUnitPrice ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户总价">{{ activeDetail?.custTotalPrice ?? '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-2 flex-1 min-h-0 flex-card compact-card" v-if="orderInfo.id">
      <template #header>
        <div class="flex justify-between items-center gap-2 flex-wrap">
          <div class="font-bold text-sm">业务调整（Biz Ver: {{ orderInfo.currentBusinessVersion }}）</div>
          <div class="flex items-center gap-2" v-if="canEdit">
            <el-button size="small" type="warning" plain @click="batchDialogVisible = true">批量生产/安装打点</el-button>
            <el-button size="small" type="primary" icon="Save" @click="saveChanges">保存</el-button>
          </div>
        </div>
        <div class="mt-2 flex gap-2 text-xs text-gray-600 flex-wrap">
          <el-tag type="info" size="small">生产成本: {{ fmt(summary.factoryTotal) }}</el-tag>
          <el-tag type="success" size="small">客户出厂: {{ fmt(summary.customerFactoryTotal) }}</el-tag>
          <el-tag type="info" size="small">安装成本: {{ fmt(summary.installTotal) }}</el-tag>
          <el-tag type="success" size="small">客户安装: {{ fmt(summary.customerInstallTotal) }}</el-tag>
          <el-tag type="warning" size="small">客户总价: {{ fmt(summary.customerTotalPrice) }}</el-tag>
          <el-tag type="danger" size="small">额外金额: {{ fmt(extraAmount) }}</el-tag>
        </div>
      </template>

      <el-table :data="businessItems" border stripe size="small" height="100%" highlight-current-row @current-change="onCurrentRowChange">
        <el-table-column prop="rowNum" label="#" width="46" align="center" />
        <el-table-column prop="productName" label="产品" min-width="130" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" min-width="130" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="70" align="right" />
        <el-table-column prop="factoryTotal" label="出厂成本" width="92" align="right" />
        <el-table-column prop="installTotal" label="安装成本" width="92" align="right" />

        <el-table-column label="生产打点(%)" width="118">
          <template #default="{ row }">
            <el-input-number v-model="row.factoryProfitRate" :controls="false" :precision="2" :disabled="!canEdit" @change="onFactoryRateChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="客户出厂总价" width="128" align="right">
          <template #default="{ row }">
            <el-input-number v-model="row.customerFactoryTotal" :controls="false" :precision="2" :disabled="!canEdit" @change="onCustomerFactoryTotalChange(row)" />
          </template>
        </el-table-column>

        <el-table-column label="安装打点(%)" width="118">
          <template #default="{ row }">
            <el-input-number v-model="row.installProfitRate" :controls="false" :precision="2" :disabled="!canEdit" @change="onInstallRateChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="客户安装总价" width="128" align="right">
          <template #default="{ row }">
            <el-input-number v-model="row.customerInstallTotal" :controls="false" :precision="2" :disabled="!canEdit" @change="onCustomerInstallTotalChange(row)" />
          </template>
        </el-table-column>

        <el-table-column label="销售打点(%)" width="108">
          <template #default="{ row }">
            <el-input-number v-model="row.salesPoint" :controls="false" :precision="2" :disabled="!canEdit" @change="onSalesPointChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="客户单价" width="118" align="right">
          <template #default="{ row }">
            <el-input-number v-model="row.customerUnitPrice" :controls="false" :precision="2" :disabled="!canEdit" @change="onCustomerUnitPriceChange(row)" />
          </template>
        </el-table-column>

        <el-table-column prop="customerTotalPrice" label="客户总价" width="108" align="right" />
        <el-table-column prop="salesUnitAdjustAmount" label="调价差额" width="108" align="right" />
      </el-table>
    </el-card>

    <el-dialog v-model="batchDialogVisible" title="批量设置生产/安装打点" width="420px" append-to-body>
      <el-form label-width="120px">
        <el-form-item label="生产打点(%)">
          <el-input-number v-model="batchForm.factoryRate" :controls="false" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="安装打点(%)">
          <el-input-number v-model="batchForm.installRate" :controls="false" :precision="2" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchApply">确定生效</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuoteOrder } from '@/api/quote/order'
import { listQuoteDetails } from '@/api/quote/detail'
import { listBusinessItems, saveBusinessItems } from '@/api/quote/business'
import QuoteInfo from '../detail/components/QuoteInfo.vue'

const route = useRoute()
const router = useRouter()
const quoteId = route.params.id as string

const loading = ref(false)
const orderInfo = ref<any>({ actionPermissions: [] })
const detailPreview = ref<any[]>([])
const businessItems = ref<any[]>([])
const currentRow = ref<any>(null)

const batchDialogVisible = ref(false)
const batchForm = ref({ factoryRate: null as number | null, installRate: null as number | null })

const canEdit = computed(() => orderInfo.value.actionPermissions?.includes('business-edit'))
const activeDetail = computed(() => {
  if (!currentRow.value) return null
  return detailPreview.value.find((d) => d.id === currentRow.value.detailId) || null
})

const statusMap: Record<string, string> = { '0': '报价中', '1': '审核中', '2': '业务调整', '3': '已完成', '4': '重新报价中', '5': '业务重调中', '6': '重新报价中' }
const statusText = (status: string) => statusMap[status] || status

const n = (v: any) => Number(v || 0)
const to2 = (v: number) => Number(v.toFixed(2))
const fmt = (v: number) => to2(v).toFixed(2)

const extraAmount = computed(() => summary.value.customerTotalPrice - summary.value.customerFactoryTotal - summary.value.customerInstallTotal)
const summary = computed(() => businessItems.value.reduce((acc, item) => {
  acc.factoryTotal += n(item.factoryTotal)
  acc.customerFactoryTotal += n(item.customerFactoryTotal)
  acc.installTotal += n(item.installTotal)
  acc.customerInstallTotal += n(item.customerInstallTotal)
  acc.customerTotalPrice += n(item.customerTotalPrice)
  return acc
}, { factoryTotal: 0, customerFactoryTotal: 0, installTotal: 0, customerInstallTotal: 0, customerTotalPrice: 0 }))

const recalcByRates = (row: any) => {
  row.customerFactoryTotal = to2(n(row.factoryTotal) * (1 + n(row.factoryProfitRate) / 100))
  row.customerInstallTotal = to2(n(row.installTotal) * (1 + n(row.installProfitRate) / 100))
  if (!row.customerUnitPrice || n(row.customerUnitPrice) === 0) {
    row.customerUnitPrice = to2((n(row.customerFactoryTotal) + n(row.customerInstallTotal)) / Math.max(n(row.quantity), 1))
  }
  recalcByUnitPrice(row)
}

const recalcByUnitPrice = (row: any) => {
  row.customerTotalPrice = to2(n(row.customerUnitPrice) * n(row.quantity))
  row.salesUnitAdjustAmount = to2(n(row.customerUnitPrice) - n(row.salesUnitPrice))
}

const onFactoryRateChange = (row: any) => recalcByRates(row)
const onInstallRateChange = (row: any) => recalcByRates(row)
const onCustomerFactoryTotalChange = (row: any) => {
  row.factoryProfitRate = n(row.factoryTotal) ? to2((n(row.customerFactoryTotal) - n(row.factoryTotal)) / n(row.factoryTotal) * 100) : 0
  recalcByRates(row)
}
const onCustomerInstallTotalChange = (row: any) => {
  row.installProfitRate = n(row.installTotal) ? to2((n(row.customerInstallTotal) - n(row.installTotal)) / n(row.installTotal) * 100) : 0
  recalcByRates(row)
}
const onSalesPointChange = (row: any) => {
  row.customerUnitPrice = to2(n(row.salesUnitPrice) * (1 + n(row.salesPoint) / 100))
  recalcByUnitPrice(row)
}
const onCustomerUnitPriceChange = (row: any) => {
  const totalCost = n(row.customerFactoryTotal) + n(row.customerInstallTotal)
  const totalPrice = n(row.customerUnitPrice) * n(row.quantity)
  row.salesPoint = totalCost ? to2((totalPrice - totalCost) / totalCost * 100) : 0
  recalcByUnitPrice(row)
}

const confirmBatchApply = () => {
  const factoryRate = batchForm.value.factoryRate
  const installRate = batchForm.value.installRate
  if (factoryRate === null && installRate === null) {
    ElMessage.warning('请至少输入一个打点')
    return
  }

  businessItems.value.forEach((row) => {
    if (factoryRate !== null) {
      row.factoryProfitRate = factoryRate
    }
    if (installRate !== null) {
      row.installProfitRate = installRate
    }
    recalcByRates(row)
  })

  batchDialogVisible.value = false
}

const saveChanges = async () => {
  await saveBusinessItems(businessItems.value)
  ElMessage.success('业务调整保存成功')
  await loadBusinessItems()
}

const loadBusinessItems = async () => {
  const res = await listBusinessItems(quoteId)
  businessItems.value = (res.data || []).map((item: any) => ({ ...item }))
  currentRow.value = businessItems.value[0] || null
}

const onCurrentRowChange = (row: any) => {
  currentRow.value = row
}

const loadPageData = async () => {
  loading.value = true
  try {
    orderInfo.value = await getQuoteOrder(quoteId)
    if (!orderInfo.value.actionPermissions) {
      orderInfo.value.actionPermissions = []
    }
    detailPreview.value = await listQuoteDetails(quoteId)
    await loadBusinessItems()
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  const sourceTab = route.query.sourceTab || 'all'
  router.push({ path: '/quote/order', query: { tab: sourceTab } })
}

loadPageData()
</script>

<style scoped>
.business-adjust-page :deep(.el-card__header) {
  padding: 6px 10px !important;
}
.business-adjust-page :deep(.el-card__body) {
  padding: 8px !important;
}
.business-adjust-page :deep(.el-table--small .el-table__cell) {
  padding: 3px 0 !important;
}
.business-adjust-page :deep(.el-input-number) {
  width: 100%;
}
.label {
  color: #909399;
  margin-right: 6px;
}
.compact-card {
  border-radius: 8px;
}
.flex-card {
  display: flex;
  flex-direction: column;
}
.flex-card :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  padding: 0 !important;
}
</style>
