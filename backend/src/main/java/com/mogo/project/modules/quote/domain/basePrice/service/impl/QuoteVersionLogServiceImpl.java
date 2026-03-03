package com.mogo.project.modules.quote.domain.basePrice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.modules.quote.domain.basePrice.mapper.QuoteVersionLogMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteVersionLog;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteVersionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class QuoteVersionLogServiceImpl extends ServiceImpl<QuoteVersionLogMapper, QuoteVersionLog> implements IQuoteVersionLogService {

    /**
     * 根据报价版本查询版本日志
     * @param version
     * @return
     */
    @Override
    public QuoteVersionLog selectByVersion(String version) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<QuoteVersionLog>().eq(QuoteVersionLog::getVersion, version)
        );
    }

    @Override
    public void deleteByIdPhysically(Long id) {
        baseMapper.deleteByIdPhysically(id);
    }
}