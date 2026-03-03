<template>
  <div class="flex flex-col h-full p-4 ">
    <el-card shadow="never" class="mb-2">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="部门名称">
          <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Plus" type="primary" plain @click="handleAdd()" v-permission="['system:dept:add']">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="flex-1 min-h-0 flex flex-col" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column' }">  
    <div class="flex-1 min-h-0">
      <el-table
        v-loading="loading"
        :data="deptList"
        row-key="id"
        default-expand-all
        style="height: 100%"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
        :row-class-name="tableRowClassName" 
      >
        <el-table-column prop="deptName" label="部门名称" width="260" />
        <el-table-column prop="orderNum" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="200" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-permission="['system:dept:edit']">修改</el-button>
            <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-permission="['system:dept:add']">新增</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-permission="['system:dept:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </el-card>

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px" @close="resetForm">
      <el-form ref="deptFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptOptions"
            :props="{ value: 'id', label: 'deptName', children: 'children' }"
            value-key="id"
            placeholder="选择上级部门"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="负责人" prop="leaderId">
          <el-select 
            v-model="form.leaderId" 
            placeholder="请选择负责人" 
            filterable 
            clearable
            style="width: 100%"
            @change="handleLeaderChange"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="item.username"
              :value="item.id"
            >
              <span style="float: left">{{ item.username }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ item.dept ? item.dept.deptName : '' }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
        </el-form-item>
        <el-form-item label="部门状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="SystemDeptIndex">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { listDept, addDept, updateDept, delDept } from '@/api/system/dept'
import { listUserOption } from '@/api/system/user' // 引入接口

const loading = ref(false)
const deptList = ref<any[]>([])
const deptOptions = ref<any[]>([])
const deptFormRef = ref<FormInstance>()
const userOptions = ref<any[]>([]) // 存储用户下拉列表

const queryParams = reactive({
  deptName: undefined,
  status: undefined
})

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive<any>({
  id: undefined,
  parentId: 0,
  deptName: '',
  orderNum: 0,
  leaderId: undefined, // 修改：使用 leaderId
  leaderName: '',      // 显示用，提交时不一定要传
  phone: '',           // 这些如果是自动关联带出的，可以设为只读
  email: '',
  status: 1
})

const rules = {
  parentId: [{ required: true, message: '上级部门不能为空', trigger: 'blur' }],
  deptName: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }],
  orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }]
}

// 【新增】加载用户列表
const getUserList = async () => {
  const res: any = await listUserOption()
  userOptions.value = res
}

// 【新增】当选择负责人后，自动填充 电话和邮箱 (可选优化)
const handleLeaderChange = (val: any) => {
  const user = userOptions.value.find(item => item.id === val)
  if (user) {
    // form.phone = user.phone
    // form.email = user.email
    // form.leaderName = user.nickName // 如果需要
  }
}

// 新增：定义行类名的方法
const tableRowClassName = ({ row }: { row: any }) => {
  // 判断条件：如果有 children 且不为空，视为父级部门，给予吸附效果
  // 你也可以根据 row.parentId === 0 来只让顶级部门吸附
  if (row.children && row.children.length > 0) {
    return 'sticky-row'
  }
  return ''
}

const handleQuery = async () => {
  loading.value = true
  const res: any = await listDept(queryParams)
  deptList.value = res
  loading.value = false
}

const getDeptTree = async () => {
  const res: any = await listDept()
  const dept: any = { id: 0, deptName: 'MOGO科技', children: [] }
  dept.children = res
  deptOptions.value = [dept]
}

const resetForm = () => {
  form.id = undefined
  form.parentId = 0
  form.deptName = ''
  form.orderNum = 0
  form.leader = ''
  form.phone = ''
  form.email = ''
  form.status = 1
  if (deptFormRef.value) deptFormRef.value.resetFields()
}

const handleAdd = async (row?: any) => {
  resetForm()
  await getDeptTree()
  if (row != null && row.id) {
    form.parentId = row.id
  } else {
    form.parentId = 0
  }
  dialog.title = '新增部门'
  dialog.visible = true
}

const handleUpdate = async (row: any) => {
  resetForm()
  await getDeptTree()
  Object.assign(form, row)
  dialog.title = '修改部门'
  dialog.visible = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`是否确认删除名称为"${row.deptName}"的部门?`, '警告', { type: 'warning' }).then(async () => {
    await delDept(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  })
}

const submitForm = async () => {
  if (!deptFormRef.value) return
  await deptFormRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await updateDept(form)
        ElMessage.success('修改成功')
      } else {
        await addDept(form)
        ElMessage.success('新增成功')
      }
      dialog.visible = false
      handleQuery()
    }
  })
}

onMounted(() => {
  handleQuery()
  getUserList() // 加载用户列表
})
</script>

<style scoped>
/* 核心吸附样式 */
:deep(.el-table__body-wrapper tr.sticky-row) {
  position: sticky;
  top: 0;
  z-index: 10; /* 保证在普通行之上 */
  /* 必须设置背景色，否则吸附时文字会重叠，透明的 */
  background-color: var(--el-bg-color); 
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05); /* 加一点阴影区分层次 */
}

/* 确保单元格也有背景色，防止透明穿透 */
:deep(.el-table__body-wrapper tr.sticky-row td) {
  background-color: var(--el-bg-color-overlay); /* 使用 Element 的变量适配暗黑模式 */
}

/* 鼠标悬停时保持颜色一致，避免吸附时闪烁 */
:deep(.el-table__body-wrapper tr.sticky-row:hover > td) {
  background-color: var(--el-table-row-hover-bg-color) !important;
}
</style>