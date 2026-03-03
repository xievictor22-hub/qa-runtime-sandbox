package com.mogo.project.modules.quote.domain.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity;
import com.mogo.project.modules.quote.core.convert.enums.QuoteStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 报价单总表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_order")
public class QuoteOrder extends BaseEntity {

    private String projectCode;
    private Integer projectType;
    private String projectName;
    private Long deptId;

    /**
     * 状态: 0=草稿, 1=审核中, 2=业务调整, 3=已完成, 4=已驳回, 5=重新调整
     * @see QuoteStatus
     */

    private String status;

    /** 当前处理人ID */
    private Long currentHandlerId;


    private BigDecimal taxRate;
    private String customerName;

    // 费用
    private BigDecimal totalMaterialPrice;
    private BigDecimal totalInstallPrice;
    private BigDecimal finalTotalPrice;
    // JSON 字段建议用 String 接收，或者配合 Jackson TypeHandler
    private String otherFeesJson;

    // --- 业务传参 (不存库) ---
    @TableField(exist = false)
    private Long nextHandlerId; // 下一步处理人
    @TableField(exist = false)
    private String auditComment; // 审核备注

    private Integer currentQuoteVersion;//当前报价版本
    private Integer currentBusinessVersion;//当前业务员版本

}