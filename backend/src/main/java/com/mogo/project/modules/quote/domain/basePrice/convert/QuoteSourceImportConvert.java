package com.mogo.project.modules.quote.domain.basePrice.convert;

import com.mogo.project.modules.quote.core.convert.IBaseMapper;
import com.mogo.project.modules.quote.domain.basePrice.dto.QuoteBasePriceImportExcelDto;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring",
        // 【核心配置】: 如果输入是 null，返回默认值（对于 List 就是返回空 ArrayList，而不是 null）
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface QuoteSourceImportConvert extends IBaseMapper<QuoteBasePriceImportExcelDto, QuoteImportSource> {


}
