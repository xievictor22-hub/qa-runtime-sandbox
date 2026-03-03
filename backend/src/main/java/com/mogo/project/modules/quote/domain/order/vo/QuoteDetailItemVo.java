package com.mogo.project.modules.quote.domain.order.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "报价明细子项视图对象")
public class QuoteDetailItemVo {

    @Schema(description = "主键ID", type = "string") // 明确告诉文档这是字符串格式
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "项目1(类别)")
    private String process1;

    @Schema(description = "项目2(名称)")
    private String process2;

    @Schema(description = "项目3(规格)")
    private String process3;

    @Schema(description = "项目4(材质)")
    private String process4;

    @Schema(description = "分销价(单价)")
    private BigDecimal distPrice;

    @Schema(description = "汇总价")
    private BigDecimal totalPrice;

    @Schema(description = "备注")
    private String remark;
}