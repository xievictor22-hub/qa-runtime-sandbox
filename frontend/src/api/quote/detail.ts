import request from '@/api/index'

// 报价单明细VO
export interface QuoteDetailVO {
  /** 主键ID */
  id: string|undefined;
  /** 归属报价单ID */
  quoteId: number|undefined;
  /** 版本号 */
  detailVersion: number|undefined;
  /** 行序号 */
  rowNum: number|undefined;
  
  /** 基础信息 */
  productName: string;       // 产品名称
  version: string;     // 来源版本/BOM编码
  categoryLvl1: string;      // 一级分类
  categoryLvl2: string;      // 二级分类
  projectArea: string;       // 项目区域
  position: string;          // 位置
  
  /** 规格属性 */
  spec: string;              // 规格
  model: string;             // 型号
  thickness: string;         // 厚度
  material: string;          // 材质
  materialCode: string;      // 图纸材料代号
  color: string;             // 颜色
  length?: number|undefined;           // 长 (可选)
  width?: number|undefined;            // 宽 (可选)
  quantity?: number|undefined;          // 数量
  unit: string;              // 单位

  elevationNo: string;       // 里面图号
  nodeNo: string;            // 节点图号
  
  /** 工艺费单价 (后端通常返回 Decimal，前端对应 number) */
  installPriceUnit: number|undefined;      // 安装单价
  noFingerprintPrice: number|undefined;    // 无指纹单价
  noFingerprintFlag: string|undefined;     // 无指纹标记 ('0'|'1')
  slottingPrice: number|undefined;         // 开槽价
  shearingFoldingPrice: number|undefined;  // 剪折价
  laserMPrice: number|undefined;           // 激光M价
  
  /** 成本总价 (工厂视角) */
  factoryTotal: number|undefined;      // 出厂总价
  installTotal: number|undefined;      // 安装总价
  taxAmount: number|undefined;         // 税金
  
  /** 客户报价 (销售视角) */
  custUnitPrice: number|undefined;     // 客户单价
  custTotalPrice: number|undefined;    // 客户总价
  
  /** 变更与备注 */
  changeCategory?: string;   // 变更类别
  deptOwner?: string;        // 承担部门
  changeDesc?: string;       // 变更说明
  remarkDesc?: string;       // 技术备注
  remark?: string;           // 通用备注
}

//子项父项关联信息
export interface ParentInfo   {
  version: string;
  remarkDesc: string;
  projectType: number;//项目类型
  id: string,
  quoteId: string,//报价单ID
  detailVersion: number,//报价单版本
  rowNum: number,//子项行序号
  projectArea?: string,//项目区域
  position?: string,//位置
  spec?: string,//规格
  model?: String,//型号
  thickness?: number,//厚度
  material?: string,//材质
  materialCode?: string,//图纸材料代号
  color?: string,//颜色
  length?: number,//长 (可选)
  width?: number,//宽 (可选)
  quantity?: number,//数量
  unit?: string,//单位
  elevationNo?: string,//里面图号
  nodeNo?: string,//节点图号
  installPriceUnit?: number,//安装单价
  productName?: string;//产品名称
  distPrice?: number,//客户单价
  summaryPrice?: number,//客户总价
}

/** 查询明细列表 (当前版本) */
export function listQuoteDetails(quoteId: string): Promise<QuoteDetailVO[]> {
  return request({
    url: `/quote/detail/${quoteId}/list`,
    method: 'get'
  })
}
export function selectQuoteDetail(detailId: string): Promise<ParentInfo> {
  return request({
    url: `/quote/detail/${detailId}`,
    method: 'get'
  })
}

/** 导入明细 (覆盖上传) */
export function importQuoteDetail(quoteId: string, file: File): Promise<void> {
  const formData = new FormData()
  formData.append('file', file)
  return request<void>({
    url: `/quote/detail/${quoteId}/import`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/** 修改单行明细 */
export function updateQuoteDetail(data: ParentInfo): Promise<void> {
  return request<void>({
    url: '/quote/detail/update',
    method: 'put',
    data
  })
}

/** 删除单行明细 (级联删除子件) */
export function deleteQuoteDetail(id: string): Promise<void> {
  return request<void>({
    url: `/quote/detail/${id}`,
    method: 'delete'
  })
}
