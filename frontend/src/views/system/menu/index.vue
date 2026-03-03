<template>
  <div class="p-5">
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="菜单名称">
          <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Plus" type="primary" plain @click="handleAdd()">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
        height="calc(100vh - 280px)"
      >
        <el-table-column prop="menuName" label="菜单名称" :show-overflow-tooltip="true" width="180" />
        <el-table-column prop="icon" label="图标" align="center" width="80">
          <template #default="scope">
            <el-icon v-if="scope.row.icon">
              <component :is="scope.row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="perms" label="权限标识" :show-overflow-tooltip="true" />
        <el-table-column prop="component" label="组件路径" :show-overflow-tooltip="true" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="primary" :icon="Plus" @click="handleAdd(scope.row)">新增</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="680px" @close="resetForm">
      <el-form ref="menuFormRef" :model="form" :rules="rules" label-width="100px">
        
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="menuOptions"
            :props="{ value: 'id', label: 'menuName', children: 'children' }"
            value-key="id"
            placeholder="选择上级菜单"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio label="M">目录</el-radio>
            <el-radio label="C">菜单</el-radio>
            <el-radio label="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="菜单图标" prop=":icon" v-if="form.menuType !== 'F'">
          <el-input v-model="form.icon" placeholder="请输入 Element :icon 名称 (如: User)" />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="sort">
              <el-input-number v-model="form.sort" controls-position="right" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12" v-if="form.menuType !== 'F'">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="form.path" placeholder="请输入路由地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType === 'C'">
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="form.component" placeholder="请输入组件路径" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="权限字符" prop="perms" v-if="form.menuType !== 'M'">
          <el-input v-model="form.perms" placeholder="例如: system:user:add" />
        </el-form-item>

        <el-row>
          <el-col :span="12" v-if="form.menuType !== 'F'">
            <el-form-item label="显示状态">
              <el-radio-group v-model="form.visible">
                <el-radio :label="1">显示</el-radio>
                <el-radio :label="0">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="SystemMenuIndex">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { listMenu, addMenu, updateMenu, delMenu, type MenuForm, type MenuQuery } from '@/api/system/menu'
import { Search, Plus, Delete, Edit } from '@element-plus/icons-vue'

// --- 状态定义 ---
const loading = ref(false)
const menuList = ref<any[]>([]) // 列表数据
const menuOptions = ref<any[]>([]) // 树形下拉选项
const menuFormRef = ref<FormInstance>()

const queryParams = reactive<MenuQuery>({
  menuName: undefined,
  status: undefined
})

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive<MenuForm>({
  id: undefined,
  parentId: 0,
  menuType: 'M',
  icon: '',
  menuName: '',
  sort: 0,
  path: '',
  component: '',
  perms: '',
  visible: 1,
  status: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由地址', trigger: 'blur' }]
}

// --- 方法 ---

/** 查询菜单列表 */
const handleQuery = async () => {
  console.log("开始查询")
  loading.value = true
  try {
    const res: any = await listMenu(queryParams)
    console.log("查询结果:", res)
    menuList.value = res // 后端已经构建好了 children 树
  } finally {
    loading.value = false
  }
}

/** 获取菜单树 (用于下拉选择) */
const getMenuTreeselect = async () => {
  const res: any = await listMenu()
  // 添加一个根节点选项
  const menu: any = { id: 0, menuName: '主类目', children: [] }
  menu.children = res
  menuOptions.value = [menu]
}

/** 新增 */
const handleAdd = (row?: any) => {
  resetForm()
  getMenuTreeselect() // 刷新下拉树
  if (row != null && row.id) {
    form.parentId = row.id // 如果是从行内点击新增，默认选中该行作为父级
  } else {
    form.parentId = 0
  }
  dialog.title = '新增菜单'
  dialog.visible = true
}

/** 修改 */
const handleUpdate = async (row: any) => {
  resetForm()
  await getMenuTreeselect() // 刷新下拉树
  Object.assign(form, row)
  dialog.title = '修改菜单'
  dialog.visible = true
}

/** 删除 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`是否确认删除名称为"${row.menuName}"的菜单项?`, '警告', {
    type: 'warning'
  }).then(async () => {
    await delMenu(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  })
}

/** 提交表单 */
const submitForm = async () => {
  if (!menuFormRef.value) return
  await menuFormRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await updateMenu(form)
        ElMessage.success('修改成功')
      } else {
        await addMenu(form)
        ElMessage.success('新增成功')
      }
      dialog.visible = false
      handleQuery()
    }
  })
}

const resetForm = () => {
  form.id = undefined
  form.parentId = 0
  form.menuType = 'M'
  form.icon = ''
  form.menuName = ''
  form.sort = 0
  form.path = ''
  form.component = ''
  form.perms = ''
  form.visible = 1
  form.status = 1
  if (menuFormRef.value) {
    menuFormRef.value.resetFields()
  }
}

// --- 初始化 ---
onMounted(() => {
  handleQuery()
})
</script>