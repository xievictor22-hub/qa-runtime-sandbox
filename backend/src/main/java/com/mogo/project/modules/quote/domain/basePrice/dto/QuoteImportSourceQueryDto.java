package com.mogo.project.modules.quote.domain.basePrice.dto;


import com.mogo.project.common.entity.BasePageQuery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class QuoteImportSourceQueryDto extends BasePageQuery {

    private Integer projectType;
    private String keyword;
    private String process1;
    private String process2;
    private String process3;
    private String process4;
    private String unit;

    @NotBlank(message = "版本号(version)不能为空")
    private String version;



}
