package com.mogo.project.modules.quote.domain.basePrice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.quote.domain.basePrice.dto.ProductLibraryProcessTreeQueryDto;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteProductLibrary;
import com.mogo.project.modules.quote.domain.basePrice.vo.ProductLibraryProcessTreeVO;

import java.util.List;


public interface IQuoteProductLibraryService extends IService<QuoteProductLibrary> {

    void deleteByVersion(String version);

    void deleteByVersionPhysically(String version);

    /**
     * 获取制品底价库树形结构
     * @return
     */
    List<ProductLibraryProcessTreeVO> getTree(ProductLibraryProcessTreeQueryDto queryDto);


}
