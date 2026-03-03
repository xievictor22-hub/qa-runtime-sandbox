<template>
  <el-dialog
    v-model="visible"
    title="新增底价条目"
    width="680px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" ref="formRef" :rules="rules" label-width="110px">
      <el-form-item label="目标版本" prop="version">
        <el-input 
          v-model="form.version" 
          placeholder="请输入版本号 (如 A0018)" 
          clearable
        />
      </el-form-item>

      <el-divider content-position="left">基础信息</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="项目类型" prop="projectType">
            <el-select v-model="form.projectType" placeholder="请选择" style="width: 100%">
              <el-option label="家装 (0)" :value="0" />
              <el-option label="工装 (1)" :value="1" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单位" prop="unit">
            <el-select v-model="form.unit" placeholder="请选择或输入自定义单位" style="width: 100%" filterable allow-create>
              <el-option label="m" value="m" />
              <el-option label="㎡" value="㎡" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="单价" prop="unitPrice">
            <el-input-number 
              v-model="form.unitPrice" 
              :min="0" 
              :precision="2" 
              style="width: 100%" 
              placeholder="请输入单价"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="损耗系数" prop="quoteFormula">
            <el-input-number 
              v-model="form.quoteFormula" 
              :precision="4" 
              :step="0.01" 
              style="width: 100%" 
              placeholder="默认1.0"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="计入折件" prop="isFolding">
        <el-select v-model="form.isFolding" placeholder="是否计入折件计算" style="width: 100%">
          <el-option label="否 (0)" :value="0" />
          <el-option label="类型 1" :value="1" />
          <el-option label="类型 2" :value="2" />
          <el-option label="类型 3" :value="3" />
          <el-option label="类型 4" :value="4" />
        </el-select>
      </el-form-item>

      <el-divider content-position="left">工艺层级 (Process)</el-divider>

      <el-form-item label="项目1" prop="process1">
        <el-input v-model="form.process1" placeholder="一级分类" />
      </el-form-item>
          <el-form-item label="项目2" prop="process2" label-width="60px">
            <el-input v-model="form.process2" />
          </el-form-item>
          <el-form-item label="项目3" prop="process3" label-width="60px">
            <el-input v-model="form.process3" />
          </el-form-item>
          <el-form-item label="项目4" prop="process4" label-width="60px">
            <el-input v-model="form.process4" />
          </el-form-item>
      

      <el-divider content-position="left">范围参数 (选填)</el-divider>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="刀数范围值 C" prop="rangeValC">
            <el-input :disabled="form.isFolding === 0" v-model="form.rangeValC" placeholder="刀数范围" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="宽度范围值 W" prop="rangeValW">
            <el-input :disabled="form.isFolding === 0"  v-model="form.rangeValW" placeholder="宽范围" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="板厚范围值 D" prop="rangeValD">
            <el-input :disabled="form.isFolding === 0" v-model="form.rangeValD" placeholder="厚范围" />
          </el-form-item>
        </el-col>
      </el-row>
      
       <el-form-item label="打点系数" prop="pointCoefficient">
            <el-input-number 
              v-model="form.pointCoefficient" 
              :precision="2" 
              :step="0.1" 
              style="width: 100%" 
            />
       </el-form-item>

    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        提交
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { addBasePriceItem } from '@/api/quote/base'

const emit = defineEmits(['success'])
const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive({
  version: '',
  projectType: 0, // 默认家装
  process1: '',
  process2: '',
  process3: '',
  process4: '',
  unit: '',
  unitPrice: 0,
  quoteFormula: 1.0, // 默认系数
  isFolding: 0, // 默认否
  rangeValC: '',
  rangeValW: '',
  rangeValD: '',
  pointCoefficient: 1.0
})

// 校验规则
const rules = {
  version: [{ required: true, message: '版本号不能为空', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  process1: [{ required: true, message: '项目1不能为空', trigger: 'blur' }],
  unit: [{ required: true, message: '单位不能为空', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '单价不能为空', trigger: 'blur' }],
  quoteFormula: [{ required: true, message: '损耗系数不能为空', trigger: 'blur' }],
  isFolding: [{ required: true, message: '请选择是否计入折件', trigger: 'change' }]
}

/**
 * 打开弹窗
 * @param options 可选，配置选项
 * @param options.defaultVersion 可选，从列表页带入当前选中的版本
 * @param options.data 可选，预填的表单数据
 */
const open = (options?: { defaultVersion?: string, data?: any }) => {
  visible.value = true
  resetForm()
  
  // 设置默认版本
  if (options?.defaultVersion) {
    form.version = options.defaultVersion
  }
  
  // 预填表单数据（排除 id）
  if (options?.data) {
    Object.keys(form).forEach(key => {
      if (key !== 'id' && options.data[key] !== undefined) {
        (form as any)[key] = options.data[key]
      }
    })
  }
}

const resetForm = () => {
  form.version = ''
  form.projectType = 0
  form.process1 = ''
  form.process2 = ''
  form.process3 = ''
  form.process4 = ''
  form.unit = ''
  form.unitPrice = 0
  form.quoteFormula = 1.0
  form.isFolding = 0
  form.rangeValC = ''
  form.rangeValW = ''
  form.rangeValD = ''
  form.pointCoefficient = 1.0
  
  // 清除校验结果
  if (formRef.value) {
     formRef.value.clearValidate()
  }
}

const handleClose = () => {
  // 关闭时的逻辑，Reset 已在 Open 时处理，这里可留空
}



const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.isFolding === 0) {
          form.rangeValC = ''
          form.rangeValW = ''
          form.rangeValD = ''
        }
        loading.value = true
        // 提取 version，剩下的作为 body
        const { version, ...bodyData } = form
        
        await addBasePriceItem(bodyData, version)
        
        ElMessage.success('新增成功')
        visible.value = false
        emit('success')
      } catch (e) {
        console.error(e)
      } finally {
        loading.value = false
      }
    }
  })
}

defineExpose({ open })
</script>