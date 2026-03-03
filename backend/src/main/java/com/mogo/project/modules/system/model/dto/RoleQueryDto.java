package com.mogo.project.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色查询参数")
public class RoleQueryDto {

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "页大小")
    private Integer pageSize = 10;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "权限字符")
    private String roleKey;

    @Schema(description = "状态")
    private Integer status;
}