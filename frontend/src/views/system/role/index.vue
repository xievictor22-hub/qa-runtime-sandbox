<template>
  <div class="p-5">
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="queryParams.roleKey" placeholder="请输入权限字符" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="mt-2">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="角色名称" prop="roleName" min-width="120" />
        <el-table-column label="权限字符" prop="roleKey" min-width="120" />
        <el-table-column label="显示顺序" prop="sort" width="100" align="center" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="success" :icon="CircleCheck" @click="handleMenuScope(scope.row)">菜单权限</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" @close="resetForm">
      <el-form ref="roleFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="例如：超级管理员" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="例如：admin" />
        </el-form-item>
        
        <el-form-item label="显示顺序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数据权限">
          <el-select v-model="form.dataScope" @change="handleDataScopeChange">
            <el-option label="全部数据权限" value="1" />
            <el-option label="自定义数据权限" value="2" />
            <el-option label="本部门数据权限" value="3" />
            <el-option label="本部门及以下数据权限" value="4" />
            <el-option label="仅本人数据权限" value="5" />
          </el-select>
        </el-form-item>

        <el-form-item v-show="form.dataScope === '2'" label="数据范围">
          <el-tree
            class="tree-border"
            ref="deptTreeRef"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            :expand-all="deptExpand"
            node-key="id"
            empty-text="加载中..."
            :props="{ label: 'deptName', children: 'children' }"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog title="分配菜单权限" v-model="permDialog.visible" width="500px">
      <el-form label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="permDialog.roleName" disabled />
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            ref="menuTreeRef"
            :data="menuOptions"
            :props="{ label: 'menuName', children: 'children' }"
            show-checkbox
            node-key="id"
            default-expand-all
            check-strictly
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="permDialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitPermForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="SystemRoleIndex">
import { ref, reactive, onMounted } from 'vue'
import { listDept } from '@/api/system/dept'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, type RoleQuery, type RoleForm,getRoleDeptIds } from '@/api/system/role'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
// 引入 Element 的 Tree 实例类型
import { ElTree } from 'element-plus'
// 引入 API
import { listMenu } from '@/api/system/menu'
import { getRoleMenuIds, assignRoleMenus } from '@/api/system/role'
// 引入图标
import { CircleCheck } from '@element-plus/icons-vue'

// --- 状态 ---
const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const roleFormRef = ref<FormInstance>()

const queryParams = reactive<RoleQuery>({
  pageNum: 1,
  pageSize: 10,
  roleName: '',
  roleKey: '',
  status: undefined
})

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive<RoleForm>({
  id: undefined,
  roleName: '',
  roleKey: '',
  sort: 0,
  status: 1,
  remark: '',
  dataScope: '', // 新增
  deptIds: [] // 新增
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入顺序', trigger: 'blur' }]
}

// --- 权限分配相关 ---
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const menuOptions = ref([]) // 菜单树数据
const deptTreeRef = ref<InstanceType<typeof ElTree>>()
const deptOptions = ref([]) // 存储部门树数据
const deptExpand = ref(true) // 是否展开，可选


const permDialog = reactive({
  visible: false,
  roleId: undefined as number | undefined,
  roleName: ''
})

/** 打开权限分配弹窗 */
const handleMenuScope = async (row: any) => {
  permDialog.roleId = row.id
  permDialog.roleName = row.roleName
  
  // 1. 获取所有菜单树 (如果还没加载过)
  if (menuOptions.value.length === 0) {
    const res: any = await listMenu() // 获取全量菜单树
    menuOptions.value = res
  }
  
  permDialog.visible = true

  // 2. 获取该角色已有的菜单ID
  const checkedIds: any = await getRoleMenuIds(row.id)
  
  // 3. 回显勾选状态
  // nextTick 确保 Tree 组件已经渲染
  setTimeout(() => {
    if (menuTreeRef.value) {
      // 重点：使用 setCheckedKeys 设置勾选
      // 注意：check-strictly设为true，是为了回显时，如果后端只返回了部分子节点，不会因为父子联动导致所有子节点都被勾选
      menuTreeRef.value.setCheckedKeys(checkedIds, false)
    }
  }, 100)
}

/** 提交权限分配 */
const submitPermForm = async () => {
  if (!permDialog.roleId || !menuTreeRef.value) return
  
  // 1. 获取全选的节点
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  // 2. 获取半选的节点 (父节点) -> 这一点非常重要！
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  
  // 3. 合并所有ID
  const finalIds = [...checkedKeys, ...halfCheckedKeys]
  
  await assignRoleMenus(permDialog.roleId, finalIds as number[])
  ElMessage.success('授权成功')
  permDialog.visible = false
}

// --- 方法 ---
/** 获取部门树数据 */
const getDeptTree = async () => {
  const res: any = await listDept()
  deptOptions.value = res // 根据你实际API返回结构调整，可能是 res.data 或 res
}

/** 数据权限下拉框变化 */
const handleDataScopeChange = (value: string) => {
  // 如果选择了自定义(2)且树没数据，就加载树
  if (value === '2' && deptOptions.value.length === 0) {
    getDeptTree()
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res: any = await getRoleList(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.roleName = ''
  queryParams.roleKey = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  handleQuery()
}

const handleAdd = () => {
  resetForm()
  dialog.title = '新增角色'
  dialog.visible = true
}

/** 编辑角色 */
const handleEdit = async (row: any) => {
  resetForm()
  dialog.title = '修改角色'
  dialog.visible = true
  Object.assign(form, row)
  // 2. 特殊处理：如果是自定义权限，需要回显部门树
  if (form.dataScope === '2') {
    // 确保树已加载
    if (deptOptions.value.length === 0) {
      await getDeptTree()
    }
    // 获取后端存储的已选部门
    const res: any = await getRoleDeptIds(row.id)
    // 等待DOM更新后勾选
    setTimeout(() => {
      deptTreeRef.value?.setCheckedKeys(res) // res.data 是 ID 数组
    }, 100)
  }
}

const resetForm = () => {
  form.id = undefined
  form.roleName = ''
  form.roleKey = ''
  form.sort = 0
  form.status = 1
  form.remark = ''
  form.dataScope = '1'
  form.deptIds = []
  if (deptTreeRef.value) {
    deptTreeRef.value.setCheckedKeys([])
  }
  if (roleFormRef.value) {
    roleFormRef.value.resetFields()
  }
}

const submitForm = async () => {
  if (!roleFormRef.value) return
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      // 1. 处理自定义权限的部门ID
      if (form.dataScope === '2' && deptTreeRef.value) {
        form.deptIds = deptTreeRef.value?.getCheckedKeys() as number[]
      }else{
        form.deptIds = []
      }
      if (form.id) {
        await updateRole(form)
        ElMessage.success('修改成功')
      } else {
        await addRole(form)
        ElMessage.success('新增成功')
      }
      dialog.visible = false
      handleQuery()
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`是否确认删除角色 "${row.roleName}"?`, '警告', {
    type: 'warning'
  }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  })
}

// --- 初始化 ---
onMounted(() => {
  handleQuery()
})
</script>