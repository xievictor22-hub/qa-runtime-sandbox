<template>
  <div class="p-2 bg-gray-50  flex flex-col h-full overflow-hidden formula-page">
    
    <el-card shadow="never" class="mb-2 flex-shrink-0 !border-none compact-card" ref="formCardRef" >
      <div class="flex justify-between items-center h-8">
        <div class="flex items-center gap-2">
          <el-button icon="ArrowLeft" circle size="small" @click="handleBack" />
          <span class="font-bold text-gray-700 text-lg">组价明细</span>
          <el-tag type="info" size="small" v-if="detailId">ID: {{ detailId }}</el-tag>
        </div>
        <div class="flex gap-2">
          <el-button size="small" @click="initData">刷新页面</el-button>
        </div>
      </div>
    </el-card>

    <div class="flex flex-col flex-1 min-h-0 gap-2" >
      
      <div class="flex gap-2 h-[250px] flex-shrink-0">
        
        <el-card shadow="never" class="w-1/2 flex flex-col compact-card relative" :class="{ 'edit-highlight': isEditing }">
          <template #header>
            <div class="flex justify-between items-center">
              <span class="font-bold text-gray-700">{{itemForm.id ? '修改' : '新增项录入'}}</span>
              <div 
                @mouseenter="isHoveringMode = true" 
                @mouseleave="isHoveringMode = false"
                @click="itemForm.id && handleCancelEdit()"
                :class="{ 'cursor-pointer': itemForm.id }"
              >
                <el-tag 
                  size="small" 
                  class="transition-all duration-200 select-none"
                  :effect="itemForm.id && isHoveringMode ? 'dark' : 'plain'"
                  :type="itemForm.id ? (isHoveringMode ? 'danger' : 'primary') : 'info'"
                >
                  <template v-if="itemForm.id && isHoveringMode">
                    <div class="flex items-center gap-1">
                      <el-icon><CircleClose /></el-icon>
                      <span>退出编辑</span>
                    </div>
                  </template>
                  <template v-else>
                    当前模式: {{ itemForm.id ? '修改' : '新增' }}
                  </template>
                </el-tag>
              </div>
            </div>
          </template>
          
          <div class="flex-1 overflow-auto p-2">
            <el-form :model="itemForm"  
            :rules="itemFormRules" label-width="60px" size="small" ref="itemFormRef">
              <div class="grid grid-cols-4  gap-y-1">
                <el-form-item label="项目1" prop="process1">
                  <el-input v-model="itemForm.process1" placeholder="类别 (如: 制作材料)" tabindex="1" />
                </el-form-item>
                <el-form-item label="项目2" prop="process2">
                  <el-input v-model="itemForm.process2" placeholder="名称 (如: 短管201)" tabindex="2" />  
                </el-form-item>
                <el-form-item label="项目3" prop="process3">
                  <el-input v-model="itemForm.process3" placeholder="规格 (如: 10*30)" tabindex="3" />
                </el-form-item>
                <el-form-item label="项目4" prop="process4">
                  <el-input v-model="itemForm.process4" placeholder="材质/厚度" tabindex="4" />
                </el-form-item>
              </div>
              <div class="grid grid-cols-4 gap-x-2 gap-y-1 mt-1">
                <el-form-item label="数量" prop="quantity">
                  <el-input-number  v-model.number="itemForm.quantity" placeholder="请输入数量" :controls="false" class="w-full" :precision="4" />
                </el-form-item>
                <el-form-item label="单价" prop="distPrice">
                  <el-input-number v-model.number="itemForm.distPrice" placeholder="请输入单价" :controls="false" class="w-full" :precision="2" />
                </el-form-item>
                <el-form-item label="单位" prop="unit">
                  <el-input v-model="itemForm.unit" placeholder="件/m/m²" />
                </el-form-item>
                <div class="flex gap-2 justify-end">
                <el-button  size="small" type="danger" @click="handleResetForm()">清空</el-button>
                <el-button 
                  :type="itemForm.id ? 'warning' : 'primary'" 
                  size="small" 
                  :icon="itemForm.id ? 'Edit' : 'Plus'" 
                  @click="handleSubmit(itemFormRef)"
                >
                  {{ itemForm.id ? '修改' : '新增' }}
                </el-button>
                </div>
              </div>

              <el-form-item label="备注" prop="remark" class="mt-1">
                <el-input v-model="itemForm.remark" type="textarea" :rows="2" placeholder="备注信息" />
              </el-form-item>

              <div class="bg-blue-50 p-2 rounded border border-blue-100 mt-2">
                <el-form-item label="计算器" class="!mb-1">
                  <el-input 
                    ref="calcInputRef"
                    v-model="calcExpression" 
                    placeholder="输入算式 (如 10+20+5) 回车计算;CTRL+回车添加括号" 
                    @keydown.enter.exact.prevent="handleCalculate"
                    @keydown.ctrl.enter.prevent="handleInsertParentheses"
                    clearable
                  >
                    <template #append>
                      <el-button @click="handleCalculate">计算</el-button>
                    </template>
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

        <el-card shadow="never" class="w-1/2 flex flex-col compact-card">
          <template #header>
            <div class="flex justify-between items-center gap-2">
              <span class="font-bold text-gray-700 flex-shrink-0">底价库</span>
              
              <div class="flex gap-1 flex-1">
                 <el-input v-model="libraryQuery.keyword" placeholder="搜名称/规格..." size="small" clearable @keyup.enter="loadLibrary" />
                 <el-select 
                    v-model="libraryQuery.version"
                    placeholder="请选择版本"
                    clearable
                    filterable
                    class="w-40"
                  >
                    <el-option
                      v-for="item in versionOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>    
                 <el-button type="primary" icon="Search" size="small" @click="loadLibrary">查</el-button>
              </div>
            </div>
          </template>

          <el-table 
            :data="libraryData" 
            border 
            stripe 
            height="100%" 
            size="small" 
            highlight-current-row
            @row-click="handleLibraryPick"
            class="cursor-pointer select-none"
            v-loading="libraryLoading"
          >
            <el-table-column width="35" align="center">
              <template #default>
                <el-icon class="text-gray-400"><Select /></el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="process1" label="项目1" width="80" show-overflow-tooltip />
            <el-table-column prop="process2" label="项目2" width="90" show-overflow-tooltip />
            <el-table-column prop="process3" label="项目3" width="90" show-overflow-tooltip />
            <el-table-column prop="process4" label="项目4" min-width="90" show-overflow-tooltip />
            <el-table-column prop="unitPrice" label="分销价" width="70" align="right"/>
            <el-table-column prop="unit" label="单位" width="50" align="center" />
          </el-table>
        </el-card>
      </div>

      <el-card 
        shadow="never" 
        class="flex-shrink-0 compact-card bg-orange-50 border-orange-100 parent-form-card"
        v-loading="parentLoading"
      >
        <el-form :model="parentInfo" size="small" label-width="70px" ref="detailFormRef">
          <div class="grid grid-cols-7 gap-x-2 gap-y-1">
            
            <el-form-item label="区域/户型">
              <el-input v-model="parentInfo.projectArea" />
            </el-form-item>
            <el-form-item label="位置">
              <el-input v-model="parentInfo.position" />
            </el-form-item>
            <el-form-item label="产品名称">
              <el-input v-model="parentInfo.productName" class="font-bold" />
            </el-form-item>
            <el-form-item label="图纸材料代号" label-width="auto">
              <el-input v-model="parentInfo.materialCode" placeholder="材料代号" />
            </el-form-item>
            <el-form-item label="立面图号">
              <el-input v-model="parentInfo.elevationNo" />
            </el-form-item>
            <el-form-item label="节点图号">
              <el-input v-model="parentInfo.nodeNo" />
            </el-form-item>

            <el-form-item label="材料">
              <el-input v-model="parentInfo.material" />
            </el-form-item>
            <el-form-item label="厚度">
              <el-input v-model="parentInfo.thickness" />
            </el-form-item>
            <el-form-item label="型号">
              <el-input v-model="parentInfo.model" />
            </el-form-item>
            <el-form-item label="表面工艺">
              <el-input v-model="parentInfo.color" />
            </el-form-item>
            <el-form-item label="规格/尺寸" >
              <el-input v-model="parentInfo.spec" />
            </el-form-item>

            <el-form-item label="宽度(展)">
              <el-input-number v-model="parentInfo.width" :controls="false" class="w-full" placeholder="展开mm" />
            </el-form-item>
            <el-form-item label="长度">
              <el-input-number v-model="parentInfo.length" :controls="false" class="w-full" placeholder="mm" />
            </el-form-item>
            <el-form-item label="单位">
              <el-input v-model="parentInfo.unit" />
            </el-form-item>
            <el-form-item label="数量">
              <el-input-number v-model="parentInfo.quantity" :controls="false" class="w-full font-bold text-red-600" />
            </el-form-item>
            <el-form-item label="报价版本">
              <el-input v-model="parentInfo.version" disabled placeholder="自动生成" />
            </el-form-item>
            <el-form-item label="序号">
              <el-input-number v-model="parentInfo.rowNum" :controls="false" class="w-full" />
            </el-form-item>

            <el-form-item label="备注" class="col-span-3 !mb-0">
              <el-input type="textarea" v-model="parentInfo.remarkDesc" placeholder="技术备注" />
            </el-form-item>
            <el-form-item >
              <el-button
                type="primary"
                size="small"
                @click="handleUpdateDetail"
              >
                保存
              </el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-card>

      <el-card shadow="never" class="flex-1 min-h-0 flex flex-col compact-card">
        <template #header>
           <div class="flex justify-between items-center h-6">
             <div class="flex items-center gap-2">
               <span class="font-bold">材料明细 (BOM)</span>
               <el-tag type="info" size="small">{{ filteredItems.length }} 项</el-tag>
             </div>
             <div class="text-sm">
               合计成本: <span class="text-red-600 font-bold text-lg font-mono mr-1">{{ totalAmount }}</span> 元
             </div>
           </div>
        </template>
        
        <div class="flex flex-col flex-1 min-h-0 h-full">
          <div class="flex gap-2 p-2 border-b bg-gray-50 items-center flex-shrink-0">
            <el-input v-model="searchParams.process1" placeholder="搜项目1(类别)" size="small" clearable style="width: 100px;margin-left: 40px;" />
            <el-input v-model="searchParams.process2" placeholder="搜项目2(名称)" size="small" clearable style="width: 105px" />
            <el-input v-model="searchParams.process3" placeholder="搜项目3(规格)" size="small" clearable style="width: 130px" />
            <el-input v-model="searchParams.process4" placeholder="搜项目4(材质)" size="small" clearable style="width: 100px" />
            <el-input v-model="searchParams.distPrice" placeholder="搜分销价" size="small" clearable style="width: 75px" />
            <el-input v-model="searchParams.unit" placeholder="搜单位" size="small" clearable style="width: 50px" />
            <el-input v-model="searchParams.quantity" placeholder="搜数量" size="small" clearable style="width: 75px" />
            <el-input v-model="searchParams.totalPrice" placeholder="搜汇总价" size="small" clearable style="width: 120px" />
            <el-input v-model="searchParams.remark" placeholder="搜备注" size="small" clearable style="width: 120px" />
            <el-button type="primary" icon="Search" size="small" @click="handleSearch">搜索</el-button>
            <el-button icon="Refresh" size="small" @click="handleResetSearch">重置</el-button>
          </div>

          <div class="flex-1 min-h-0 ">
            <el-table :data="displayItems" border stripe height="100%" size="small">
              <el-table-column type="index" width="50" align="center">
                <template #default="scope">
                  {{ (pagination.current - 1) * pagination.size + scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column prop="process1" label="项目1(类别)" width="100" show-overflow-tooltip />
              <el-table-column prop="process2" label="项目2(名称)" width="120" show-overflow-tooltip />
              <el-table-column prop="process3" label="项目3(规格)" width="140" show-overflow-tooltip />
              <el-table-column prop="process4" label="项目4(材质)" width="100" show-overflow-tooltip />
              <el-table-column prop="distPrice" label="分销价" width="90" align="right"/>
              <el-table-column prop="unit" label="单位" width="60" align="center" />
              <el-table-column prop="quantity" label="数量" width="90" align="center" class-name="bg-gray-50 font-bold" />
              <el-table-column prop="totalPrice" label="汇总价" width="100" align="right"/>
              <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
              <el-table-column label="操作" width="150" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link icon="Edit" @click="handleEditItem(row)">编辑</el-button>
                  <el-button type="danger" link icon="Delete" @click="handleRemove(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="flex justify-end p-1 border-t flex-shrink-0 bg-white">
            <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :page-sizes="[9, 20, 50, 100]"
              layout="total, sizes, prev, pager, next"
              :total="filteredItems.length"
              size="small"
              background
            />
          </div>
        </div>
      </el-card>
      
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick,onMounted,onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTagsViewStore } from '@/stores/system/tagsView' // 请根据实际项目路径调整
import { ElMessage } from 'element-plus'
// 模拟 API 导入，实际开发请解开注释
import { listDetailItems, addDetailItem, updateDetailItem, deleteDetailItem,type QuoteDetailItemVO,type QuoteDetailItemAddDto,type QuoteDetailItemUpdateDto, QuoteDetailItemFormDto } from '@/api/quote/item'
import { ParentInfo, selectQuoteDetail,updateQuoteDetail } from '@/api/quote/detail'
import { getBasePriceVersions } from '@/api/quote/base'
import { FormRules,FormInstance } from 'element-plus'

// import { listBasePriceLibrary } from '@/api/quote/base'
//通用
const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()// 引入 tagsViewStore 用于更新标签


const versionOptions = ref<{label:string,value:string}[]>([])// 版本选项
//组价明细模块
//新增/编辑组价明细
const getInitItemForm = ():QuoteDetailItemFormDto => ({// 初始化组价明细表单
  id: '',
  unit: '',
  quantity: undefined,
  process1: '',
  process2: '',
  process3: '',
  process4: '',
  distPrice: undefined,
  remark: '',
  quoteId: undefined,
  detailId: undefined,
  sourceLibraryId: '',
  version: '',
  formulaSnapshot: 1,//损耗
})
const itemForm = reactive<QuoteDetailItemFormDto>(getInitItemForm())// 组价明细表单数据
const itemFormRef = ref<FormInstance>()// 组价明细表单引用

const itemFormRules = reactive<FormRules<QuoteDetailItemFormDto>>({
  process1: [{ required: true, message: '请输入项目1(类别)', trigger:'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type:'number',validator:(rule:any, value:number, callback:any) => {
      if (value <= 0) {
        callback(new Error('数量必须大于0'))
      } else {
        callback()
      }
    }, trigger: 'blur' },
],
  distPrice: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type:'number', min:0,  message: '单价不能小于0', trigger: 'blur' },
],
})

//-辅助功能
const calcExpression = ref('')// 计算器表达式
const knifeExpression = ref('')// 切分表达式
const knifeResult = ref('')// 切分结果
const calcInputRef = ref()// 计算器输入框引用
const isHoveringMode = ref(false)// 是否悬停在当前模式按钮上
const formCardRef = ref()// 表单卡片引用
const isEditing = ref(false)// 是否处于编辑模式

// 数量计算器计算
const handleCalculate = () => {
  if (!calcExpression.value) return
  try {
    // 安全清洗：只允许数字和运算符号
    const safeExpr = calcExpression.value.replace(/[^0-9+\-*/().]/g, '')
    if (!safeExpr) return

    // 使用 Function 替代 eval
    const result = new Function(`return ${safeExpr}`)()
    
    if (!isNaN(result) && isFinite(result)) {
      itemForm.quantity = Number(result.toFixed(4))
      
      // 自动追加备注
      const formulaRecord = `${safeExpr}=${itemForm.quantity}`
      if (itemForm.remark && !itemForm.remark.includes(safeExpr)) {
        itemForm.remark += `; ${formulaRecord}`
      } else if (!itemForm.remark) {
        itemForm.remark = formulaRecord
      }
      ElMessage.success({ message: `计算完成: ${result}`, duration: 1500 })
    }
  } catch (e) {
    ElMessage.warning('算式格式错误')
  }
}
// 计算器插入括号
const handleInsertParentheses = async () => {
  // 获取原生的 input DOM 元素
  // Element Plus 的 el-input 组件暴露了 input 属性指向原生节点
  const inputEl = calcInputRef.value?.input || calcInputRef.value?.$el.querySelector('input')
  
  if (!inputEl) return

  // 获取当前光标位置
  const start = inputEl.selectionStart
  const end = inputEl.selectionEnd
  const oldVal = calcExpression.value || ''

  // 在光标处插入 "()"
  // 如果有选中文本，会用 "()" 替换选中文本
  calcExpression.value = oldVal.substring(0, start) + '()' + oldVal.substring(end)

  // 等待 Vue 更新 DOM 后再移动光标
  await nextTick()

  // 将光标移动到括号中间 (即 start + 1 的位置)
  inputEl.setSelectionRange(start + 1, start + 1)
  inputEl.focus() // 保持聚焦
}
// 刀数统计
const handleKnifeCount = () => {
  if (!knifeExpression.value) return
  const count = (knifeExpression.value.match(/\+/g) || []).length
  knifeResult.value = String(count)
}
//查询组价明细
// 1. 过滤后的数据 (页面内搜索逻辑)
const filteredItems = computed(() => {
  // 1. 提取出用户已输入内容的搜索条件，生成 [ ['item1', 'value'], ... ] 的数组
  //    这样避免了每次循环都去判断 if (!searchParams.xxx)
  const activeFilters = Object.entries(searchParams).filter(([_, value]) => value)

  // 2. 性能优化：如果没有搜索条件，直接返回原数组，跳过遍历
  if (activeFilters.length === 0) return items.value

  // 3. 执行过滤
  return items.value.filter(item => {
    // every 代表所有有效条件都必须满足 (AND 关系)
    return activeFilters.every(([key, query]) => {
      const itemValue = item[key as keyof typeof item]
      // 核心匹配逻辑：判空 + 转字符串 + 转小写 + 包含判断
      return itemValue !=null && String(itemValue).toLowerCase().includes(String(query).toLowerCase())
    })
  })
})

// 2. 分页后的数据 (显示逻辑)
const displayItems = computed(() => {
  const start = (pagination.current - 1) * pagination.size
  const end = start + pagination.size
  return filteredItems.value.slice(start, end)
})

// 后端查询获取报价明细子项
const getDetailItems = async (detailId: string) => {
  const res = await listDetailItems(detailId)
  items.value = res;
  pagination.current = 1;
}

 
// 2.1 执行搜索
const handleSearch = () => {
  pagination.current = 1 // 搜索时重置回第一页
  // computed 会自动更新 displayItems，这里不需要额外逻辑
  ElMessage.success('搜索已更新')
}
// 2.2 重置搜索
const handleResetSearch = () => {
  searchParams.unit = ''
  searchParams.quantity = undefined
  searchParams.process1 = ''
  searchParams.process2 = ''
  searchParams.process3 = ''
  searchParams.process4 = ''
  searchParams.distPrice = undefined
  searchParams.totalPrice = undefined
  searchParams.remark = ''
  pagination.current = 1
}

// 你的退出编辑逻辑
const handleCancelEdit = () => {
  // ... 原有的退出逻辑 ...
  handleResetForm()
  // 重置 hover 状态，防止切回新增模式后状态残留
  isHoveringMode.value = false 
  // 重置编辑状态
  isEditing.value = false
}

// 新增/修改提交
const handleSubmit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  // 核心修改：await 等待校验结果，并在回调内部处理后续逻辑
  await formEl.validate(async(valid,fields) => {
    if (valid) {
      if(itemForm.id){
        //进行类型重定义
        const updateData = itemForm as QuoteDetailItemUpdateDto

        // 2. 调用修改接口
          await updateDetailItem(updateData)
          ElMessage.success('修改成功')
      }else{
        const {id,...rest} = itemForm
        const addData: QuoteDetailItemAddDto = {
          ...rest,
          detailId: detailId.value,
          quoteId: quoteId.value
        }
        // 新增模式
        await addDetailItem(addData)
        ElMessage.success('新增成功')
      }
      handleResetForm()
      // 刷新当前页数据
        getDetailItems(detailId.value)
    } else {
      // --- 校验失败 ---
      ElMessage.warning('请填写完整信息')
      console.log('校验失败字段:', fields)
      return 
    }
  })
}

//shift+enter 调用添加
const handleGlobalKeydown = (e: KeyboardEvent) => {
  if (e.altKey && (e.key === 'Enter' || e.code === 'Enter')) {
    // 防抖建议：如果想要防止连点，可以在这里加个 loading 判断
    handleSubmit(itemFormRef.value)
  }
}
// 初始化版本选项
const initVersionOptions =  async () => {
  const versions = await getBasePriceVersions()
  // 初始化版本选项
  versionOptions.value = versions.map(v=>({label:v,value:v}))
  
}

onMounted(() => {
  initVersionOptions()//初始化报价版本
  window.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalKeydown)
})

//删
const handleRemove = async (itemId: string) => {
  try {
    await deleteDetailItem(itemId)
    ElMessage.success('删除成功')
    getDetailItems(detailId.value)
  } catch (error) {
    ElMessage.error('删除失败')
  }
}
//改
const handleEditItem = async (row: any) => {
  //赋值id就是编辑模式
  Object.assign(itemForm, row)
  
  // 显示编辑模式提示
  isEditing.value = true
  
  // 滚动到表单区域
  await nextTick()
  if (formCardRef.value) {
    formCardRef.value.$el.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    })
  }
  
  // 3秒后自动关闭高亮效果
  setTimeout(() => {
    isEditing.value = false
  }, 3000)
}

// 重置组价明细新增表单
const handleResetForm = () => {
  Object.assign(itemForm, getInitItemForm())
  console.log("清空后的表单",itemForm)
  editingIndex.value = -1
  calcExpression.value = ''
  knifeExpression.value = ''
  knifeResult.value = ''
}
// 计算总金额
const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + (item.totalPrice || 0), 0).toFixed(2)
})

// === 核心状态 ===
const detailId = ref('')// 明细ID
const quoteId = ref('')// 报价ID
const parentInfo = ref({
  id: '',
  quoteId: '',
  detailVersion: -1,
  rowNum: -1,
  projectArea: '',
  position: '',
  spec: '',
  model: '',
  thickness: undefined,
  material: '',
  materialCode: '',
  color: '',
  length: undefined,
  width: undefined,
  quantity: undefined,
  unit: '',
  elevationNo: '',
  nodeNo: '',
  installPriceUnit: undefined,
  version: '',
  remarkDesc: '',
  projectType: -1,
}as ParentInfo) // 父级信息
const items = ref<QuoteDetailItemVO[]>([])// 组价明细列表
const editingIndex = ref(-1)// 正在编辑的行索引
// ... 状态定义 ...态 ===
const parentLoading = ref(false)// 父级信息加载状态
// === 搜索与分页 ===
const searchParams = reactive<QuoteDetailItemVO>({
  id: undefined,
unit: ''    ,
quantity: undefined,
process1: '',
process2: '',
process3: '',
process4: '',
distPrice: undefined,
totalPrice: undefined,
remark: ''
})



const pagination = reactive({
  current: 1,
  size: 9
})

// === 表单与计算器 ===

const detailFormRef = ref<FormInstance>()// 明细表单引用



// === 底价库 ===
const libraryLoading = ref(false)
const libraryQuery = reactive({ version: 'A0018', keyword: '' })
const libraryData = ref<any[]>([])


// === 保存明细 ===
const handleUpdateDetail = async () => {
  if (!detailFormRef.value?.validate()) return
  try{
    await updateQuoteDetail(parentInfo.value)
    ElMessage.success('保存成功')
  }catch(e){
    console.error('保存失败:', e)
    ElMessage.error('保存失败')
  }
}
// ==========================================
// 1. 初始化父级清单
// ==========================================
const initData = async () => {
  const id = route.query.detailId as string
  if (!id) return

  detailId.value = id
  handleResetForm() // 切换单据时重置表单
  try{
  parentLoading.value = true
  const [detailRes] = await Promise.all([
    selectQuoteDetail(id),
    getDetailItems(id)
  ])
  // 1.1 加载父级信息 (优先从参数读，防止多次请求)
  // 实际项目中建议只传 ID，这里调接口查，为了演示直接取 Query
  parentInfo.value = detailRes || {}
  libraryQuery.version = parentInfo.value.version || 'A0018'
  quoteId.value = parentInfo.value.quoteId || ''
  }catch(e){
    console.error('加载明细失败:', e)
    ElMessage.error('加载明细失败')
  }finally{
    parentLoading.value = false
  }
  // 1.2 加载 BOM 明细 (模拟请求)
  console.log(`正在加载 BOM, ID: ${id}`)
  mockLoadLibrary() // 加载底价库
  
}

// 监听路由参数变化，实现单页更新
watch(
  () => route.query.detailId,
  (newId) => { if (newId) initData() },
  { immediate: true }
)

// ==========================================
// 3. 业务逻辑：底价库
// ==========================================
const loadLibrary = async () => {
  libraryLoading.value = true
  // const res = await listBasePriceLibrary(libraryQuery)
  // libraryData.value = res.rows
  setTimeout(() => {
     mockLoadLibrary() // 模拟刷新
     libraryLoading.value = false
  }, 300)
}

const handleLibraryPick = (row: any) => {
  itemForm.unit = row.unit
  itemForm.process1 = row.process1
  itemForm.process2 = row.process2
  itemForm.process3 = row.process3
  itemForm.process4 = row.process4 || ''
  itemForm.distPrice = row.distPrice
  // 不覆盖数量
  ElMessage.success(`已选择: ${row.process2}`)
}

// ==========================================
// 4. 业务逻辑：CRUD
// ==========================================





const handleBack = () => {
  // 关闭当前 Tag 并返回
  if (tagsViewStore && tagsViewStore.delView) {
    tagsViewStore.delView(route)
  }
  router.go(-1)
}



// ==========================================
// 5. 模拟数据 (可删除)
// ==========================================


const mockLoadLibrary = () => {
  // 模拟筛选
  const all = [
    { process1: '板材材料', process2: '201-2b', process3: '0.4mm', process4: '一级', unit: 'm²', distPrice: 31 },
    { process1: '板材材料', process2: '201-2b', process3: '0.5mm', process4: '一级', unit: 'm²', distPrice: 38 },
    { process1: '板材材料', process2: '201-2b', process3: '0.6mm', process4: '一级', unit: 'm²', distPrice: 42 },
    { process1: '板材材料', process2: '201-2b', process3: '0.7mm', process4: '一级', unit: 'm²', distPrice: 49 },
    { process1: '板材材料', process2: '304-2b', process3: '1.0mm', process4: '一级', unit: 'm²', distPrice: 85 },
    { process1: '安装工艺', process2: '商住大区', process3: '常规门套', process4: '', unit: 'm²', distPrice: 105 },
    { process1: '屏风系统', process2: '移门外框', process3: '10万以上', process4: '有色板-1.0', unit: 'm²', distPrice: 375.5 },
  ]
  if (libraryQuery.keyword) {
    libraryData.value = all.filter(i => i.process2.includes(libraryQuery.keyword) || i.process3.includes(libraryQuery.keyword))
  } else {
    libraryData.value = all
  }
}
</script>

<style scoped>
/* 全局覆盖 Element Plus 样式以实现紧凑布局 */
/* 1. 必须先把 el-card 本身变成 Flex 容器 (垂直方向) */
/* 1. 卡片容器：变成 Flex 列布局 */
.formula-page :deep(.el-card) {
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 关键：防止卡片整体被撑大 */
}

/* 编辑模式高亮效果 */
.edit-highlight {
  animation: edit-highlight 0.5s ease-in-out;
  border: 2px solid #409EFF !important;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

@keyframes edit-highlight {
  0% {
    box-shadow: 0 0 0 rgba(64, 158, 255, 0);
  }
  50% {
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.8);
  }
  100% {
    box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
  }
}

/* 2. 卡片 Header：防止被压缩 */
.formula-page :deep(.el-card__header) {
  flex-shrink: 0;   /* 关键：头部不可压缩 */
  padding: 6px 12px !important;
  background-color: #f9fafb;
  border-bottom: 1px solid #ebeef5;
}

/* 3. 卡片 Body：自动占满剩余空间 */
.formula-page :deep(.el-card__body) {
  flex: 1 ;   /* 关键：占满剩余空间 */
  min-height: 0;    /* 允许内部 flex 缩放 */
  padding: 0 !important;
  display: flex;    /* 开启 flex 布局 */
  flex-direction: column;
  overflow: hidden; /* 关键：防止内部内容溢出 */
}

/* 描述列表紧凑化 */
.compact-desc :deep(.el-descriptions__cell) {
  padding: 4px 8px !important;
}

/* 输入框紧凑 */
:deep(.el-form-item--small) {
  margin-bottom: 8px; /* 默认是 18px */
}

/* 表格头部 */
:deep(.el-table thead) {
  color: #374151;
  font-weight: 500;
}
</style>