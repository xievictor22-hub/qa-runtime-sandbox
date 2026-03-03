<template>
  <div class="flex flex-col h-full p-4 gap-4">
    <el-card shadow="never" class="shrink-0">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="登录地址">
          <el-input v-model="queryParams.ipaddr" placeholder="请输入IP" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="用户名称">
          <el-input v-model="queryParams.username" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

     <el-card shadow="never" class="flex-1 min-h-0 flex flex-col" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column' }">  
       <div class="flex-1 min-h-0">
    <el-table v-loading="loading" :data="tableData" border style="width: 100%" class="flex-1">
        <el-table-column label="序号" type="index" width="50" align="center" />
        <el-table-column label="会话编号" prop="uuid" :show-overflow-tooltip="true" />
        <el-table-column label="用户名称" prop="username" align="center" />
        <el-table-column label="主机" prop="ipaddr" align="center" />
        <el-table-column label="浏览器" prop="browser" align="center" />
        <el-table-column label="操作系统" prop="os" align="center" />
        <el-table-column label="登录时间" align="center" width="180">
          <template #default="scope">
            {{ formatDate( scope.row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="danger" :icon="Delete" @click="handleForceLogout(scope.row)" v-permission="['monitor:online:forceLogout']">强退</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOnline, forceLogout } from '@/api/monitor/online'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const queryParams = reactive({
  ipaddr: '',
  username: ''
})

const handleQuery = async () => {
  loading.value = true
  const res: any = await listOnline(queryParams)
  tableData.value = res
  loading.value = false
}

const handleReset = () => {
  queryParams.ipaddr = ''
  queryParams.username = ''
  handleQuery()
}

const handleForceLogout = (row: any) => {
  ElMessageBox.confirm(`确认强退用户 "${row.username}" 吗?`, '提示', { type: 'warning' }).then(async () => {
    await forceLogout(row.uuid)
    ElMessage.success('强退成功')
    handleQuery()
  })
}

const formatDate = (time: any) => {
    if (!time) return ''
  return dayjs(Number(time)).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  handleQuery()
})
</script>