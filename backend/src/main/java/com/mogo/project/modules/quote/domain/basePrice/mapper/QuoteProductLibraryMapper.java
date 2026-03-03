package com.mogo.project.modules.quote.domain.basePrice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;


import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteProductLibrary;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuoteProductLibraryMapper  extends BaseMapper<QuoteProductLibrary> {

    @Delete("DELETE from quote_product_library where version = #{version} ")
    void deleteByVersionPhysically(String version);
}
