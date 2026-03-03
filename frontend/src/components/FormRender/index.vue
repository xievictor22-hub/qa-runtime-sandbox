<template>
  <div v-loading="loading">
    <form-create
      v-model:api="fApi"
      :rule="rule"
      :option="option"
    />
    
    <div class="mt-6 flex justify-center">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" @click="submitForm">提交申请</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps({
  // 后端返回的 formConf JSON 字符串
  conf: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['submit', 'cancel'])

const loading = ref(false)
// 显式声明为 any，告诉 TS "相信我，这个对象以后会有东西的"
const fApi = ref<any>({}) // 表单实例 API
const rule = ref([])
const option = ref({})

// 解析 JSON
const parseConf = (jsonStr: string) => {
  try {
    const data = JSON.parse(jsonStr)
    rule.value = data.rule || []
    option.value = data.option || {}
  } catch (e) {
    console.error('表单配置解析失败', e)
    rule.value = []
  }
}

watch(() => props.conf, (val) => {
  if (val) parseConf(val)
}, { immediate: true })

// 提交
const submitForm = () => {
  // fApi.validate 触发表单校验 (满足需求 2)
  fApi.value.validate((valid: boolean, fail: any) => {
    if (valid) {
      // 获取表单数据
      const formData = fApi.value.formData()
      emit('submit', formData)
    } else {
      console.log('校验失败', fail)
    }
  })
}
</script>