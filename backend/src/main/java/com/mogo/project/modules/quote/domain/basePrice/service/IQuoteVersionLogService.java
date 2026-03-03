package com.mogo.project.modules.quote.domain.basePrice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteVersionLog;

/**
 * 业务调整价格服务
 */
public interface IQuoteVersionLogService extends IService<QuoteVersionLog> {

    QuoteVersionLog selectByVersion(String version);

    void deleteByIdPhysically(Long id);
}
