/**
 * 分页排序参数
 */
export interface SortParams {
  orderByColumn?: string;
  isAsc?: 'asc' | 'desc';
}

/**
 * 基础分页查询参数
 */
export interface PageQuery extends SortParams {
  pageNum: number;
  pageSize: number;
  
  /**
   * 扩展参数：用于处理 createTime[begin], createTime[end] 这种范围查询
   * 或者数据权限等隐式参数
   */
  params?: Record<string, any>;
  
  // 允许索引签名，适应某些极端的动态参数情况
  [key: string]: any;
}

// 使用时的技巧：params 不需要显式定义，直接赋值即可
// const query: UserSearchDto = {
//    pageNum: 1, pageSize: 10,
//    username: 'admin',
//    params: {
//       beginTime: '2023-01-01',
//       endTime: '2023-12-31'
//    }
// }

/**
 * 分页响应对象
 * T: 行数据的类型
 */
export interface PageResult<T> {
  rows: T[];
  total: number;
  code: number;
  msg: string;
}

/**
 * 辅助函数：快速重置分页参数
 * 场景：用户点击“重置”按钮时，不仅清空表单，还要把页码归1
 */
export function resetPageParam(query: PageQuery, defaultSize = 10) {
  query.pageNum = 1;
  query.pageSize = defaultSize;
  query.orderByColumn = undefined;
  query.isAsc = undefined;
  query.params = {};
}