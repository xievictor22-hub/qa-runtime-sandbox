package com.mogo.project.modules.quote.domain.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.common.annotation.DataScope;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.common.util.SecurityUtils;
import com.mogo.project.modules.quote.core.enums.LogAction;
import com.mogo.project.modules.quote.core.event.QuoteStateChangeEvent;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteOrderMapper;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.core.convert.enums.QuoteStatus;
import com.mogo.project.modules.quote.domain.order.vo.QuoteOrderVo;
import com.mogo.project.modules.quote.domain.order.service.IQuoteManageService;
import com.mogo.project.modules.quote.core.component.QuotePermissionService;
import com.mogo.project.modules.quote.domain.process.service.impl.QuoteLogServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 1. 基础管理服务 (IQuoteManageService)
 * 负责：CRUD、列表查询、详情、导入导出。
 */
@Service
@RequiredArgsConstructor
public class QuoteManageServiceImpl extends ServiceImpl<QuoteOrderMapper, QuoteOrder> implements IQuoteManageService {


    private final QuoteLogServiceImpl quoteLogServiceImpl;
    private final QuotePermissionService permissionService;

    private final ApplicationEventPublisher eventPublisher;//广播更新单据


    /**
     * 创建报价单
     * @param quote 报价单
     * @return res
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createQuote(QuoteOrder quote) {
        quote.setCreateBy(SecurityUtils.getUserId());
        quote.setCreateTime(LocalDateTime.now());
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setCurrentHandlerId(SecurityUtils.getUserId());
        quote.setCurrentQuoteVersion(1);
        quote.setCurrentBusinessVersion(0);
        boolean res = this.save(quote);
        if(res) quoteLogServiceImpl.record(quote.getId(), LogAction.CREATE, "创建报价单"); // 封装recordLog方法
        eventPublisher.publishEvent(new QuoteStateChangeEvent(this, quote.getId(), LogAction.CREATE.name()));
        return res;
    }

    /**
     * 搜索待办清单
     * @param page 分页条件
     * @param query 查询条件
     * @return 待办清单
     */
    @Override
    public IPage<QuoteOrder> selectTodoList(Page<QuoteOrder> page, QuoteOrder query) {
        Long userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<QuoteOrder> wrapper = new LambdaQueryWrapper<>();

        // 只看当前处理人是我的
        wrapper.eq(QuoteOrder::getCurrentHandlerId, userId)
                .like(StringUtils.hasText(query.getProjectName()), QuoteOrder::getProjectName, query.getProjectName())
                .eq(StringUtils.hasText(query.getStatus()), QuoteOrder::getStatus, query.getStatus())
                .orderByDesc(QuoteOrder::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 查询部门下报价单列表
     * @param page 分页条件
     * @param query 查询条件
     * @return 结果列表
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u") // 只有查全部时才启用部门隔离
    public IPage<QuoteOrder> selectOrderPage(Page<QuoteOrder> page, QuoteOrder query) {
        // 使用 MyBatis Plus 的 XML 自定义查询，或者继续用 Wrapper 但需手动处理 DataScope
        // 如果用 Wrapper 配合注解，需要在 BaseMapper 中定义方法并加注解，不能直接用 selectPage
        // 建议：在 Mapper 中写一个 selectOrderList 方法
        return baseMapper.selectOrderList(page, query);
    }

    /**
     * 安全获取单据
     */
    @Override
    public QuoteOrder getByIdSafe(Long id) {
        QuoteOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new ServiceException("报价单不存在");
        }
        return order;
    }

    @Override
    public QuoteOrderVo getDetailWithPerms(Long id) {
        QuoteOrder order = this.getById(id);
        if (order == null) throw new ServiceException("单据不存在");
        QuoteOrderVo vo = new QuoteOrderVo();
        BeanUtils.copyProperties(order, vo);
        // 注入权限
        vo.setActionPermissions(permissionService.calcPermissions(order));
        // 注入人名 (略，建议调用 UserUtils 或关联查询)
        return vo;
    }
}
