import request from '@/api/index'


// 1. 定义公共业务字段 (Base)
// 这些字段是新增、修改、查看都共有的
export interface QuoteDetailItemBase {
  unit?: string
  // 允许 undefined 是为了前端表单初始化方便
  quantity?: number 
  process1?: string
  process2?: string
  process3?: string
  process4?: string
  distPrice?: number
  remark?: string
}
export interface QuoteDetailItemSearchDto extends QuoteDetailItemBase {
  totalPrice?: number
}

// 2. 视图对象 (VO) - 用于接收后端数据
// 继承 Base，并增加后端返回的只读字段 (如 id, totalPrice, createTime)
export interface QuoteDetailItemVO extends QuoteDetailItemBase {
  id?: string       // 查询回来肯定有 ID
  totalPrice?: number // 这是后端算出来的，前端只读
  // 如果还有 createTime, updateTime 都在这里加
}

// 3. 新增参数 (AddDTO) - 用于传给后端
// 通常直接继承 Base 即可，因为 Base 里没有 id 和 totalPrice
export interface QuoteDetailItemAddDto extends QuoteDetailItemBase {
  // 必须加上 backend 需要的关联 id
  quoteId: string 
  detailId: string
}

// 4. 修改参数 (UpdateDTO) - 用于传给后端
// 继承 Base，但强制要求有 ID
export interface QuoteDetailItemUpdateDto extends QuoteDetailItemBase {
  id: string
}
// 5. 修改添加框
// 继承 Base，但强制要求有 ID
export interface QuoteDetailItemFormDto extends QuoteDetailItemBase {
  id?: string
  quoteId?: string 
  detailId?: string
  sourceLibraryId: string
  version: string
  formulaSnapshot: number//损耗
}


  


/** 查询某行的所有子件 */
export function listDetailItems(detailId: string): Promise<QuoteDetailItemVO[]> {
  return request({
    url: `/quote/item/list/${detailId}`,
    method: 'get'
  })
}

/** 添加子件 */
export function addDetailItem(data: QuoteDetailItemAddDto): Promise<void> {
  return request({
    url: '/quote/item/add',
    method: 'post',
    data
  })
}

/** 修改子件 只修改数量、单价、总价 */
export function updateDetailItem(data: QuoteDetailItemUpdateDto): Promise<void> {
  return request({
    url: '/quote/item/update',
    method: 'put',
    data
  })
}

/** 删除子件 */
export function deleteDetailItem(id: string): Promise<void> {
  return request({
    url: `/quote/item/${id}`,
    method: 'delete'
  })
}
