package com.mogo.project.modules.quote.domain.basePrice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.modules.quote.domain.basePrice.mapper.QuoteImportSourceMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteImportSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务调整价格模块
 */
@Service
@RequiredArgsConstructor
public class QuoteImportSourceServiceImpl extends ServiceImpl<QuoteImportSourceMapper, QuoteImportSource> implements IQuoteImportSourceService {


    /**
     * 根据版本搜索，按创建时间排序
     * @param version
     * @return
     */
    @Override
    public List<QuoteImportSource> selectByVersion(String version) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<QuoteImportSource>()
                        .eq(QuoteImportSource::getVersion,version)
                        .orderByDesc(QuoteImportSource::getCreateTime)
        );
    }

    /**
     * 根据版本删除
     * @param version
     */
    @Override
    public void deleteByVersion(String version) {
        baseMapper.delete(new LambdaQueryWrapper<QuoteImportSource>().eq(QuoteImportSource::getVersion,version));
    }

    @Override
    public void deleteByVersionPhysically(String version) {
        baseMapper.deleteByVersionPhysically(version);
    }
}