package com.mogo.project.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户列表查询参数")
public class UserQueryDto {

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "页大小", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "用户名 (模糊查询)")
    private String username;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态 (1正常 0禁用)")
    private Integer status;

    private String deptId;



}