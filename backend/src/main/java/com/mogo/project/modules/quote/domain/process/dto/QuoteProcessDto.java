package com.mogo.project.modules.quote.domain.process.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class QuoteProcessDto {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;              // 单据ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nextHandlerId;   // 下一步处理人
    private String auditComment;  // 审核/完成备注
}