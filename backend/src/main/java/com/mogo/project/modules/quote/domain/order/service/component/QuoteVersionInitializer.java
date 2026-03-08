package com.mogo.project.modules.quote.domain.order.service.component;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.quote.core.enums.LogAction;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailMapper;
import com.mogo.project.modules.quote.domain.process.entity.QuoteLog;
import com.mogo.project.modules.quote.domain.process.mapper.QuoteLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 报价版本初始化器：封装“回退到报价并创建新报价版本”的复制逻辑
 */
@Component
@RequiredArgsConstructor
public class QuoteVersionInitializer {

    private final QuoteDetailMapper quoteDetailMapper;
    private final QuoteLogMapper quoteLogMapper;

    public Long resolveReturnQuoterId(Long quoteId, Long fallbackUserId) {
        QuoteLog lastSubmitLog = quoteLogMapper.selectOne(new LambdaQueryWrapper<QuoteLog>()
                .select(QuoteLog::getOperatorId)
                .eq(QuoteLog::getQuoteId, quoteId)
                .eq(QuoteLog::getAction, LogAction.SUBMIT_AUDIT.name())
                .orderByDesc(QuoteLog::getCreateTime)
                .last("LIMIT 1"));
        if (lastSubmitLog != null && lastSubmitLog.getOperatorId() != null) {
            return lastSubmitLog.getOperatorId();
        }
        return fallbackUserId;
    }

    public List<QuoteDetail> cloneQuoteDetailsToNewVersion(Long quoteId, Integer oldVer, Integer newVer) {
        List<QuoteDetail> oldDetails = quoteDetailMapper.selectList(new LambdaQueryWrapper<QuoteDetail>()
                .eq(QuoteDetail::getQuoteId, quoteId)
                .eq(QuoteDetail::getDetailVersion, oldVer));

        if (oldDetails.isEmpty()) {
            throw new ServiceException("当前版本无明细数据");
        }

        List<QuoteDetail> newDetails = oldDetails.stream().map(d -> {
            QuoteDetail newDetail = new QuoteDetail();
            BeanUtils.copyProperties(d, newDetail, "id", "createTime", "updateTime");
            newDetail.setDetailVersion(newVer);
            newDetail.setLockStatus(false);
            return newDetail;
        }).collect(Collectors.toList());

        newDetails.forEach(quoteDetailMapper::insert);
        return newDetails;
    }
}