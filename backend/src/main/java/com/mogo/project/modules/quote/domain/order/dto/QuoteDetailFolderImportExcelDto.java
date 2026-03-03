package com.mogo.project.modules.quote.domain.order.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 折件导入表excel类
 */
@Data
public class QuoteDetailFolderImportExcelDto {

    // --- 基础定位 ---
    @ExcelProperty("序号")
    private Integer rowNum;

    @ExcelProperty("报价版本")
    private String version;

    @ExcelProperty("一级分类")
    private String categoryLvl1; // 项目1

    @ExcelProperty("二级分类")
    private String categoryLvl2; // 项目2

    @ExcelProperty("项目区域/户型")
    private String projectArea;

    @ExcelProperty("位置")
    private String position;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("图纸材料代号")
    private String materialCode;

    // --- 规格参数 ---
    @ExcelProperty("立面图号")
    private String elevationDrawingNo;

    @ExcelProperty("节点图号")
    private String nodeDrawingNo;

    @ExcelProperty("材料")
    private String material;

    @ExcelProperty("厚度")
    private BigDecimal thickness;

    @ExcelProperty("型号")
    private String model;

    @ExcelProperty("无指纹")
    private String noFingerprint; // 是/否

    @ExcelProperty("颜色")
    private String color;

    @ExcelProperty("纹路")
    private String texture;

    @ExcelProperty("开槽")
    private String slotting;

    @ExcelProperty("剪折")
    private String folding;

    @ExcelProperty("激光M")
    private String laserM;

    @ExcelProperty("安装")
    private String install;

    @ExcelProperty("规格/尺寸")
    private String spec; // 描述性规格

//    @ExcelProperty("宽度(展开)mm")
//    private BigDecimal width; // String接收，后续转BigDecimal 不接收了

    @ExcelProperty("长度mm")
    private BigDecimal length;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("数量")
    private BigDecimal quantity;

    // --- 其他备注 ---
    @ExcelProperty("备注说明")
    private String remarkDesc;


    @ExcelProperty("其他备注")
    private String otherRemark;
}