import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
//负责计算器和刀数统计逻辑。
export function useCalculator(formData: any) {
  const calcExpression = ref('')
  const knifeExpression = ref('')
  const knifeResult = ref('')
  const calcInputRef = ref()

  // 数量计算
  const handleCalculate = () => {
    if (!calcExpression.value) return
    try {
      const safeExpr = calcExpression.value.replace(/[^0-9+\-*/().]/g, '')
      if (!safeExpr) return
      // eslint-disable-next-line no-new-func
      const result = new Function(`return ${safeExpr}`)()
      
      if (!isNaN(result) && isFinite(result)) {
        formData.quantity = Number(result.toFixed(4))
        
        // 自动追加备注
        const formulaRecord = `${safeExpr}=${formData.quantity}`
        if (formData.remark && !formData.remark.includes(safeExpr)) {
          formData.remark += `; ${formulaRecord}`
        } else if (!formData.remark) {
          formData.remark = formulaRecord
        }
        calcExpression.value = ''
        ElMessage.success({ message: `计算完成: ${result}`, duration: 1500 })
      }
    } catch (e) {
      ElMessage.warning('算式格式错误')
    }
  }

  // 插入括号
  const handleInsertParentheses = async () => {
    const inputEl = calcInputRef.value?.input || calcInputRef.value?.$el.querySelector('input')
    if (!inputEl) return
    const start = inputEl.selectionStart
    const end = inputEl.selectionEnd
    const oldVal = calcExpression.value || ''
    calcExpression.value = oldVal.substring(0, start) + '()' + oldVal.substring(end)
    await nextTick()
    inputEl.setSelectionRange(start + 1, start + 1)
    inputEl.focus()
  }

  // 刀数统计
  const handleKnifeCount = () => {
    if (!knifeExpression.value) return
    const count = (knifeExpression.value.match(/\+/g) || []).length
    knifeResult.value = String(count)
  }
  // 重置计算器
  const resetCalculator = () => {
    calcExpression.value = ''
    knifeExpression.value = ''
    knifeResult.value = ''
  }

  return {
    calcExpression,
    knifeExpression,
    knifeResult,
    calcInputRef,
    handleCalculate,
    handleInsertParentheses,
    handleKnifeCount,
    resetCalculator
  }
}