<template>
  <div class="p-5">
    <el-row :gutter="20">
      <el-col :span="8" :xs="24">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>个人信息</span>
            </div>
          </template>
          <div class="text-center">
            <el-upload
              class="avatar-uploader"
              action="#"
              :http-request="customUpload"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
            >
              <div class="avatar-wrapper relative">
                <img v-if="user.avatar" :src="user.avatar" class="user-avatar" />
                <img v-else src="@/assets/logo.png" class="user-avatar" /> <div class="upload-mask">
                  <el-icon class="text-white text-2xl"><Camera /></el-icon>
                </div>
              </div>
            </el-upload>
          </div>
          
          <ul class="list-group list-group-striped mt-4 text-sm text-gray-600">
            <li class="list-group-item">
              <div class="flex items-center"><el-icon class="mr-2"><User /></el-icon>用户名称</div>
              <div>{{ user.username }}</div>
            </li>
            <li class="list-group-item">
              <div class="flex items-center"><el-icon class="mr-2"><Phone /></el-icon>手机号码</div>
              <div>{{ user.phone }}</div>
            </li>
            <li class="list-group-item">
              <div class="flex items-center"><el-icon class="mr-2"><Message /></el-icon>用户邮箱</div>
              <div>{{ user.email }}</div>
            </li>
            <li class="list-group-item">
              <div class="flex items-center"><el-icon class="mr-2"><OfficeBuilding /></el-icon>所属部门</div>
              <div>{{ user.dept ? user.dept.deptName : '暂无' }}</div>
            </li>
            <li class="list-group-item">
              <div class="flex items-center"><el-icon class="mr-2"><Calendar /></el-icon>创建日期</div>
              <div>{{ user.createTime }}</div>
            </li>
          </ul>
        </el-card>
      </el-col>

      <el-col :span="16" :xs="24">
        <el-card>
          <template #header>
            <div class="clearfix">
              <span>基本资料</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="userinfo">
              <el-form ref="userFormRef" :model="user" :rules="rules" label-width="80px">
                <el-form-item label="用户昵称" prop="nickname">
                  <el-input v-model="user.nickname" maxlength="30" />
                </el-form-item>
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="user.phone" maxlength="11" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="user.email" maxlength="50" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="user.sex">
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitUserInfo">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="修改密码" name="resetPwd">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" placeholder="请输入旧密码" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" placeholder="请输入新密码" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" placeholder="请确认新密码" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitPwd">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="UserProfile">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { Camera, User, Phone, Message, Calendar, OfficeBuilding } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, updateUserPwd, updateUserAvatar } from '@/api/system/profile'
import { uploadFile } from '@/api/oss/file'
import { useUserStore } from '@/stores/auth/user'

const userStore = useUserStore()
const activeTab = ref('userinfo')
const user = ref<any>({})
const userFormRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  phone: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取信息
const getProfile = async () => {
  const res: any = await getUserProfile()
  user.value = res
}

// 提交基本信息
const submitUserInfo = async () => {
  if (!userFormRef.value) return
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      await updateUserProfile(user.value)
      ElMessage.success('修改成功')
      // 同步更新 Pinia，让右上角名字立即变化
      userStore.nickname = user.value.nickname
    }
  })
}

// 提交修改密码
const submitPwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      await updateUserPwd(pwdForm.oldPassword, pwdForm.newPassword)
      ElMessage.success('修改成功')
      // 清空表单
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    }
  })
}

// --- 头像上传 ---
const beforeAvatarUpload = (file: any) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
  if (!isLt2M) ElMessage.error('上传头像图片大小不能超过 2MB!')
  return isJPG && isLt2M
}

const customUpload = async (options: any) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    // 1. 上传到 MinIO
    const res: any = await uploadFile(formData)
    const avatarUrl = res.url
    
    // 2. 更新用户表中的头像URL
    user.value.avatar = avatarUrl
    await updateUserAvatar(avatarUrl)
    
    // 3. 更新 Pinia
    userStore.avatar = avatarUrl
    
    ElMessage.success('头像修改成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('上传失败')
  }
}

onMounted(() => {
  getProfile()
})
</script>

<style scoped>
.list-group {
  padding-left: 0;
  list-style: none;
}
.list-group-item {
  border-bottom: 1px solid #e7eaec;
  border-top: 1px solid #e7eaec;
  margin-bottom: -1px;
  padding: 11px 0;
  font-size: 13px;
  display: flex;
  justify-content: space-between;
}
/* 头像容器 */
.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}
.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
/* 悬停遮罩 */
.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .upload-mask {
  opacity: 1;
}
</style>