<template>
  <el-dialog
    v-model="visible"
    title="导入报价底表"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" ref="formRef" :rules="rules" label-width="90px">
      <el-form-item label="版本号" prop="version">
        <el-input 
          v-model="form.version" 
          placeholder="如 A0018 (重复将覆盖)" 
        >
        <template #prepend>
          A
        </template>
        </el-input>
      </el-form-item>
      
      <el-form-item label="发布说明" prop="description">
        <el-input 
          v-model="form.description" 
          type="textarea" 
          :rows="2" 
          placeholder="请输入本次更新内容..."
        />
      </el-form-item>
      
      <el-form-item label="文件" prop="file">
        <el-upload
          ref="uploadRef"
          action="#"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx, .xls"
          :on-change="handleFileChange"
          :on-remove="() => selectedFile = null"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">拖拽或点击上传</div>
        </el-upload>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定导入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, type FormInstance, type UploadFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { importBasePrice } from '@/api/quote/base'

const emit = defineEmits(['success'])
const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const uploadRef = ref()
const selectedFile = ref<File | null>(null)

const form = reactive({
  version: '',
  description: ''
})

const rules = {
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  file: [{ 
    required: true, 
    validator: (_: any, __: any, cb: any) => selectedFile.value ? cb() : cb(new Error('请选择文件')), 
    trigger: 'change' 
  }]
}

const open = () => {
  visible.value = true
  form.version = ''
  form.description = ''
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

const handleFileChange = (file: UploadFile) => {
  selectedFile.value = file.raw as File
  formRef.value?.validateField('file')
}

const handleClose = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid && selectedFile.value) {
      try {
        loading.value = true
        await importBasePrice({ file: selectedFile.value, ...form })
        ElMessage.success('导入成功')
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