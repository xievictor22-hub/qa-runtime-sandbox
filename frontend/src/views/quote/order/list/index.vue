<template>
  <div class="p-4 relative min-h-screen bg-gray-50">
    <div class="absolute top-4 right-6 z-10">
      <el-tag :type="wsStatusType" size="small" effect="plain" round>
        <span class="flex items-center">
          <span class="w-2 h-2 rounded-full mr-1" :class="isConnected ? 'bg-green-500' : 'bg-gray-400'"></span>
          {{ wsStatusText }}
        </span>
      </el-tag>
    </div>
    
    <el-card shadow="never">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="项目名称">
          <el-input v-model="queryParams.projectName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" value="0" />
            <el-option label="审核中" value="1" />
            <el-option label="业务调整" value="2" />
            <el-option label="已完成" value="3" />
            <el-option label="待重新报价" value="4" />
            <el-option label="业务重新调整中" value="5" />
            <el-option label="待重新报价(完结回退)" value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-badge :is-dot="hasNewData" class="mr-3">
            <el-button type="primary" icon="Search" @click="handleManualQuery">搜索</el-button>
          </el-badge>
          <el-button type="success" icon="Plus" @click="handleCreate">新建报价</el-button>
        </el-form-item>
      </el-form>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="mt-2">
        <el-tab-pane name="todo">
          <template #label>
            <span class="flex items-center">
              <el-icon class="mr-1"><User /></el-icon> 待我处理
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="all" label="全部报价"></el-tab-pane>
      </el-tabs>

      <div class="relative">
        <transition name="el-fade-in">
          <div 
            v-if="hasNewData" 
            class="absolute top-2 left-1/2 transform -translate-x-1/2 z-20 cursor-pointer"
            @click="handleManualQuery"
          >
            <el-alert
              type="info"
              effect="dark"
              :closable="false"
              class="!rounded-full !py-2 !px-6 shadow-lg hover:shadow-xl transition-all border border-blue-200"
            >
              <template #title>
                <div class="flex items-center gap-2">
                  <el-icon class="animate-bounce"><Bell /></el-icon>
                  <span class="font-bold">数据已更新，点击刷新</span>
                </div>
              </template>
            </el-alert>
          </div>
        </transition>
      <el-table :data="tableData" border v-loading="loading" class="mt-2">
        <el-table-column prop="id" label="单号" width="80" />
        <el-table-column prop="projectName" label="项目名称" min-width="180"  show-overflow-tooltip/>
        <el-table-column prop="customerName" label="客户名称" width="120" show-overflow-tooltip/>
        
        <el-table-column label="版本" width="140" align="center">
            <template #default="{ row }">
              <div class="flex flex-col gap-1 items-start pl-2">
                <el-tag size="small" type="info" effect="plain">报价 v{{ row.currentQuoteVersion }}</el-tag>
                <el-tag size="small" type="warning" effect="plain" v-if="row.currentBusinessVersion > 0">
                  业务 v{{ row.currentBusinessVersion }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
               <el-tag :type="statusType(row.status)" effect="dark">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>

        <el-table-column label="操作" width="260" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
              <el-button link type="warning" icon="Edit" @click="handleBusinessAdjust(row)" v-if="['2','5'].includes(row.status)">业务调整</el-button>
              <el-button link type="info" icon="Document" @click="handleLogs(row)">履历</el-button>
              <el-button link type="danger" icon="Delete" v-if="row.status === '0'" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
      </el-table>
      </div>
      <div class="mt-4 flex justify-end">
         </div>
      </el-card>
    <LogDrawer ref="logDrawerRef" />
    <el-dialog v-model="createVisible" title="新建报价单" width="500px">
      <el-form :model="form" label-width="80px" :rules="newOrderRules" ref="newOrderFormRef">
        <el-form-item label="项目名称" prop="projectName"><el-input v-model="form.projectName" /></el-form-item>
        <el-form-item label="客户名称"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item label="项目类型" prop="projectType" required>
          <el-select v-model="form.projectType" placeholder="请选择项目类型">
            <el-option label="家装" value="0" />
            <el-option label="工装" value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, onUnmounted, type Ref } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { listQuoteOrder, deleteQuoteOrder, createQuoteOrder, type QuoteOrder, type QuoteOrderQuery } from '@/api/quote/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import LogDrawer from '../detail/components/LogDrawer.vue' // 引入组件
import { useWebSocket } from '@/hooks/useWebSocket'

const router = useRouter()
const route = useRoute()
const logDrawerRef = ref()//履历信息
const loading = ref(false)
const tableData = ref<QuoteOrder[]>([])
const createVisible = ref(false)
const form = reactive({ projectName: '', customerName: '', projectType: undefined })
const newOrderFormRef = ref()

// 状态管理：新数据提醒 & 当前页签
const hasNewData = ref(false)
const activeTab  = ref<'all' | 'todo'>('todo')

const queryParams: QuoteOrderQuery = reactive({ 
  pageNum: 1, 
  pageSize: 20, 
  projectName: '', 
  status: '' ,
  queryType: 'todo' as 'all' | 'todo' // scope: 'all' | 'todo'
})

// WebSocket 相关
const { connect, subscribe, disconnect, isConnected } = useWebSocket()
const wsSubscription = ref<any>(null)

// 状态字典
const statusMap: {[key: string]: string} = { '0': '草稿', '1': '审核中', '2': '业务调整', '3': '已完成', '4': '待重新报价', '5': '重调中', '6': '待重新报价' }
const statusText = (s: string) => statusMap[s] || s
const statusType = (s: string) => ['3'].includes(s) ? 'success' : ['1','2','5'].includes(s) ? 'warning' : ['4','6'].includes(s) ? 'danger' : 'info'
const wsStatusText = computed(() => isConnected.value ? '服务已连接' : '服务断开')
const wsStatusType = computed(() => isConnected.value ? 'success' : 'info')

/** 刷新报价单列表 */
const handleQuery = async () => {
  loading.value = true
  try {
    const res: any = await listQuoteOrder(queryParams)
    tableData.value = res.records || []
    // 【关键】手动刷新成功后，熄灭红点和悬浮球
    hasNewData.value = false
  } finally {
    loading.value = false
  }
}
/** 手动点击搜索/刷新球 */
const handleManualQuery = () => {
  handleQuery()
}

/** 切换 Tab (全部/待办) */
const handleTabChange = (name: 'all' | 'todo') => {
  queryParams.queryType = name
  queryParams.pageNum = 1 // 切换Tab重置页码
  handleQuery()
}
/** 点击新建按钮 */
const handleCreate = () => { createVisible.value = true }

const newOrderRules = reactive({
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
})

/** 点击新建按钮 */
const submitCreate = async () => {
  // 校验表单
  const valid = await newOrderFormRef.value.validate()
  if (!valid) {
    return
  }
  await createQuoteOrder(form)
  ElMessage.success('创建成功')
  createVisible.value = false
  handleQuery() 
}
/** 点击详情按钮 */
const handleDetail = (row: QuoteOrder) => 
router.push({
  path: `/quote/order/detail/${row.id}`,
  query: { sourceTab: activeTab.value } // 传递当前 tab 信息
})

const handleBusinessAdjust = (row: QuoteOrder) =>
router.push({
  path: `/quote/business-adjust/${row.id}`,
  query: { sourceTab: activeTab.value }
})
/**
 * 点击删除按钮
 * @param row 报价单对象
 */
const handleDelete = (row: QuoteOrder) => {
  ElMessageBox.confirm('确认删除?', '提示').then(async () => {
    await deleteQuoteOrder(row.id)
    handleQuery()
  })
}
/** 打开履历抽屉 */
const handleLogs = (row: QuoteOrder) => {
  logDrawerRef.value.open(row.id)
}

/** 初始化 WebSocket (改为红点模式) */
const initWebSocket = () => {
  connect(
    () => {
      // 订阅公共广播
      wsSubscription.value = subscribe('/topic/quote-updates', (message: { body: any, destination: string }) => {
        try {
          // 兼容处理：body 可能是字符串也可能是对象(取决于hook实现)
          const data = typeof message.body === 'string' ? JSON.parse(message.body) : message.body
          const quoteId = data.quoteId
          const action = data.action
          
          if (quoteId) {
            console.log(`收到 WS 通知: ID=${quoteId}, Action=${action}`)
            
            // 判断是否需要提醒用户
            // 逻辑：如果这个单据在当前页面，或者是新增动作(ADD)，则提示有更新
            const index = tableData.value.findIndex(item => item.id === quoteId)
            
            // 简单判断：只要有更新就亮红点，不做复杂的数据比对
            if (index !== -1 || action === 'CREATE') {
              // 【修改点 4】不再自动请求接口，只是改变状态
              hasNewData.value = true
            }
          }
        } catch (error) {
          console.error('WS消息解析失败:', error)
        }
      })
    }
  )
}

// 组件挂载时初始化
onMounted(() => {
  if (route.query.tab) {
    activeTab.value = route.query.tab as 'all' | 'todo'
  } else {
    activeTab.value = 'all' as 'all' | 'todo'
  }
  queryParams.queryType = activeTab.value
  handleQuery()
  initWebSocket()
})

// 组件卸载时断开连接
onUnmounted(() => {
  if (wsSubscription.value) {
    wsSubscription.value.unsubscribe()
  }
  disconnect()
})
</script>