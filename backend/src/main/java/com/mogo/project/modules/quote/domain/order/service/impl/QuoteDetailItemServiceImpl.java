package com.mogo.project.modules.quote.domain.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.quote.domain.order.convert.QuoteDetailItemConvert;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailItemMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailMapper;

import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetailItem;
import com.mogo.project.modules.quote.domain.order.constant.QuoteMatchConstant;
import com.mogo.project.modules.quote.domain.order.vo.QuoteDetailItemVo;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailItemService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.List;


@Service
@RequiredArgsConstructor
public class QuoteDetailItemServiceImpl extends ServiceImpl<QuoteDetailItemMapper, QuoteDetailItem> implements IQuoteDetailItemService {

    private final QuoteDetailMapper quoteDetailMapper; // 需要操作父表
    private final QuoteDetailItemConvert quoteDetailItemConvert;

    @Override
    public List<QuoteDetailItemVo> listByDetailId(Long detailId) {
        List<QuoteDetailItem> list = this.list(new LambdaQueryWrapper<QuoteDetailItem>()
                .eq(QuoteDetailItem::getDetailId, detailId)
                .orderByDesc(QuoteDetailItem::getCreateTime));
        //MapStruct转vo
        List<QuoteDetailItemVo> voList = quoteDetailItemConvert.toVoList(list);
        return voList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItem(QuoteDetailItem quoteDetailItem) {
        QuoteDetail parent = quoteDetailMapper.selectById(quoteDetailItem.getDetailId());
        if (parent == null) {
            throw new ServiceException("关联的报价明细行不存在");
        }
        BigDecimal distPrice = quoteDetailItem.getDistPrice();
        // 计算子件单行总价 = 单价 * 数量 * 损耗
        BigDecimal total = distPrice.multiply(quoteDetailItem.getQuantity()).multiply(new BigDecimal( quoteDetailItem.getFormulaSnapshot()));
        quoteDetailItem.setTotalPrice(total);
        quoteDetailItem.setUnitPriceSnapshot(distPrice);
        quoteDetailItem.setQuoteId(parent.getQuoteId()); // 冗余存储 quoteId 方便查询
        this.save(quoteDetailItem);
        // 核心：反算父级价格
        updateParentPrice(quoteDetailItem.getDetailId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(QuoteDetailItem item) {
        LambdaUpdateWrapper<QuoteDetailItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(QuoteDetailItem::getId, item.getId());
        updateWrapper.set(QuoteDetailItem::getProcess1, item.getProcess1());
        updateWrapper.set(QuoteDetailItem::getProcess2, item.getProcess2());
        updateWrapper.set(QuoteDetailItem::getProcess3, item.getProcess3());
        updateWrapper.set(QuoteDetailItem::getProcess4, item.getProcess4());
        updateWrapper.set(QuoteDetailItem::getUnit,item.getUnit());
        updateWrapper.set(QuoteDetailItem::getUnitPriceSnapshot,item.getUnitPriceSnapshot());
        updateWrapper.set(QuoteDetailItem::getFormulaSnapshot,item.getFormulaSnapshot());
        updateWrapper.set(QuoteDetailItem::getPointCoefficientSnapshot,item.getPointCoefficientSnapshot());
        updateWrapper.set(QuoteDetailItem::getRemarkDesc,item.getRemarkDesc());
        updateWrapper.set(QuoteDetailItem::getQuantity,item.getQuantity());
        updateWrapper.set(QuoteDetailItem::getDistPrice,item.getDistPrice());
        updateWrapper.set(QuoteDetailItem::getRemark,item.getRemark());
        // 重新计算子件总价
        if (item.getDistPrice() != null && item.getQuantity() != null) {
            updateWrapper.set(QuoteDetailItem::getTotalPrice, item.getDistPrice().multiply(item.getQuantity()));
        }else{
            updateWrapper.set(QuoteDetailItem::getTotalPrice, null);
        }
        this.update(updateWrapper);

        // 核心：反算父级价格
        updateParentPrice(item.getDetailId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        QuoteDetailItem item = this.getById(id);
        if (item != null) {
            Long detailId = item.getDetailId();
            this.removeById(id);
            // 核心：反算父级价格
            updateParentPrice(detailId);
        }
    }

    @Override
    public Long countByVersion(String version) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<QuoteDetailItem>().eq(QuoteDetailItem::getVersion, version)
        );
    }

    @Override
    public void deleteByDetailIds(List<Long>  parentIds) {
        baseMapper.delete(new  LambdaQueryWrapper<QuoteDetailItem>().in(QuoteDetailItem::getDetailId, parentIds));
    }

    /**
     * 私有辅助方法：重新计算父级 Detail 的总价与单价
     * 逻辑：Sum(子件总价) = 父件出厂总价 -> 父件出厂单价 = 总价 / 父件数量
     */
    private void updateParentPrice(Long detailId) {
        // 1. 查出所有关联子件
        List<QuoteDetailItem> items = this.list(new LambdaQueryWrapper<QuoteDetailItem>()
                .eq(QuoteDetailItem::getDetailId, detailId)
                .orderByAsc(QuoteDetailItem::getId));

        // 2. 累加所有子件的总价
        BigDecimal totalCost = items.stream()
                .map(QuoteDetailItem::getTotalPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2.1 按工艺拆分安装/生产总成本（避免 process1 为 null 导致 NPE）
        BigDecimal installTotalCost = items.stream()
                .filter(detailItem -> QuoteMatchConstant.PROCESS_INSTALL.equals(detailItem.getProcess1()))
                .map(QuoteDetailItem::getTotalPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal factoryTotalCost = items.stream()
                .filter(detailItem -> !QuoteMatchConstant.PROCESS_INSTALL.equals(detailItem.getProcess1()))
                .map(QuoteDetailItem::getTotalPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 更新父对象
        QuoteDetail parent = quoteDetailMapper.selectById(detailId);
        if (parent != null) {
            parent.setSummaryPrice(totalCost); // 子件的总价就是明细的总价
            parent.setInstallTotal(installTotalCost);
            parent.setFactoryTotal(factoryTotalCost);
            // 反算单价：如果父件数量为0，避免除以零异常
            if (parent.getQuantity() != null && parent.getQuantity().compareTo(BigDecimal.ZERO)  > 0) {
                // 单价 = 总价 / 数量 (保留2位小数，四舍五入)
                BigDecimal unitPrice = totalCost.divide(
                        parent.getQuantity(),
                        2,
                        RoundingMode.HALF_UP
                );
                parent.setDistPrice(unitPrice);
                parent.setInstallCostUnitPrice(installTotalCost.divide(parent.getQuantity(), 2, RoundingMode.HALF_UP));
                parent.setFactoryCostUnitPrice(factoryTotalCost.divide(parent.getQuantity(), 2, RoundingMode.HALF_UP));
            } else {
                parent.setDistPrice(BigDecimal.ZERO);
                parent.setInstallCostUnitPrice(BigDecimal.ZERO);
                parent.setFactoryCostUnitPrice(BigDecimal.ZERO);
            }
            quoteDetailMapper.updateById(parent);
        }
    }
}
