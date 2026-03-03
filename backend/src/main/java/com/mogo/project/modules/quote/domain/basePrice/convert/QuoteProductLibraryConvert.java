package com.mogo.project.modules.quote.domain.basePrice.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.modules.quote.core.convert.IBaseMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteProductLibrary;
import com.mogo.project.modules.quote.domain.basePrice.vo.ProductLibraryVo;
import com.mogo.project.modules.quote.domain.basePrice.vo.QuoteImportSourceVo;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
//        ,unmappedTargetPolicy = ReportingPolicy.IGNORE // 忽略未映射的字段
)
public interface QuoteProductLibraryConvert extends IBaseMapper<ProductLibraryVo, QuoteProductLibrary> {

}