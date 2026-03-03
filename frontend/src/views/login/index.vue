<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white p-8 rounded-lg shadow-lg w-96">
      <h2 class="text-2xl font-bold mb-6 text-center text-gray-800">MoGo 管理系统</h2>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            placeholder="请输入密码" 
            :prefix-icon="Lock"
            show-password
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button 
          type="primary" 
          class="w-full mt-4" 
          :loading="loading" 
          size="large"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '立即登录' }}
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts" name="LoginIndex">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/auth/user'
import { ElMessage, type FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue' // 需确保已安装图标库

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin', // 默认填好方便测试
  password: '123456'  // 假设你也把密码改成 admin 了，或者填 123456
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        router.push('/dashboard') // 登录成功跳转
      } catch (error) {
        // 错误已经在 axios 拦截器处理过了，这里可以不做额外处理
      } finally {
        loading.value = false
      }
    }
  })
}
</script>