<template>
  <el-card shadow="never" class="flex-shrink-0 compact-card bg-orange-50 border-orange-100" v-loading="loading">
    <el-form :model="modelValue" size="small" label-width="70px" ref="formRef">
      <div class="grid grid-cols-7 gap-x-1 gap-y-0">
        <el-form-item label="区域/户型"><el-input v-model="modelValue.projectArea" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="modelValue.position" /></el-form-item>
        <el-form-item label="产品名称"><el-input v-model="modelValue.productName" class="font-bold" /></el-form-item>
        <el-form-item label="图纸代号"><el-input v-model="modelValue.materialCode" /></el-form-item>
        <el-form-item label="立面图号"><el-input v-model="modelValue.elevationNo" /></el-form-item>
        <el-form-item label="节点图号"><el-input v-model="modelValue.nodeNo" /></el-form-item>
        
        <el-form-item label="材料"><el-input v-model="modelValue.material" /></el-form-item>
        <el-form-item label="厚度"><el-input v-model="modelValue.thickness" /></el-form-item>
        <el-form-item label="型号"><el-input v-model="modelValue.model" /></el-form-item>
        <el-form-item label="表面工艺"><el-input v-model="modelValue.color" /></el-form-item>
        <el-form-item label="规格/尺寸"><el-input v-model="modelValue.spec" /></el-form-item>
        
        <el-form-item label="宽度(展)"><el-input-number v-model="modelValue.width" :controls="false" class="w-full" /></el-form-item>
        <el-form-item label="长度"><el-input-number v-model="modelValue.length" :controls="false" class="w-full" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="modelValue.unit" /></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="modelValue.quantity" :controls="false" class="w-full font-bold text-red-600" /></el-form-item>
        <el-form-item label="报价版本"><el-input v-model="modelValue.version" disabled /></el-form-item>
        <el-form-item label="序号"><el-input-number v-model="modelValue.rowNum" :controls="false" class="w-full" /></el-form-item>
        
        <el-form-item label="备注" class="col-span-3 !mb-0">
          <el-input type="textarea" v-model="modelValue.remarkDesc" placeholder="技术备注" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="handleSave">保存</el-button>
        </el-form-item>
      </div>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ParentInfo } from '@/api/quote/detail';
import '../style/cartStyle.css'

const props = defineProps<{
  modelValue: ParentInfo // QuoteDetailVO
  loading: boolean
}>()
const emit = defineEmits(['update:modelValue', 'save'])
const formRef = ref()

const handleSave = async () => {
  // 可以加简单的校验
  if (!formRef.value) return
  await formRef.value.validate()
  emit('save')
}
</script>