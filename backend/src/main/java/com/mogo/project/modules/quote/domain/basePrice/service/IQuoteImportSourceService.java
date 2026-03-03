package com.mogo.project.modules.quote.domain.basePrice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;

import java.util.List;

/**
 * 业务调整价格服务
 */
public interface IQuoteImportSourceService extends IService<QuoteImportSource> {

    List<QuoteImportSource> selectByVersion(String version);

    void deleteByVersion(String version);

    void deleteByVersionPhysically(String version);

}
