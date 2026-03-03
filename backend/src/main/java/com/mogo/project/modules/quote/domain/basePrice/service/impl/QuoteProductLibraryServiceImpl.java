package com.mogo.project.modules.quote.domain.basePrice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.modules.quote.domain.basePrice.dto.ProductLibraryProcessTreeQueryDto;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteFoldingRule;
import com.mogo.project.modules.quote.domain.basePrice.manager.ProductLibraryManager;
import com.mogo.project.modules.quote.domain.basePrice.mapper.QuoteProductLibraryMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteProductLibrary;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteProductLibraryService;
import com.mogo.project.modules.quote.domain.basePrice.vo.ProductLibraryProcessTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteProductLibraryServiceImpl extends ServiceImpl<QuoteProductLibraryMapper, QuoteProductLibrary> implements IQuoteProductLibraryService {

    private final ProductLibraryManager productLibraryManager;

    @Override
    public void deleteByVersion(String version) {
        baseMapper.delete(new LambdaQueryWrapper<QuoteProductLibrary>().eq(QuoteProductLibrary::getVersion,version));
    }

    @Override
    public void deleteByVersionPhysically(String version) {
        baseMapper.deleteByVersionPhysically(version);
    }

    /**
     * 获取制品底价库树形结构
     *
     * @return
     */
    @Override
    public List<ProductLibraryProcessTreeVO> getTree(ProductLibraryProcessTreeQueryDto query) {
        // 直接调用 Manager 的缓存方法
        return productLibraryManager.getProcessTreeCache(query);
    }
}
