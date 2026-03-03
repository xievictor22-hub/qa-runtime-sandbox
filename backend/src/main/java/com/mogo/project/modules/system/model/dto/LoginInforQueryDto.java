package com.mogo.project.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录日志查询参数")
public class LoginInforQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String ipaddr;
    private String username;
    private String status;     // 状态
}
