<template>
  <div class="flex flex-col h-full p-4 gap-4">
    <el-card shadow="never" class="shrink-0">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="归属部门" prop="deptId">
        <el-tree-select
            v-model="queryParams.deptId"
            :data="deptOptions"
            :props="{ value: 'id', label: 'deptName', children: 'children' }"
            value-key="id"
            placeholder="请选择归属部门"
            check-strictly
            filterable
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" v-permission="['system:user:query']" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-upload
        action="#"
        :http-request="handleImport"
        :show-file-list="false"
        accept=".xlsx, .xls"
        :before-upload="beforeUpload"
        :disabled="loading"
      >
        <el-button type="warning" plain icon="Upload" :loading="loading">
          导入
        </el-button>
      </el-upload>

      <el-button 
        type="success" 
        plain 
        icon="Download" 
        @click="handleExport" 
        :loading="loading"
      >
        导出
      </el-button>
        </el-form-item>
      </el-form>

      <div class="mt-2">
        <el-button type="primary" v-permission="['system:user:add']" :icon="Plus" @click="handleAdd">新增用户</el-button>
        <el-button type="danger" v-permission="['system:user:remove']" :icon="Delete" :disabled="ids.length === 0" @click="handleBatchDelete">批量删除</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="flex-1 min-h-0 flex flex-col" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column' }">  
      <div class="flex-1 min-h-0">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="height: 100%"
        class="flex-1"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="用户名" prop="username" min-width="60" />
        <el-table-column label="昵称" prop="nickname" min-width="60" />
        <el-table-column label="部门" prop="deptNamePath" min-width="250" />
        <el-table-column label="手机号" prop="phone" width="120" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="400" align="center" fixed="right">
          <template #default="scope">
            <el-button type="warning" v-permission="['system:user:edit']" link :icon="Key" @click="handleAuthRole(scope.row)">分配角色</el-button>
            <el-button type="primary" v-permission="['system:user:edit']" link :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" v-permission="['system:user:remove']" link :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" @close="resetForm">
      <el-form ref="userFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="归属部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptOptions"
            :props="{ value: 'id', label: 'deptName', children: 'children' }"
            value-key="id"
            placeholder="请选择归属部门"
            check-strictly
            filterable
            clearable
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname" v-if="form.id">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog title="分配角色" v-model="roleDialog.visible" width="500px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="roleDialog.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="roleDialog.checkedRoleIds">
            <el-checkbox 
              v-for="role in roleDialog.roleList" 
              :key="role.id" 
              :label="role.id"
            >
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitRoleAuth">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="SystemUserIndex">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance ,type UploadRequestOptions} from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, Key } from '@element-plus/icons-vue' // 如果报错，请确保安装了 icons
import { getUserList, addUser, updateUser, deleteUser, deleteBatchUser, type UserQuery, type UserForm, exportUserApi, importUserApi } from '@/api/system/user'
import { getAllRoles } from '@/api/system/role'
import { getUserRoleIds, assignUserRoles } from '@/api/system/user'
import { listDept } from '@/api/system/dept'
import { downloadBlob } from '@/utils/download';

// ★★★ 必须添加这个，且名字必须与 permission.ts 生成的一致 ★★★
// 规则：目录名+文件名 (大驼峰) -> System + User + Index = SystemUserIndex

// --- 状态定义 ---
const loading = ref(false)
const ids = ref<number[]>([]) // 选中的ID数组
const total = ref(0)
const tableData = ref([])
const userFormRef = ref<FormInstance>()
// 2. 【定义状态】 在 const userFormRef ... 下方添加
const deptOptions = ref<any[]>([]) // 部门树选项

// 查询参数
const queryParams = reactive<UserQuery>({
  pageNum: 1,
  pageSize: 10,
  username: '',
  phone: '',
  deptId: undefined, // 新增部门ID字段
  status: undefined
})

// 弹窗控制
const dialog = reactive({
  visible: false,
  title: ''
})

// 表单对象
const form = reactive<UserForm>({
  id: undefined,
  deptId: undefined, // 新增部门ID字段
  username: '',
  nickname: '',
  password: '',
  phone: '',
  status: 1
})

// 表单校验规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择归属部门', trigger: 'change' }], // 新增部门ID校验
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}
// --- 角色分配相关状态 ---
const roleDialog = reactive({
  visible: false,
  userId: undefined as number | undefined,
  username: '',
  roleList: [] as any[], // 所有可用角色
  checkedRoleIds: [] as number[] // 选中的角色ID
})

// --- 方法 ---

/**
 * 导出操作
 */
const handleExport = async () => {
  try {
    loading.value = true;
    ElMessage.info('正在生成 Excel，请稍候...');
    
    // 1. 调用 API 获取 Blob
    // 注意：这里传入查询参数 queryParams，实现带条件导出
    const response = await exportUserApi({ /* ...queryParams.value */ });
    
    // 2. 调用工具函数下载
    downloadBlob(response.data, `用户数据_${new Date().getTime()}.xlsx`);
    
    ElMessage.success('导出成功');
  } catch (error) {
    console.error(error);
    ElMessage.error('导出失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 导入前校验 (可选)
 */
const beforeUpload = (file: File) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || file.name.endsWith('.xlsx');
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isExcel) {
    ElMessage.error('只能上传 xlsx 格式的文件!');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('文件大小不能超过 5MB!');
    return false;
  }
  return true;
};

/**
 * 执行导入
 * options 包含了 file 对象
 */
const handleImport = async (options: UploadRequestOptions) => {
  try {
    loading.value = true;
    const { file } = options;
    
    // 调用封装的 API
    await importUserApi(file);
    
    ElMessage.success('导入成功');
    // 刷新列表
    // getList(); 
  } catch (error) {
    console.error(error);
    ElMessage.error('导入失败，请检查文件格式');
  } finally {
    loading.value = false;
  }
};

const getDeptTree = async () => {
  const res: any = await listDept()
  // 如果后端返回的是扁平列表，通常需要 handleTree转换，这里假设后端逻辑已优化为返回树
  // 或者你可以在前端做简单的处理，通常 RuoYi 风格的后端在 list 接口可能返回扁平数据
  // 这里我们直接赋值，依靠 el-tree-select 的能力（如果后端返回了 children 嵌套结构）
  deptOptions.value = res 
}

/** 打开分配角色弹窗 */
const handleAuthRole = async (row: any) => {
  roleDialog.userId = row.id
  roleDialog.username = row.username
  roleDialog.checkedRoleIds = []
  
  // 1. 并行查询：所有角色 + 用户当前角色
  const [allRolesRes, userRolesRes]: any = await Promise.all([
    getAllRoles(),
    getUserRoleIds(row.id)
  ])
  
  roleDialog.roleList = allRolesRes
  roleDialog.checkedRoleIds = userRolesRes
  
  roleDialog.visible = true
}

/** 提交角色分配 */
const submitRoleAuth = async () => {
  if (!roleDialog.userId) return
  await assignUserRoles(roleDialog.userId, roleDialog.checkedRoleIds)
  ElMessage.success('分配成功')
  roleDialog.visible = false
}
// --- 方法定义 ---

/** 查询列表 */
const handleQuery = async () => {
  loading.value = true
  try {
    const res: any = await getUserList(queryParams)
    // 根据后端返回结构调整，假设 ApiResponse.data 是 Page 对象
    // res 经过 axios 拦截器处理后已经是 data 部分
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

/** 重置查询 */
const handleReset = () => {
  queryParams.username = ''
  queryParams.phone = ''
  queryParams.status = undefined
  queryParams.deptId = undefined // 新增部门ID字段
  queryParams.pageNum = 1
  handleQuery()
}

/** 打开新增弹窗 */
const handleAdd = async () => {
  resetForm()
  await getDeptTree() // <--- 加载部门数据
  dialog.title = '新增用户'
  dialog.visible = true
}

/** 打开编辑弹窗 */
const handleEdit = async (row: any) => {
  resetForm()
  await getDeptTree() // <--- 加载部门数据
  dialog.title = '修改用户'
  dialog.visible = true
  // 浅拷贝数据填充表单
  Object.assign(form, row)
  form.password = '' // 编辑时不回显密码
}

/** 重置表单 */
const resetForm = () => {
  form.id = undefined
  form.deptId = undefined // 新增部门ID字段
  form.username = ''
  form.nickname = ''
  form.password = ''
  form.phone = ''
  form.status = 1
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

/** 提交表单 */
const submitForm = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await updateUser(form)
        ElMessage.success('修改成功')
      } else {
        form.nickname = form.username
        await addUser(form)
        ElMessage.success('新增成功')
      }
      dialog.visible = false
      handleQuery()
    }
  })
}

/** 删除单个用户 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`是否确认删除用户 "${row.username}"?`, '警告', {
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  })
}

/** 批量删除 */
const handleBatchDelete = () => {
  if (ids.value.length === 0) return
  ElMessageBox.confirm(`是否确认删除这 ${ids.value.length} 个用户?`, '警告', {
    type: 'warning'
  }).then(async () => {
    await deleteBatchUser(ids.value)
    ElMessage.success('删除成功')
    handleQuery()
  })
}

/** 表格多选 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map(item => item.id)
}

// --- 初始化 ---
onMounted(() => {
  handleQuery()
  getDeptTree() // <--- 加载部门数据
})
</script>