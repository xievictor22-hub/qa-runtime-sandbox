package com.mogo.project.modules.quote.domain.order.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.mogo.project.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报价清单字段
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("quote_detail")
public class QuoteDetail extends BaseEntity {

    private Long quoteId;
    private Integer detailVersion;//版本号
    private Boolean lockStatus;//锁定状态

    // --- 业务字段 ---
    private Integer rowNum;              // 行序号
    private String version;        // 报价版本
    private String orderCodeSource;      // OrderID
    private String topId;                // TOPId
    private String bomId;                // BomID
    private String bomType;              // BomType
    private Integer detailType;              // 折件0制品1
    private Integer projectType;        //0家装1工装


    private String categoryLvl1;         // 一级分类
    private String categoryLvl2;         // 二级分类
    private String distributorName;      // 经销商
    private String customerName;         // 客户
    private String regionAscription;     // 区域归属
    private String projectArea;          // 项目区域
    private String position;             // 位置
    private String productName;          // 产品名称
    private String spec;                 // 规格
    private String model;                // 型号
    private String material;             // 材质
    private String materialCode;           //图纸材料代号
    private String color;                // 颜色
    private BigDecimal length;           // 长
    private BigDecimal width;            // 宽
    private BigDecimal thickness;            // 厚
    private String unit;                 // 单位
    private String surfaceProcess;  //表面工艺

    private BigDecimal quantity;         // 零件数量
    private BigDecimal totalQuantity;    // 合计数量
    private String pricingFormula;       // 计价公式
    private String reverseFormula;       // 反推公式
    private BigDecimal installPoint;         // 安装打点
    private BigDecimal factoryPoint;         // 出厂打点


    private BigDecimal craftCoeff;       // 工艺系数
    private BigDecimal nonStdCoeff;      // 非标系数

    private String nodeDrawingNo;        // 节点图号
    private String elevationDrawingNo;   // 立面图号

    // --- 工艺价格 ---
    private String install;          // 安装(标记)
    private BigDecimal installPrice; // 安装价

    private String noFingerprint; //无指纹
    private BigDecimal noFingerprintPrice;

    private String texture;   // 纹路
    private BigDecimal texturePrice;

    private String slotting;         // 开槽
    private BigDecimal slottingPrice;

    private String folding;  // 剪折
    private BigDecimal foldingPrice;

    private String laserM;           // 激光M
    private BigDecimal laserMPrice;

    private BigDecimal materialPrice;    // 材料价
    private BigDecimal colorPrice;       // 颜色价

    // --- 汇总价格 ---
    private BigDecimal distPrice;        // 出厂单价
    private BigDecimal summaryPrice;     // 出厂总价

    private BigDecimal factoryCostUnitPrice;   //生产成本单价--
    private BigDecimal factoryDiscounts;    //生产折扣--
    private BigDecimal factoryUnitPrice;    //生产单价--
    private BigDecimal factoryTotal;     // 生产总价
    private BigDecimal factoryProfit;    // 生产利润
    private BigDecimal factoryProfitRate; //生产打点-- 报价单全选

    private BigDecimal installCostUnitPrice;   //安装成本单价--
    private BigDecimal installDiscounts;    //安装折扣--
    private BigDecimal installUnitPrice;    //安装单价--
    private BigDecimal installTotal;     // 安装总价
    private BigDecimal installProfit;    // 安装利润
    private BigDecimal installProfitRate; //安装打点-- 报价单全选

    // --- 备注与杂项 ---
    private String deptOwner;            // 承担部门
    private String changeCategory;       // 增减类别
    private String changeDesc;           // 增减说明
    private String otherRemark;          // 其他备注
    private String remarkDesc;           // Note

    private Integer updateVersion;   //数据版本号，更新数据时会更新  用来识别在新建业务时提示被修改
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sourceAddTime; // AddTime

    // --- 关联数据 (不存本表) ---
    @TableField(exist = false)
    private List<QuoteDetailItem> items; // 制品的子件列表



    //后端ws推送前端对象，提示单据已更新
    @Data
    @AllArgsConstructor
    public static class QuoteUpdateMessage {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long quoteId;
        private String action; // "REFRESH", "DELETE" 等
    }
}