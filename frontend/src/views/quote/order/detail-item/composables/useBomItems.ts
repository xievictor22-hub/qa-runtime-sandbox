import { ref, reactive, computed } from 'vue'
import { listDetailItems } from '@/api/quote/item'
import type { QuoteDetailItemSearchDto, QuoteDetailItemVO } from '@/api/quote/item'
import { PageQuery } from '@/api/types'
//负责 BOM 列表的数据、前端过滤、分页和总价计算
export function useBomItems() {
  const items = ref<QuoteDetailItemVO[]>([])
  const loading = ref(false)
  
  // 搜索参数
  const searchParams = reactive<QuoteDetailItemSearchDto>({
    process1: '',
    process2: '',
    process3: '',
    process4: '',
    distPrice: undefined,
    unit: '',
    quantity: undefined,
    totalPrice: undefined,
    remark: ''
  })

  // 分页参数
  const pagination = reactive<PageQuery>({
    pageNum: 1,
    pageSize: 9
  })

  // 过滤逻辑
  const filteredItems = computed(() => {
    console.log("items.value",items.value)

    const activeFilters = Object.entries(searchParams).filter(([_, value]) => value !== '' && value !== undefined)
    if (activeFilters.length === 0) return items.value

    return items.value.filter(item => {
      return activeFilters.every(([key, query]) => {
        const itemValue = item[key as keyof QuoteDetailItemVO]
        return itemValue != null && String(itemValue).toLowerCase().includes(String(query).toLowerCase())
      })
    })
  })

  // 分页显示数据
  const displayItems = computed(() => {
    console.log("filteredItems.value",filteredItems.value)
    const start = (pagination.pageNum - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    return filteredItems.value.slice(start, end)
  })

 
  // 加载数据
  const loadItems = async (detailId: string) => {
    if (!detailId) return
    loading.value = true
    try {
      const res = await listDetailItems(detailId)
      items.value = res
      // 如果当前页超过总页数，重置回第一页
      const maxPage = Math.ceil(res.length / pagination.size) || 1
      if (pagination.current > maxPage) pagination.current = 1
    } finally {
      loading.value = false
    }
  }

  const resetSearch = () => {
    Object.keys(searchParams).forEach(key => (searchParams as any)[key] = undefined)
    searchParams.unit = ''
    searchParams.process1 = '' // 特定重置为空字符串
    searchParams.quantity = undefined
    searchParams.distPrice = undefined
    searchParams.remark = ''
    searchParams.totalPrice = undefined
    searchParams.process2 = ''
    searchParams.process3 = ''
    searchParams.process4 = ''
    // 重置分页
    pagination.current = 1
  }

  return {
    items,
    loading,
    searchParams,
    pagination,
    displayItems,
    filteredItems, // 暴露给外部用于显示 total count
    loadItems,
    resetSearch
  }
}