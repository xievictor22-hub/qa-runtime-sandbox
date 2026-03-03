package com.mogo.project.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "操作日志查询参数")
public class OperLogQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String title;       // 模块标题
    private String operName;    // 操作人员
    private Integer businessType; // 业务类型
    private Integer status;     // 状态
}