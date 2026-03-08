<template>
  <el-card shadow="never" class="h-full flex-card border-none">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-bold">成本核算明细 (Quote Ver: {{ version }})</span>
        <div v-if="canEdit" class="flex gap-2">
          <el-button type="primary" link icon="Download">导出</el-button>
          <el-upload
            :show-file-list="false"
            :http-request="handleImport"
            accept=".xlsx"
            class="inline-block"
          >
            <el-button type="primary" icon="Upload" >导入Excel</el-button>
          </el-upload>
        </div>
      </div>
    </template>

    <el-table 
      v-table-drag
      :data="tableData" 
      border 
      stripe 
      height="100%" 
      size="small"
      highlight-current-row
    >
      <el-table-column fixed label="#" prop="rowNum" width="45" align="center" />
      <el-table-column fixed label="产品名称" prop="productName" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="font-bold">{{ row.productName }}</div>
          <div class="text-xs text-gray-400">{{ row.sourceVerCode }}</div>
        </template>
      </el-table-column>

      <el-table-column label="分类/区域" align="center">
        <el-table-column label="一级/二级分类" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.categoryLvl1 }} / {{ row.categoryLvl2 }}
          </template>
        </el-table-column>
        <el-table-column prop="projectArea" label="项目区域" width="100" show-overflow-tooltip />
        <el-table-column prop="position" label="位置" width="100" show-overflow-tooltip />
      </el-table-column>

      <el-table-column label="规格属性" align="center">
        <el-table-column prop="spec" label="规格(长*宽)" width="120" show-overflow-tooltip />
        <el-table-column prop="thickness" label="厚度" width="60" align="center" />
        <el-table-column prop="material" label="材质" width="80" show-overflow-tooltip />
        <el-table-column prop="color" label="颜色" width="80" show-overflow-tooltip />
        <el-table-column label="数量" width="90" align="center">
          <template #default="{ row }">
            <span class="font-mono">{{ row.quantity }}</span>
            <span class="text-xs text-gray-500 ml-1">{{ row.unit }}</span>
          </template>
        </el-table-column>
      </el-table-column>

      <el-table-column label="特殊工艺费 (单价)" align="center">
        <el-table-column label="安装" width="70" align="right">
          <template #default="{ row }">
            <span v-if="Number(row.installPriceUnit) > 0">{{ row.installPriceUnit }}</span>
            <span v-else class="text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="无指纹" width="70" align="right">
          <template #default="{ row }">
             <span v-if="row.noFingerprintFlag === '1' || Number(row.noFingerprintPrice)>0">{{ row.noFingerprintPrice }}</span>
             <span v-else class="text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="加工杂项" width="100" show-overflow-tooltip>
           <template #default="{ row }">
             <div class="flex flex-col text-xs scale-90 items-end">
               <span v-if="Number(row.slottingPrice)>0">开槽:{{row.slottingPrice}}</span>
               <span v-if="Number(row.shearingFoldingPrice)>0">剪折:{{row.shearingFoldingPrice}}</span>
               <span v-if="Number(row.laserMPrice)>0">激光:{{row.laserMPrice}}</span>
             </div>
           </template>
        </el-table-column>
      </el-table-column>

      <!-- <el-table-column label="工厂成本 (内部)" align="center" class-name="bg-red-50">
        <el-table-column prop="factoryTotal" label="出厂总价" width="100" align="right">
          <template #default="{ row }">
            <span class="font-bold text-red-600">{{ row.factoryTotal }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="installTotal" label="安装总价" width="90" align="right">
           <template #default="{ row }">{{ row.installTotal }}</template>
        </el-table-column>
        <el-table-column prop="taxAmount" label="税金" width="80" align="right" />
      </el-table-column> -->
      <el-table-column label="工厂成本 (内部)" align="center" class-name="bg-red-50">
        <el-table-column prop="summaryPrice" label="出厂总价" width="100" align="right">
          <template #default="{ row }">
            <span class="font-bold text-red-600">{{ row.summaryPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="installTotal" label="安装总价" width="90" align="right">
           <template #default="{ row }">{{ row.installTotal }}</template>
        </el-table-column>
        <el-table-column prop="distPrice" label="单价" width="80" align="right" />
      </el-table-column>

      <el-table-column label="客户报价 (外部)" align="center" class-name="bg-blue-50">
        <el-table-column prop="custUnitPrice" label="客户单价" width="100" align="right" />
        <el-table-column prop="custTotalPrice" label="客户总价" width="110" align="right">
          <template #default="{ row }">
            <span class="font-bold text-blue-600">{{ row.custTotalPrice }}</span>
          </template>
        </el-table-column>
        </el-table-column>

      <el-table-column label="变更信息" align="center">
        <el-table-column prop="changeCategory" label="类型" width="80" show-overflow-tooltip />
        <el-table-column prop="deptOwner" label="承担部门" width="90" show-overflow-tooltip />
        <el-table-column prop="changeDesc" label="说明" min-width="120" show-overflow-tooltip />
      </el-table-column>

      <el-table-column prop="remarkDesc" label="技术备注" min-width="150" show-overflow-tooltip />

      <el-table-column fixed="left" label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button 
            v-if="canEdit"
            link type="primary" 
            icon="Edit"
            @click="openItemDialog(row)"
            :disabled="row.detailType === 0"
          >
            明细
          </el-button>
          <el-button v-else link icon="View" :disabled="row.detailType === 0"  @click="openItemDialog(row)">查看</el-button>
          
          </template>
      </el-table-column>
    </el-table>

    
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { listQuoteDetails, importQuoteDetail,type QuoteDetailVO } from '@/api/quote/detail'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const props = defineProps<{ quoteId: string, version: number, permissions: string[] }>()
const emit = defineEmits(['refresh-head'])

const tableData = ref<QuoteDetailVO[]>([])
const router = useRouter()


const canEdit = computed(() => props.permissions?.includes('cost-edit'))

const loadData = async () => {
  // 这里调用后端接口，返回的数据结构需匹配上述 prop
  // SQL字段转驼峰: category_lvl1 -> categoryLvl1
  const res = await listQuoteDetails(props.quoteId)
  tableData.value = res || []
}

const handleImport = async (options: any) => {
  try {
    await importQuoteDetail(props.quoteId, options.file)
    ElMessage.success('导入成功')
    loadData()
    emit('refresh-head')
  } catch(e) {}
}

const openItemDialog = (row: any) => {
  // 改为路由跳转
  // router.push({
  //   path: '/quote/formula-editor', // 对应路由配置的 path
  //   query: {
  //     detailId: row.id
  //     // 添加一个随机数或者时间戳，确保即使点击同一行也能触发路由钩子(可选，一般ID变了就行) 
  //   }
  // })

  router.push({
    path: '/quote/detail-item', // 对应路由配置的 path
    query: {
      detailId: row.id
      // 添加一个随机数或者时间戳，确保即使点击同一行也能触发路由钩子(可选，一般ID变了就行) 
    }
  })
}

watch(() => [props.quoteId, props.version], loadData, { immediate: true })
</script>

<style scoped>
/* 适配 flex-card 逻辑，参考之前的 CSS 修改 */
.flex-card {
  display: flex;
  flex-direction: column;
}
/* 头部不许压缩 */
:deep(.el-card__header) {
  flex-shrink: 0;
}
/* 【关键修改】加强选择器优先级，确保覆盖父组件的样式 */
/* 使用 .flex-card 前缀来提升权重 */
.flex-card :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  padding: 0 !important; /* 强制覆盖 index.vue 的 8px */
  position: relative;
}
/* 针对多级表头的背景色微调，区分内部/外部价格 */
:deep(.el-table .bg-red-50) {
  background-color: #fef2f2 !important;
}
:deep(.el-table .bg-blue-50) {
  background-color: #eff6ff !important;
}
</style>