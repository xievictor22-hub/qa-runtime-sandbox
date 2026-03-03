package com.mogo.project.modules.quote.domain.basePrice.dto;


import com.mogo.project.common.entity.BasePageQuery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductLibraryQueryDto extends BasePageQuery {

    @NotNull(message = "项目类型(projectType)不能为空")
    private Integer projectType;
    private String keyword;
    private String process1;
    private String process2;
    private String process3;
    private String process4;

    @NotBlank(message = "版本号(version)不能为空")
    private String version;

}
