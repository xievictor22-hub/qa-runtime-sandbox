import request from '@/api/index';
import type { PageResult } from '@/api/types'

// ==========================================
// TS 类型定义 (让你的代码有代码提示)
// ==========================================

// 字典数据项接口
export interface DictData {
  dictCode?: number;
  dictLabel: string;
  dictValue: string;
  dictType: string;
  dictSort: number;
  listClass?: string; // 增强字段：表格回显样式 (success/danger)
  cssClass?: string;  // 增强字段：自定义类名
  isSystem?: string;  // 增强字段：是否系统内置
  status: string;
  remark?: string;
}

// 字典类型接口
export interface DictType {
  dictId?: number;
  dictName: string;
  dictType: string;
  status: string;
  remark?: string;
}

// 查询参数接口
export interface DictTypeQuery extends PageQuery {
  dictName?: string;
  dictType?: string;
  status?: string;
}

export interface DictDataQuery extends PageQuery {
  dictType?: string;
  dictLabel?: string;
  status?: string;
}

// 分页基础接口 (建议提取到全局 type.ts)
interface PageQuery {
  pageNum: number;
  pageSize: number;
}

// ==========================================
// 1. 通用接口 (业务页面最常用的)
// ==========================================

/**
 * 根据字典类型查询字典数据信息 (给下拉框用的)
 * 对应后端: @GetMapping("/system/dict/data/type/{dictType}")
 * @param dictType 字典类型 (如 sys_user_sex)
 */
export function getDicts(dictType: string): Promise<DictData[]> {
  return request({
    url: `/system/dict/data/type/${dictType}`,
    method: 'get'
  });
}

// ==========================================
// 2. 字典类型管理 (Type Controller)
// ==========================================

/**
 * 查询字典类型列表
 */
export function listType(query: DictTypeQuery): Promise<PageResult<DictType>> {
  return request({
    url: '/system/dict/type/list',
    method: 'get',
    params: query
  });
}

/**
 * 查询字典类型详细
 */
export function getType(dictId: number): Promise<DictType> {
  return request({
    url: `/system/dict/type/${dictId}`,
    method: 'get'
  });
}

/**
 * 新增字典类型
 */
export function addType(data: DictType): Promise<void> {
  return request({
    url: '/system/dict/type',
    method: 'post',
    data: data
  });
}

/**
 * 修改字典类型
 */
export function updateType(data: DictType): Promise<void> {
  return request({
    url: '/system/dict/type',
    method: 'put',
    data: data
  });
}

/**
 * 删除字典类型
 */
export function delType(dictIds: number | number[]): Promise<void> {
  return request({
    url: `/system/dict/type/${dictIds}`,
    method: 'delete'
  });
}

/**
 * 刷新字典缓存 (重要)
 */
export function refreshCache(): Promise<void> {
  return request({
    url: '/system/dict/type/refreshCache',
    method: 'delete'
  });
}

// ==========================================
// 3. 字典数据管理 (Data Controller)
// ==========================================

/**
 * 查询字典数据列表
 */
export function listData(query: DictDataQuery): Promise<PageResult<DictData>> {
  return request({
    url: '/system/dict/data/list',
    method: 'get',
    params: query
  });
}

/**
 * 查询字典数据详细
 */
export function getData(dictCode: number): Promise<DictData> {
  return request({
    url: `/system/dict/data/${dictCode}`,
    method: 'get'
  });
}

/**
 * 新增字典数据
 */
export function addData(data: DictData): Promise<void> {
  return request({
    url: '/system/dict/data',
    method: 'post',
    data: data
  });
}

/**
 * 修改字典数据
 */
export function updateData(data: DictData): Promise<void> {
  return request({
    url: '/system/dict/data',
    method: 'put',
    data: data
  });
}

/**
 * 删除字典数据
 */
export function delData(dictCodes: number | number[]): Promise<void> {
  return request({
    url: `/system/dict/data/${dictCodes}`,
    method: 'delete'
  });
}