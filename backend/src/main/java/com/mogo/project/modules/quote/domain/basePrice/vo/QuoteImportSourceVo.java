package com.mogo.project.modules.quote.domain.basePrice.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data

/**
 * 查询返回的视图对象
 */
public class QuoteImportSourceVo {

   //value = "项目类型"
    private Integer projectType;

   //"项目1")
    private String process1;

   //"项目2")
    private String process2;

   //"项目3")
    private String process3;

   //"项目4")
    private String process4;

   //"单位")
    private String unit;


   //"判断范围值")
    private String rangeValC;

   //"W取值(宽)")
    private String rangeValW;

   //"D取值(厚)")
    private String rangeValD;

   //"报价公式") // 对应 quote_formula
    private BigDecimal quoteFormula;

   //"打点系数")
    private BigDecimal pointCoefficient;

   //"单价")
    private BigDecimal unitPrice;

   //"是否计入折件") // 0, 2, 3, 4
    private String isFolding;

    // 如果需要版本号
   //"报价版本")
    private String version;

    private LocalDateTime createTime;

}
