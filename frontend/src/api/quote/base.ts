import { PageQuery } from './../types';
import request from '@/api/index'
import type { PageResult } from '@/api/types'

export interface BasePriceQuery extends PageQuery {
  projectType?: number
  process1?: string
  process2?: string
  process3?: string
  process4?: string
  version?: string
  keyword?: string
  unit?: string
}

export interface ProductLibraryPriceQuery extends PageQuery {
  projectType: number
  process1?: string
  process2?: string
  process3?: string
  process4?: string
  version: string
  keyword?: string
}


export interface ImportForm {
  file: File
  version: string
  description?: string
}

/**
 * 底价库产品 VO
 */
export interface BasePriceVO {
  batchId?: string;
  version?: string;
  projectType?: number;
  process1?: string;
  process2?: string;
  process3?: string;
  process4?: string;
  unit?: string;
  // 原始范围文本
  rangeValC?: string;
  rangeValW?: string;
  rangeValD?: string;
  // 核心数值: 损耗系数
  quoteFormula?: number;
  pointCoefficient?: number;
  unitPrice?: number;
  // 是否计入折件
  isFolding?: number;
}
// 产品库产品 VO
export interface ProductLibraryVo  {
  id: string;
  process1: string;
  process2?: string;
  process3?: string;
  process4?: string;
  unit: string;
  unitPrice: number;
  quoteFormula: number;//损耗
  pointCoefficient: number;//打点系数
}
// 产品库产品搜索 DTO
export interface ProductLibrarySearchDto  {
  process1?: string;
  process2?: string;
  process3?: string;
  process4?: string;
  keyword?: string;
  sourceVersionCode: string;
  pageNum?: number
  pageSize?: number
}

export interface ProcessNode {
  value: string;
  label: string;
  children?: ProcessNode[];
}
export interface TreeQuery {
  projectType: number
  version: string
}

// 使用从 @/api/types 导入的 PageResult<T> 泛型接口

/** 1. 查询底价库列表 */
export async function listBasePrice(params: BasePriceQuery): Promise<PageResult<BasePriceVO>> {
  return request({
    url: '/quote/base/list',
    method: 'get',
    params
  })
}

/** 2. 导入底价库 (Excel) */
export async function importBasePrice(form: ImportForm) {
  const formData = new FormData()
  formData.append('file', form.file)
  console.log(form.file.name)
  formData.append('version', form.version==''?'':('A'+form.version))
  if (form.description) {
    formData.append('description', form.description)
  }
  return request({ 
    url: '/quote/import/upload', 
    method: 'post', 
    data: formData,
    headers: { 'Content-Type': undefined }
  })
}

/** 3. 导出底价库 */
export async function exportBasePrice(params: string): Promise<{data:Blob}> {
  return request({
    url: '/quote/import/download/'+params,
    method: 'post',
    responseType: 'blob' // 下载文件必须设置
  })
}

export async function  getBasePriceVersions():Promise<string[]>{
  return request({
    url: `/quote/base/getBasePriceVersions`,
    method: 'get'
  })
}

export async function listProductLibrary(params: BasePriceQuery): Promise<PageResult<ProductLibraryVo>> {
  return request({
    url: '/quote/base/listProductLibrary',
    method: 'get',
    params
  })
}

export async function listBaseProducts(params:any): Promise<PageResult<BasePriceVO>> {
  return request({
    url: '/quote/base/listProductLibrary',
    method: 'get',
    params
  })
}

export function addBasePriceItem(data: any, version: string) {
  return request({
    url: `/quote/import/add/${version}`, // 请根据你的实际 Controller 路径前缀调整，这里假设是 /quote/base
    method: 'post',
    data: data
  })
}
// 4. 查询选项树
export async function getProcessTreeApi(params:TreeQuery): Promise<ProcessNode[]> {
  return request({
    url: `/quote/base/getProcessTree`,
    method: 'get',
    params
  })
}

