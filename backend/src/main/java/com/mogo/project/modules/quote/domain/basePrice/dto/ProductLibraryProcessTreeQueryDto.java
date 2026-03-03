package com.mogo.project.modules.quote.domain.basePrice.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询选项树
 */
@Data
public class ProductLibraryProcessTreeQueryDto  {

    @NotNull(message = "项目类型(projectType)不能为空")
    private Integer projectType;

    @NotBlank(message = "版本号(version)不能为空")
    private String version;

}
