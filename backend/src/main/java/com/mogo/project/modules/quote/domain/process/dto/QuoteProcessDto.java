package com.mogo.project.modules.quote.domain.process.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class QuoteProcessDto {
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "单据ID不能为空")
    private Long id;              // 单据ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nextHandlerId;   // 下一步处理人
    @Size(max = 500, message = "审核备注长度不能超过500")
    private String auditComment;  // 审核/完成备注
}