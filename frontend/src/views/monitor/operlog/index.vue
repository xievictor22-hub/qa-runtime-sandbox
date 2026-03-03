<template>
  <div class="flex flex-col h-full p-4 gap-4">
    <el-card shadow="never" class="shrink-0">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="系统模块">
          <el-input v-model="queryParams.title" placeholder="请输入模块标题" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="操作人员">
          <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="danger" plain :icon="Delete" :disabled="ids.length === 0" @click="handleDelete">删除</el-button>
          <el-button type="danger" :icon="Delete" @click="handleClean">清空</el-button>
        </el-form-item>
      </el-form> 
    </el-card>

    <el-card shadow="never" class="flex-1 min-h-0 flex flex-col" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column' }">  
    <div class="flex-1 min-h-0">
      <el-table v-loading="loading" :data="tableData" style="height: 100%" border class="flex-1" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="日志编号" prop="id" width="200" align="center" />
        <el-table-column label="系统模块" prop="title" align="center" />
        <el-table-column label="业务类型" prop="businessType" align="center">
          <template #default="scope">
            <el-tag>{{ getBusinessTypeName(scope.row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求方式" prop="requestMethod" align="center" />
        <el-table-column label="操作人员" prop="operName" align="center" />
        <el-table-column label="主机" prop="operIp" align="center" width="130" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作日期" prop="operTime" width="180" align="center" />
        <el-table-column label="耗时" prop="costTime" align="center">
          <template #default="scope">
            {{ scope.row.costTime }}ms
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template #default="scope">
            <el-button link type="primary" :icon="View" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
      <div class="mt-4 shrink-0 flex justify-end">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <el-dialog title="操作日志详情" v-model="detailVisible" width="700px">
      <el-form :model="detailForm" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="操作模块:">{{ detailForm.title }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="请求地址:">{{ detailForm.operUrl }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作人员:">{{ detailForm.operName }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作IP:">{{ detailForm.operIp }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="请求方式:">{{ detailForm.requestMethod }}</el-form-item></el-col>
        </el-row>
        <el-form-item label="操作方法:">
          <div class="code-box">{{ detailForm.method }}</div>
        </el-form-item>
        <el-form-item label="请求参数:">
          <div class="code-box">{{ detailForm.operParam }}</div>
        </el-form-item>
        <el-form-item label="返回参数:">
          <div class="code-box">{{ detailForm.jsonResult }}</div>
        </el-form-item>
        <el-form-item label="错误信息:" v-if="detailForm.status === 0">
          <div class="code-box text-red-500">{{ detailForm.errorMsg }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOperLog, delOperLog, cleanOperLog } from '@/api/monitor/operlog'
import { View, Delete, Refresh, Search } from '@element-plus/icons-vue'

const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const ids = ref<number[]>([])
const detailVisible = ref(false)
const detailForm = ref<any>({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  operName: '',
  status: undefined
})

const businessTypes: any = {
  0: '其它', 1: '新增', 2: '修改', 3: '删除', 
  4: '授权', 5: '导出', 6: '导入', 7: '强退', 8: '清空'
}

const getBusinessTypeName = (type: number) => businessTypes[type] || '未知'

const handleQuery = async () => {
  loading.value = true
  try {
    const res: any = await listOperLog(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.title = ''
  queryParams.operName = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  handleQuery()
}

const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map(item => item.id)
}

const handleDetail = (row: any) => {
  detailForm.value = row
  detailVisible.value = true
}

const handleDelete = () => {
  ElMessageBox.confirm(`是否确认删除选中的 ${ids.value.length} 条日志?`, '警告', { type: 'warning' }).then(async () => {
    await delOperLog(ids.value)
    ElMessage.success('删除成功')
    handleQuery()
  })
}

const handleClean = () => {
  ElMessageBox.confirm('是否确认清空所有操作日志?', '警告', { type: 'error' }).then(async () => {
    await cleanOperLog()
    ElMessage.success('清空成功')
    handleQuery()
  })
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.code-box {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>