package com.mogo.project.modules.quote.domain.basePrice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteVersionLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuoteVersionLogMapper extends BaseMapper<QuoteVersionLog> {
    @Delete("delete from quote_version_log where id = #{id} ")
    void deleteByIdPhysically(Long id);
}
