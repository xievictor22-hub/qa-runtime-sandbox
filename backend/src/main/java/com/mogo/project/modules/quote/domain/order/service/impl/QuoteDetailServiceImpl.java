package com.mogo.project.modules.quote.domain.order.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailItemMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailMapper;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetailItem;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteDetailServiceImpl extends ServiceImpl<QuoteDetailMapper, QuoteDetail> implements IQuoteDetailService {


    private final QuoteDetailItemMapper quoteDetailItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDetailAndItems(Long id) {
        // 1. 先删子件
        quoteDetailItemMapper.delete(new LambdaUpdateWrapper<QuoteDetailItem>()
                .eq(QuoteDetailItem::getDetailId, id));

        // 2. 再删父行
        return this.removeById(id);
    }


}