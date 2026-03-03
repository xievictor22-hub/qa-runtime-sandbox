<template>
  <div>
    <template v-if="unmatch && showValue">
      {{ value }}
    </template>
    
    <template v-else>
      <template v-for="(item, index) in values" :key="index">
        <el-tag
          v-if="item.elTagType"
          :disable-transitions="true"
          :type="item.elTagType"
          :class="item.cssClass"
          style="margin-right: 5px;"
        >
          {{ item.label }}
        </el-tag>
        
        <span
          v-else
          :class="item.cssClass"
          style="margin-right: 5px;"
        >
          {{ item.label }}
        </span>
      </template>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

// 定义 Props
const props = defineProps({
  // 字典数组 (通常由 useDict 返回)
  options: {
    type: Array as any,
    default: null,
  },
  // 当前的值 (可能是 Number, String, 或者逗号分隔的 String)
  value: [Number, String, Array],
  // 当没匹配到时，是否显示原始 value
  showValue: {
    type: Boolean,
    default: true,
  }
});

/**
 * 核心逻辑：计算当前 value 对应的字典项列表
 */
const values = computed(() => {
  if (props.value === null || typeof props.value === 'undefined' || props.options === null) {
    return [];
  }
  
  // 1. 将传入的值转为数组 (处理 "1,2,3" 这种情况)
  const valueArr = String(props.value).split(',');
  
  const result: any[] = [];
  
  // 2. 遍历 options 寻找匹配项
  props.options.forEach((item: any) => {
    // 注意：这里使用 == 而不是 ===，为了兼容 string("1") 和 number(1)
    if (valueArr.includes(String(item.value))) {
      result.push({
        label: item.label,
        value: item.value,
        elTagType: item.elTagType || item.listClass || '', // 兼容不同字段名
        cssClass: item.cssClass || ''
      });
    }
  });
  
  return result;
});

// 判断是否未匹配到任何项
const unmatch = computed(() => {
  return values.value.length === 0;
});
</script>

<style scoped>
.el-tag {
  margin-left: 2px;
}
</style>