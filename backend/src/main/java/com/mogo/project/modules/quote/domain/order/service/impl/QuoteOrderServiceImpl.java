package com.mogo.project.modules.quote.domain.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetailItem;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailItemMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteOrderMapper;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailService;
import com.mogo.project.modules.quote.domain.order.service.IQuoteOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteOrderServiceImpl extends ServiceImpl<QuoteOrderMapper, QuoteOrder> implements IQuoteOrderService {



}