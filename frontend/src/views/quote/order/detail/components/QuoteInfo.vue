<template>
  <el-card shadow="never">
    <div class="flex justify-between items-center">
      <div class="flex items-center gap-2">
        <el-button size="small" icon="ArrowLeft" circle @click="handleBack" />
        <span class="text-lg font-bold text-gray-800">{{ info.projectName }}</span>
        <el-tag :type="statusType" size="small" effect="dark">{{ statusText }}</el-tag>
        <span class="text-xs text-gray-500 ml-2">
          报价 v{{ info.currentQuoteVersion }} / 业务 v{{ info.currentBusinessVersion }}
        </span>
      </div>
      
      <div class="space-x-2 flex items-center">
        <el-button size="small" @click="$emit('toggle-other-cost')">其他费用</el-button>
        <el-button size="small" @click="$emit('toggle-summary')">费用合计</el-button>
        
        
        <el-button v-if="hasPerm('submit')" size="small" type="primary" icon="Promotion" @click="dialogType = 'submit'">提交</el-button>
        <el-button v-if="hasPerm('new-version')" size="small" icon="CopyDocument" @click="handleNewVersion">新版本</el-button>

        <template v-if="hasPerm('audit-pass')">
          <el-button size="small" type="success" icon="Check" @click="dialogType = 'pass'">通过</el-button>
          <el-button size="small" type="danger" icon="Close" @click="dialogType = 'reject'">驳回</el-button>
        </template>

        <el-button v-if="hasPerm('finish')" size="small" type="primary" icon="Stamp" @click="dialogType = 'finish'">完成</el-button>
        <el-button v-if="hasPerm('reopen')" size="small" type="warning" icon="RefreshLeft" @click="handleReOpen">重调</el-button>
        
        <el-button size="small" icon="Document" @click="openLogs">履历</el-button>
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px" append-to-body>
        <el-form label-width="80px" :model="form" :rules="formRules" ref="formRef">
          <el-form-item label="处理人" prop="nextHandlerId" v-if="userOptions.length > 0">
            <el-select v-model="form.nextHandlerId" placeholder="请选择处理人" style="width: 100%">
              <el-option 
                v-for="user in userOptions" 
                :key="user.id" 
                :label="user.nickname" 
                :value="user.id" 
              >
                <div class="flex flex-col">
                  <span class="font-medium">{{ user.nickname }}<span class="ml-2 text-xs text-gray-400">{{ user.roleNames || '无角色' }}</span></span>
                  
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="auditComment">
            <el-input v-model="form.auditComment" type="textarea" :rows="3" placeholder="请输入备注信息" />
          </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="dialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleSubmit">提交</el-button>
            </span>
          </template>
        </el-dialog>
    <LogDrawer ref="logDrawerRef" />
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router' // 导入路由
import { useTagsViewStore } from '@/stores/system/tagsView' // 导入标签页管理
import LogDrawer from '../components/LogDrawer.vue' // 引入组件
import { submitAudit, auditPass, auditReject, createNewVersion,getProcessUsers,ProcessUser } from '@/api/quote/process'
import { finishBusiness, reAdjustBusiness } from '@/api/quote/business'

const route = useRoute()
const tagsViewStore = useTagsViewStore()


const props = defineProps<{ info: any }>()
const emit = defineEmits(['refresh','back','toggle-other-cost','toggle-summary'])

// 弹窗表单
const formRef = ref()
const userOptions = ref<ProcessUser[]>([]) // 候选人列表

// 表单验证规则
const formRules = reactive({
  nextHandlerId: [
    { required: true, message: '请选择处理人', trigger: 'change' }
  ],
  auditComment: [
    { required: false, message: '请输入备注', trigger: 'blur' }
  ]
})
// 履历查看
const logDrawerRef = ref()
const openLogs = () => {
  logDrawerRef.value.open(props.info.id)
}

// 修改返回按钮的点击事件处理
const handleBack = () => {
  // 1. 删除当前标签页
  tagsViewStore.delView(route)

  // 3. 通知父组件（可选）
  emit('back')
}

// 【核心】通用权限判断方法
const hasPerm = (code: string) => {
  return props.info.actionPermissions?.includes(code)
}

// 状态UI
const statusMap: {[key: string]: string} = { '0': '报价中', '1': '审核中', '2': '业务调整', '3': '已完成', '4': '重新报价中', '5': '业务重调中', '6': '重新报价中'}
const statusText = computed(() => statusMap[props.info.status] || props.info.status)
const statusType = computed(() => ['3'].includes(props.info.status) ? 'success' : ['4','6'].includes(props.info.status) ? 'danger' : 'warning')

// 弹窗控制
const dialogType = ref('') // submit, pass, reject, finish
const dialogVisible = computed({
  get: () => !!dialogType.value,
  set: (val) => { if(!val) dialogType.value = '' }
})
const dialogTitle = computed(() => {
  const map: {[key: string]: string} = { submit: '提交审核', pass: '审核通过', reject: '审核驳回', finish: '业务确认完成' }
  return map[dialogType.value]
})

const form = reactive({ nextHandlerId: undefined, auditComment: '' })


// 统一提交处理
const handleSubmit = async () => {
  // 校验表单
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
  const payload = { 
    id: props.info.id, 
    nextHandlerId: form.nextHandlerId, 
    auditComment: form.auditComment 
  }
  
  if (dialogType.value === 'submit') await submitAudit(payload)
  else if (dialogType.value === 'pass') await auditPass(payload)
  else if (dialogType.value === 'reject') await auditReject(payload)
  else if (dialogType.value === 'finish') await finishBusiness(payload)
  
  ElMessage.success('操作成功')
  dialogVisible.value = false
  emit('refresh') // 刷新主状态
}
  })
}

// 独立动作：创建新版本
const handleNewVersion = () => {
  ElMessageBox.confirm('确定创建新版本吗？这将复制当前版本数据并允许编辑。', '提示').then(async () => {
    await createNewVersion(props.info.id)
    ElMessage.success('新版本创建成功')
    emit('refresh')
  })
  }

// 独立动作：重开
const handleReOpen = () => {
  ElMessageBox.confirm('确定要重新调整吗？这将创建新的业务版本。', '警告', { type: 'warning' }).then(async () => {
    await reAdjustBusiness(props.info.id)
    ElMessage.success('已进入重新调整模式')
    emit('refresh')
  })
}

// 加载用户
const loadUsers = async (roleKey: string) => {
  try {
    const res = await getProcessUsers(roleKey)
    userOptions.value = res
  } catch (e) {
    ElMessage.error('加载用户失败')
    // 容错：如果API没通，可以给个空列表或提示
  }
}
  
  // 监听弹窗类型变化，加载对应的候选人
watch(dialogType, async (val) => {
  if (val) {
    // 重置表单
    form.nextHandlerId = undefined
    form.auditComment = ''
    userOptions.value = []
    
    // 根据动作加载不同角色的用户
    if (val === 'submit') {
      // 提交审核 -> 找“审核员” (假设角色key是 auditor)
      await loadUsers('quote_auditor') 
    } else if (val === 'pass') {
      // 审核通过 -> 找“业务员” (假设角色key是 business)
      // 或者找创建人自己？这取决于业务。通常是流转给业务经理或具体业务员。
      await loadUsers('quote_sales') 
    }
  }
})
</script>