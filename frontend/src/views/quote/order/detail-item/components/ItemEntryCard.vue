<template>
  <el-card shadow="never" class="w-1/2 flex flex-col compact-card relative" :class="{ 'edit-highlight': isEditing }">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-bold text-gray-700">{{ modelValue.id ? '修改' : '新增项录入' }}</span>
        <div 
          @mouseenter="isHoveringMode = true" 
          @mouseleave="isHoveringMode = false"
          @click="modelValue.id && $emit('cancel-edit')"
          :class="{ 'cursor-pointer': modelValue.id }"
        >
          <el-tag 
            size="small" 
            class="transition-all duration-200 select-none"
            :effect="modelValue.id && isHoveringMode ? 'dark' : 'plain'"
            :type="modelValue.id ? (isHoveringMode ? 'danger' : 'primary') : 'info'"
          >
            {{ modelValue.id && isHoveringMode ? '退出编辑' : (modelValue.id ? '修改模式' : '新增模式') }}
          </el-tag>
        </div>
      </div>
    </template>
    
    <div class="flex-1 overflow-auto p-1">
      <el-form :model="modelValue" :rules="rules" label-width="60px" size="small" ref="formRef">
        <div class="grid grid-cols-4 gap-y-0.5 gap-x-1">
          <el-form-item label="项目1" prop="process1" class="!mb-0">
            <el-select 
              v-model="modelValue.process1" 
              clearable
              filterable 
              allow-create 
              default-first-option
              @change="handleP1Change"
            >
               <el-option v-for="opt in p1Options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="项目2" prop="process2" class="!mb-0">
            <el-select 
              v-model="modelValue.process2" 
              clearable
              filterable 
              allow-create 
              :disabled="!modelValue.process1"
              @change="handleP2Change"
            >
               <el-option v-for="opt in p2Options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="项目3" prop="process3" class="!mb-0">
             <el-select 
              v-model="modelValue.process3" 
              clearable
              filterable 
              allow-create 
              :disabled="!modelValue.process2"
              @change="handleP3Change"
            >
               <el-option v-for="opt in p3Options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="项目4" prop="process4" class="!mb-0">
             <el-select 
              v-model="modelValue.process4" 
              clearable
              filterable 
              allow-create 
              :disabled="!modelValue.process3"
            >
               <el-option v-for="opt in p4Options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
        </div>

        <div class="grid grid-cols-4 gap-x-2 gap-y-0.5 mt-0.5">
          <el-form-item label="数量" prop="quantity" class="!mb-0">
            <el-input-number v-model.number="modelValue.quantity" :controls="false" class="w-full" :precision="4" />
          </el-form-item>
          <el-form-item label="单价" prop="distPrice" class="!mb-0">
            <el-input-number v-model.number="modelValue.distPrice" :controls="false" class="w-full" :precision="2" ref="priceInputRef" />
          </el-form-item>
          <el-form-item label="单位" prop="unit" class="!mb-0">
            <el-input v-model="modelValue.unit" />
          </el-form-item>
          <div class="flex gap-2 justify-end items-center">
            <el-button size="small" type="danger" @click="$emit('reset')">清空</el-button>
            <el-button 
              :type="modelValue.id ? 'warning' : 'primary'" 
              size="small" 
              :icon="modelValue.id ? 'Edit' : 'Plus'" 
              @click="handleSubmit"
            >
              {{ modelValue.id ? '修改' : '新增' }}
            </el-button>
          </div>
        </div>

        <el-form-item label="备注" prop="remark" class="mt-0.5 !mb-0">
          <el-input v-model="modelValue.remark"  type="textarea" :rows="3" />
        </el-form-item>

        <div class="bg-blue-50 p-1 rounded border border-blue-100 mt-1">
           <el-form-item label="计算器" class="!mb-0.5">
              <el-input ref="calcInputRef" 
              v-model="calcExpression" 
              placeholder="输入算式 (如 10+20+5) 回车计算;CTRL+回车添加括号" 
              @keydown.enter.exact.prevent="handleCalculate"
              @keydown.ctrl.enter.prevent="handleInsertParentheses"
              @keydown.alt.enter.prevent="handleSubmit"
              clearable>
                  <template #append><el-button @click="handleCalculate">计算</el-button></template>
              </el-input>
           </el-form-item>
           <el-form-item label="刀数" class="!mb-0">
                  <div class="flex gap-2">
                    <el-input 
                      v-model="knifeExpression" 
                      placeholder="输入折弯算式统计加号数量" 
                      @keyup.enter="handleKnifeCount"
                    />
                    <el-input 
                      v-model="knifeResult" 
                      readonly 
                      placeholder="结果" 
                      style="width: 80px" 
                      class="text-center font-bold text-blue-600"
                    />
                  </div>
                </el-form-item>
        </div>
      </el-form>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useCalculator } from '../composables/useCalculator'
import type { FormInstance, FormRules } from 'element-plus'
// 假设有这个 API 获取全量树
import { getProcessTreeApi, ProcessNode } from '@/api/quote/base'
import '../style/cartStyle.css'
import { QuoteDetailItemFormDto } from '@/api/quote/item'

const props = defineProps<{
  modelValue: QuoteDetailItemFormDto // QuoteDetailItemFormDto
  isEditing: boolean
  projectType: number
  version: string
}>()

const emit = defineEmits(['update:modelValue', 'submit', 'reset', 'cancel-edit'])

const formRef = ref<FormInstance>()
const isHoveringMode = ref(false)
const priceInputRef = ref()
const processTree = ref<ProcessNode[]>([])

// 引入计算器
const { calcExpression, calcInputRef, handleCalculate, resetCalculator
  , knifeExpression, knifeResult, handleKnifeCount,handleInsertParentheses
 } = useCalculator(props.modelValue)

// === 级联核心逻辑 ===
const p1Options = computed(() => processTree.value || [])
const p2Options = computed(() => {
  const node = processTree.value.find(n => n.value === props.modelValue.process1)
  return node?.children || []
})
const p3Options = computed(() => {
  const node = p2Options.value.find(n => n.value === props.modelValue.process2)
  return node?.children || []
})
const p4Options = computed(() => {
  const node = p3Options.value.find(n => n.value === props.modelValue.process3)
  return node?.children || []
})

// 级联清空
const handleP1Change = () => {
  props.modelValue.process2 = ''
  props.modelValue.process3 = ''
  props.modelValue.process4 = ''
}
const handleP2Change = () => {
  props.modelValue.process3 = ''
  props.modelValue.process4 = ''
}
const handleP3Change = () => {
  props.modelValue.process4 = ''
}

// 1. 封装获取逻辑
const fetchProcessTree = async () => {
  // 【关键守卫】：如果没有版本号或项目类型，直接不查，防止报错
  if (!props.version || props.projectType === -1) {
    console.log("参数未就绪，等待父级数据...")
    return
  }

  try {
    console.log("参数已就绪，开始加载树", props.projectType, props.version)
    const queryParams = {
      projectType: props.projectType,
      version: props.version
    }
    const res = await getProcessTreeApi(queryParams)
    processTree.value = res || []
  } catch(e) { 
    console.error(e) 
  }
}
// 2. 监听 props 变化
// immediate: true 保证了：
//   a. 如果挂载时就有数据，立即执行（替代 onMounted）
//   b. 如果挂载时没数据，等数据来了再执行
watch(
  () => [props.version, props.projectType],
  () => {
    fetchProcessTree()
  },
  { immediate: true } 
)


const rules = reactive<FormRules>({
  process1: [{ required: true, message: '必填', trigger:'change' }], // 改为 change
  unit: [{ required: true, message: '必填', trigger: 'blur' }],
  quantity: [{ required: true, message: '必填', trigger: 'blur' }],
  distPrice: [{ required: true, message: '必填', trigger: 'blur' }],
})

const handleSubmit = async () => {
  if (!formRef.value) return
  const params = {
    ...props.modelValue,

  }
  await formRef.value.validate((valid) => {
    if (valid) emit('submit', params)
  })
}

const clearCalculator = () => resetCalculator()
defineExpose({ clearCalculator })
</script>

<style scoped>
.edit-highlight {
  border: 2px solid #E6A23C !important;
  box-shadow: 0 0 10px rgba(230, 162, 60, 0.4);
}
</style>