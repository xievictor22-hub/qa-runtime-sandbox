<template>
  <el-drawer v-model="visible" title="报价单履历" size="400px" append-to-body>
    <el-timeline v-if="logs.length > 0">
      <el-timeline-item
        v-for="(activity, index) in logs"
        :key="index"
        :type="getLogType(activity.action)"
        :color="getLogColor(activity.action)"
        :timestamp="activity.createTime"
        placement="top"
      >
        <el-card shadow="hover" class="border-none">
          <h4 class="font-bold text-gray-800">{{ formatAction(activity.action) }}</h4>
          <p class="text-sm mt-1">
            <span class="text-gray-500">操作人: </span>
            <span class="font-medium">{{ activity.operatorName || '系统' }}</span>
          </p>
          <p class="text-sm mt-1">
            <span class="text-gray-500">接收人: </span>
            <span class="font-medium">{{ activity.receiverName || '系统' }}</span>
          </p>
          <p class="text-sm mt-1" v-if="activity.comment">
            <span class="text-gray-500">备注: </span>
            {{ activity.comment }}
          </p>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="暂无履历数据" />
  </el-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getQuoteLogs } from '@/api/quote/order'

const visible = ref(false)
const logs = ref<any[]>([])

// 字典映射
const actionMap: any = {
  CREATE: '创建报价单',
  SUBMIT_AUDIT: '提交审核',
  AUDIT_PASS: '审核通过',
  AUDIT_REJECT: '审核驳回',
  CREATE_VERSION: '创建新版本',
  COMPLETED: '确认完成',
  RE_OPEN: '重新调整'
}

const open = async (quoteId: string) => {
  visible.value = true
  logs.value = []
  const res = await getQuoteLogs(quoteId)
  logs.value = res || []
}

const formatAction = (action: string) => actionMap[action] || action

const getLogType = (action: string) => {
  if (['AUDIT_REJECT'].includes(action)) return 'danger'
  if (['COMPLETED', 'AUDIT_PASS'].includes(action)) return 'success'
  if (['SUBMIT_AUDIT'].includes(action)) return 'primary'
  return 'info'
}
const getLogColor = (action: string) => {
  if (action === 'CREATE') return '#909399'
  return '' // default
}

defineExpose({ open })
</script>