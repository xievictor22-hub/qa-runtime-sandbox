<template>
  <div class="p-6">
    <el-card shadow="never" header="钉盘文件上传测试">
      
      <el-alert
        title="上传说明"
        type="info"
        description="文件将直接上传至配置的钉盘空间与文件夹下。上传成功后返回 fileId。"
        show-icon
        class="mb-6"
      />

      <div class="flex flex-col items-center justify-center p-10 border-2 border-dashed border-gray-300 rounded-lg bg-gray-50">
        
        <el-upload
          class="upload-demo w-full max-w-lg"
          drag
          action="#" 
          :http-request="handleUpload"
          :show-file-list="true"
          :before-upload="beforeUpload"
          :limit="1"
          :on-exceed="handleExceed"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处 或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip text-center">
              单文件大小不超过 500MB
            </div>
          </template>
        </el-upload>

        <div v-if="resultFileId" class="mt-6 w-full max-w-lg">
          <el-alert type="success" :closable="false" show-icon>
            <template #title>
              <span class="font-bold">上传成功！</span>
            </template>
            <div class="mt-2">
              <span class="text-gray-600 mr-2">File ID:</span>
              <el-tag type="warning" class="font-mono">{{ resultFileId }}</el-tag>
              
              <el-button 
                link 
                type="primary" 
                class="ml-2" 
                @click="copyId"
              >
                复制ID
              </el-button>
            </div>
          </el-alert>
        </div>

      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, type UploadRequestOptions, type UploadRawFile } from 'element-plus'
import { uploadFile } from '@/api/system/dingDrive'
import { useClipboard } from '@vueuse/core' // 如果你有用 VueUse，没有可以用原生 API

const resultFileId = ref('')
const loading = ref(false)

// 1. 自定义上传逻辑
const handleUpload = async (options: UploadRequestOptions) => {
  const { file } = options
  loading.value = true
  
  const formData = new FormData()
  formData.append('file', file)

  try {
    // 调用 API
    const res = await uploadFile(formData)
    
    // 根据你的 ApiResponse 结构判断
    if (res.code === 200) {
      ElMessage.success('上传成功')
      resultFileId.value = res.data // 获取后端返回的 fileId
    } else {
      ElMessage.error(res.message || '上传失败')
      // 可以在这里调用 options.onError() 让文件列表显示红色失败状态
    }
  } catch (error: any) {
    console.error(error)
    // 这里的 error.message 可能是后端 GlobalExceptionHandler 返回的 JSON
    ElMessage.error(error.message || '网络或服务器异常')
  } finally {
    loading.value = false
  }
}

// 2. 上传前校验
const beforeUpload = (rawFile: UploadRawFile) => {
  // 示例：限制 500MB
  if (rawFile.size / 1024 / 1024 > 500) {
    ElMessage.error('文件大小不能超过 500MB!')
    return false
  }
  return true
}

// 3. 超出限制
const handleExceed = () => {
  ElMessage.warning('每次只能上传一个文件，请删除后再试')
}

// 4. 复制功能 (使用 VueUse 或 原生)
const { copy } = useClipboard()
const copyId = () => {
  if (resultFileId.value) {
    copy(resultFileId.value)
    ElMessage.success('复制成功')
  }
}
</script>