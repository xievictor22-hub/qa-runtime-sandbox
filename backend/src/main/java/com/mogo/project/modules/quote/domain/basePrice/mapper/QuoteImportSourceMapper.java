package com.mogo.project.modules.quote.domain.basePrice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuoteImportSourceMapper extends BaseMapper<QuoteImportSource> {

    /**
     * 批量插入原始数据 (自定义XML实现，性能优于 MP 的 saveBatch)
     * @param list 数据列表
     * @return 插入行数
     */


    @Delete("DELETE from quote_import_source where version = #{version} ")
    void deleteByVersionPhysically(String version);
}
