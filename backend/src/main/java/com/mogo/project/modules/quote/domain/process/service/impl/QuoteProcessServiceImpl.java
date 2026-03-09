package com.mogo.project.modules.quote.domain.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.common.util.SecurityUtils;
import com.mogo.project.modules.quote.core.enums.LogAction;
import com.mogo.project.modules.quote.core.event.QuoteStateChangeEvent;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteBusinessItemMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailMapper;
import com.mogo.project.modules.quote.domain.order.service.component.BusinessVersionInitializer;
import com.mogo.project.modules.quote.domain.order.service.component.QuoteVersionInitializer;
import com.mogo.project.modules.quote.domain.process.mapper.QuoteLogMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteOrderMapper;
import com.mogo.project.modules.quote.domain.order.entity.QuoteBusinessItem;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.process.entity.QuoteLog;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.core.convert.enums.QuoteStatus;
import com.mogo.project.modules.quote.domain.process.service.IQuoteLogService;
import com.mogo.project.modules.quote.domain.order.service.IQuoteManageService;
import com.mogo.project.modules.quote.domain.process.service.IQuoteProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 报价单流程管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteProcessServiceImpl implements IQuoteProcessService {

    private final QuoteOrderMapper quoteOrderMapper;
    private final QuoteDetailMapper quoteDetailMapper;
    private final QuoteLogMapper quoteLogMapper;
    private final QuoteBusinessItemMapper quoteBusinessMapper;

    private final IQuoteManageService quoteManageService;
    private final IQuoteLogService quoteLogService;
    private final BusinessVersionInitializer businessVersionInitializer;
    private final QuoteVersionInitializer quoteVersionInitializer;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 报价员提交报价审核
     * 场景1: 报价员提交审核 (0/4 -> 1)
     * 线程安全：CAS更新状态
     * @param quoteId       报价单id
     * @param nextHandlerId 审核接收人
     * @param comment       备注
     */
    @Override
    public void submitAudit(Long quoteId, Long nextHandlerId, String comment) {
        QuoteOrder order = quoteManageService.getByIdSafe(quoteId);
        checkPermission(order);

        // 1. 校验前置状态 (只能是 待报价0 或 待重新报价4/6)
        if (!QuoteStatus.DRAFT.equals(order.getStatus())
                && !QuoteStatus.REJECT_TO_QUOTER.equals(order.getStatus())
                && !QuoteStatus.REQUOTE_FROM_COMPLETED.equals(order.getStatus())) {
            throw new ServiceException("当前状态不可提交审核");
        }

        // 2. 锁定当前报价版本明细
        Integer currentVer = order.getCurrentQuoteVersion();
        quoteDetailMapper.update(null, new LambdaUpdateWrapper<QuoteDetail>()
                .set(QuoteDetail::getLockStatus, 1) // 锁定
                .eq(QuoteDetail::getQuoteId, quoteId)
                .eq(QuoteDetail::getDetailVersion, currentVer));

        // 3. CAS 更新主表 (状态 -> 1)
        boolean success = casUpdateStatus(
                quoteId,
                order.getStatus(), // 原状态
                QuoteStatus.PENDING_AUDIT, // 新状态
                nextHandlerId // 新处理人
        );

        if (!success) {
            throw new ServiceException("提交失败：单据状态已变更或无权操作");
        }

        // 4. 记录日志
        quoteLogService.record(quoteId, LogAction.SUBMIT_AUDIT, nextHandlerId,
                String.format("提交审核 (报价版本 v%d): %s", currentVer, comment));
        //广播审批状态修改
        eventPublisher.publishEvent(new QuoteStateChangeEvent(this, quoteId, LogAction.SUBMIT_AUDIT.name()));
    }

    /**
     * 审核员审核报价
     * 场景4: 审核通过 -> 转交业务 (1 -> 2, 初始化 Business Item v1)
     * @param quoteId       报价单id
     * @param nextHandlerId 业务接收人
     * @param comment       备注
     */
    @Override
    public void auditPass(Long quoteId, Long nextHandlerId, String comment) {
        QuoteOrder order = quoteManageService.getByIdSafe(quoteId);
        checkPermission(order);

        if (!QuoteStatus.PENDING_AUDIT.equals(order.getStatus())) {
            throw new ServiceException("当前状态不可审核通过");
        }

        Integer quoteVer = order.getCurrentQuoteVersion();
        List<QuoteDetail> quoteDetails = quoteDetailMapper.selectList(new LambdaQueryWrapper<QuoteDetail>()
                .eq(QuoteDetail::getQuoteId, quoteId)
                .eq(QuoteDetail::getDetailVersion, quoteVer));

        Integer oldBusinessVersion = order.getCurrentBusinessVersion() == null ? 0 : order.getCurrentBusinessVersion();
        Integer newBusinessVersion = oldBusinessVersion + 1;

        List<QuoteBusinessItem> businessItems = oldBusinessVersion == 0
                ? businessVersionInitializer.initFirstBusinessVersion(quoteId, quoteDetails, newBusinessVersion)
                : businessVersionInitializer.initFromReQuotedVersion(quoteId, quoteDetails, oldBusinessVersion, newBusinessVersion);

        businessItems.forEach(quoteBusinessMapper::insert);

        LambdaUpdateWrapper<QuoteOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(QuoteOrder::getStatus, QuoteStatus.PENDING_BUSINESS)
                .set(QuoteOrder::getCurrentHandlerId, nextHandlerId)
                .set(QuoteOrder::getCurrentBusinessVersion, newBusinessVersion)
                .eq(QuoteOrder::getId, quoteId)
                .eq(QuoteOrder::getStatus, QuoteStatus.PENDING_AUDIT)
                .eq(QuoteOrder::getCurrentHandlerId, SecurityUtils.getUserId());

        int rows = quoteOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ServiceException("审核通过失败：状态冲突");
        }

        quoteLogService.record(quoteId, LogAction.AUDIT_PASS, nextHandlerId,
                "审核通过，转交业务版本 v" + newBusinessVersion + ": " + comment);
        eventPublisher.publishEvent(new QuoteStateChangeEvent(this, quoteId, LogAction.AUDIT_PASS.name()));
    }

    /**
     * 审核员退回报价员
     * 场景2: 审核驳回 (1 -> 4)
     * @param quoteId 报价单id
     * @param comment 备注
     */
    @Override
    public void auditReject(Long quoteId, String comment) {
        QuoteOrder order = quoteManageService.getByIdSafe(quoteId);
        checkPermission(order);

        if (!QuoteStatus.PENDING_AUDIT.equals(order.getStatus())) {
            throw new ServiceException("当前状态不是待审核状态");
        }

        // --- 核心修改开始 ---
        // 默认退回给创建人 (兜底)
        Long targetId = order.getCreateBy();

        // 查找最近一次 "提交审核" 的日志记录
        QuoteLog lastSubmitLog = quoteLogMapper.selectOne(new LambdaQueryWrapper<QuoteLog>()
                .select(QuoteLog::getOperatorId) // 只查ID字段优化性能
                .eq(QuoteLog::getQuoteId, quoteId)
                .eq(QuoteLog::getAction, LogAction.SUBMIT_AUDIT.name()) // 筛选动作
                .orderByDesc(QuoteLog::getCreateTime) // 按时间倒序
                .last("LIMIT 1")); // 只取第一条

        if (lastSubmitLog != null) {
            targetId = lastSubmitLog.getOperatorId();
        }
        // CAS 更新主表 (状态 -> 4)
        boolean success = casUpdateStatus(
                quoteId,
                QuoteStatus.PENDING_AUDIT,
                QuoteStatus.REJECT_TO_QUOTER,
                targetId
        );

        if (!success) {
            throw new ServiceException("驳回失败：单据状态已变更");
        }

        quoteLogService.record(quoteId, LogAction.AUDIT_REJECT, targetId, "审核驳回: " + comment);
        //广播审批状态修改
        eventPublisher.publishEvent(new QuoteStateChangeEvent(this, quoteId, LogAction.AUDIT_REJECT.name()));
    }

    /**
     * 报价员创建新的报价版本，原报价直接复制
     * 场景3: 报价员创建新版本 (4 -> 4, version++)
     * 前置条件：状态为4，且当前版本已锁定
     * @param quoteId 报价单id
     */
    @Override
    public void createNewQuoteVersion(Long quoteId) {
        QuoteOrder order = quoteManageService.getByIdSafe(quoteId);
        checkPermission(order);

        if (!QuoteStatus.REJECT_TO_QUOTER.equals(order.getStatus())&&!QuoteStatus.REQUOTE_FROM_COMPLETED.equals(order.getStatus())) {
            throw new ServiceException("只有[待重新报价]状态才能创建新版本");
        }

        Integer oldVer = order.getCurrentQuoteVersion();

        Integer newVer = oldVer + 1;
        quoteVersionInitializer.cloneQuoteDetailsToNewVersion(quoteId, oldVer, newVer);

        // 2. CAS 更新主表版本号 (状态不变，还是 4)
        // 注意：这里不用 casUpdateStatus，因为状态没变，但要防止并发
        LambdaUpdateWrapper<QuoteOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(QuoteOrder::getCurrentQuoteVersion, newVer)
                .eq(QuoteOrder::getId, quoteId)
                .eq(QuoteOrder::getStatus, order.getStatus())
                .eq(QuoteOrder::getCurrentHandlerId, SecurityUtils.getUserId());

        int rows = quoteOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ServiceException("创建新版本失败：状态冲突");
        }

        quoteLogService.record(quoteId, LogAction.CREATE_VERSION, null, "创建新报价版本 v" + newVer);
    }


    /**
     * 业务员调整报价完毕
     *
     * @param quoteId 报价单id
     * @param comment 备注
     */
    @Override
    public void finishBusiness(Long quoteId, String comment) {
        QuoteOrder order = quoteManageService.getByIdSafe(quoteId);
        checkPermission(order);

        // [修改点1]：校验逻辑放宽，允许“待业务调整”或“重新调整中”
        // 假设 QuoteStatus.RE_ADJUSTING 为状态 5
        boolean canFinish = QuoteStatus.PENDING_BUSINESS.equals(order.getStatus())
                || QuoteStatus.RE_ADJUST_BUSINESS.equals(order.getStatus());
        if (!canFinish) {
            throw new ServiceException("当前状态不可执行完成操作");
        }

        // 1. 锁定当前业务版本
        Integer busVer = order.getCurrentBusinessVersion();
        quoteBusinessMapper.update(null, new LambdaUpdateWrapper<QuoteBusinessItem>()
                .set(QuoteBusinessItem::getLockStatus, 1)
                .eq(QuoteBusinessItem::getQuoteId, quoteId)
                .eq(QuoteBusinessItem::getBusinessVersion, busVer));

        // 2. CAS 更新主表 (状态 -> 3, 处理人 -> NULL)
        LambdaUpdateWrapper<QuoteOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(QuoteOrder::getStatus, QuoteStatus.COMPLETED)
                .set(QuoteOrder::getCurrentHandlerId, null) // 清空处理人
                .eq(QuoteOrder::getId, quoteId)
                .eq(QuoteOrder::getCurrentHandlerId, SecurityUtils.getUserId())
                .in(QuoteOrder::getStatus, Arrays.asList(QuoteStatus.PENDING_BUSINESS, QuoteStatus.RE_ADJUST_BUSINESS));

        int rows = quoteOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ServiceException("操作失败：并发冲突");
        }

        // 3. (模拟) 推送外部系统
        log.info("推送报价单[{}]数据到外部系统...", quoteId);

        quoteLogService.record(quoteId, LogAction.COMPLETED, null, "业务调整完成，版本 v" + busVer+":"+comment);
    }

    /**
     * 业务员重新创建调整报价信息
     * 场景6: 业务重新调整 (3 -> 5)
     * 特殊权限：状态为3时，Handler为null，需允许业务员角色操作
     * @param quoteId 报价单id
     */
    @Override
    public void reAdjustBusiness(Long quoteId) {
        QuoteOrder order = quoteManageService.getByIdSafe(quoteId);

        // 特殊权限校验
        if (!QuoteStatus.COMPLETED.equals(order.getStatus())) {
            throw new ServiceException("只有[已完成]状态才能重新调整");
        }
        // --- 核心权限校验开始 ---
        // 管理员直接放行
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = SecurityUtils.getUserId();
            Long lastFinisherId = null;
            String lastFinisherName = "未知用户";

            // 查找最近一次 "确认完成" 的日志记录
            QuoteLog lastFinishLog = quoteLogMapper.selectOne(new LambdaQueryWrapper<QuoteLog>()
                    .select(QuoteLog::getOperatorId, QuoteLog::getOperatorName)
                    .eq(QuoteLog::getQuoteId, quoteId)
                    .eq(QuoteLog::getAction, LogAction.COMPLETED.name()) // 筛选动作
                    .orderByDesc(QuoteLog::getCreateTime) // 找最新的
                    .last("LIMIT 1"));

            if (lastFinishLog != null) {
                lastFinisherId = lastFinishLog.getOperatorId();
                lastFinisherName = lastFinishLog.getOperatorName();
            }

            // 校验：当前用户是否是最后完成人
            // 注意：如果历史数据没有日志，lastFinisherId为null，这里会禁止操作(除了管理员)
            if (!Objects.equals(currentUserId, lastFinisherId)) {
                throw new ServiceException("无权操作：该单据由【" + lastFinisherName + "】确认完成，只能由其本人发起调整");
            }
        }
        Long currentUserId = SecurityUtils.getUserId();

        Integer oldVer = order.getCurrentBusinessVersion();
        Integer newVer = oldVer + 1;
        List<QuoteBusinessItem> newItems = businessVersionInitializer.cloneForReAdjustBusiness(quoteId, oldVer, newVer);
        newItems.forEach(quoteBusinessMapper::insert);

        // 2. CAS 更新主表 (状态 -> 5, 处理人 -> 我)
        LambdaUpdateWrapper<QuoteOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(QuoteOrder::getStatus, QuoteStatus.RE_ADJUST_BUSINESS)
                .set(QuoteOrder::getCurrentBusinessVersion, newVer)
                .set(QuoteOrder::getCurrentHandlerId, currentUserId) // 抢回单据
                .eq(QuoteOrder::getId, quoteId)
                .eq(QuoteOrder::getStatus, QuoteStatus.COMPLETED); // 旧状态

        int rows = quoteOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ServiceException("重开失败：状态已被变更");
        }

        quoteLogService.record(quoteId, LogAction.RE_OPEN, null, "业务重新调整，创建版本 v" + newVer);
    }


    @Override
    public void withdrawToQuote(Long quoteId) {
        QuoteOrder order = quoteManageService.getByIdSafe(quoteId);

        List<String> allowed = Arrays.asList(
                QuoteStatus.PENDING_AUDIT,
                QuoteStatus.PENDING_BUSINESS,
                QuoteStatus.RE_ADJUST_BUSINESS,
                QuoteStatus.COMPLETED,
                QuoteStatus.REJECT_TO_QUOTER,
                QuoteStatus.REQUOTE_FROM_COMPLETED
        );
        if (!allowed.contains(order.getStatus())) {
            throw new ServiceException("当前状态不可撤回到报价阶段");
        }

        Long currentUserId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin() && !QuoteStatus.COMPLETED.equals(order.getStatus())
                && !Objects.equals(currentUserId, order.getCurrentHandlerId())) {
            throw new ServiceException("您没有权限操作此单据");
        }

        if (QuoteStatus.COMPLETED.equals(order.getStatus()) && !SecurityUtils.isAdmin()) {
            QuoteLog lastFinishLog = quoteLogMapper.selectOne(new LambdaQueryWrapper<QuoteLog>()
                    .select(QuoteLog::getOperatorId)
                    .eq(QuoteLog::getQuoteId, quoteId)
                    .eq(QuoteLog::getAction, LogAction.COMPLETED.name())
                    .orderByDesc(QuoteLog::getCreateTime)
                    .last("LIMIT 1"));
            Long lastFinisherId = lastFinishLog == null ? null : lastFinishLog.getOperatorId();
            if (!Objects.equals(currentUserId, lastFinisherId)) {
                throw new ServiceException("无权操作：只能由最后完成人或管理员撤回");
            }
        }

        Long targetId = quoteVersionInitializer.resolveReturnQuoterId(quoteId, order.getCreateBy());

        LambdaUpdateWrapper<QuoteOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(QuoteOrder::getStatus, QuoteStatus.DRAFT)
                .set(QuoteOrder::getCurrentHandlerId, targetId)
                .eq(QuoteOrder::getId, quoteId)
                .eq(QuoteOrder::getStatus, order.getStatus());
        if (!QuoteStatus.COMPLETED.equals(order.getStatus())) {
            wrapper.eq(QuoteOrder::getCurrentHandlerId, currentUserId);
        }

        int rows = quoteOrderMapper.update(null, wrapper);
        if (rows == 0) {
            throw new ServiceException("撤回失败：单据状态已变更");
        }

        quoteLogService.record(quoteId, LogAction.RETURN_TO_QUOTE, targetId, "流程撤回到报价阶段");
        eventPublisher.publishEvent(new QuoteStateChangeEvent(this, quoteId, LogAction.RETURN_TO_QUOTE.name()));
    }

    /**
     * 权限校验 (增强版)
     */
    private void checkPermission(QuoteOrder order) {
        // 1. 管理员放行
        if (SecurityUtils.isAdmin()) {
            return;
        }

        // 2. 已完成状态校验在 reAdjustBusiness 单独处理，这里拦截非法的通用操作
        if (QuoteStatus.COMPLETED.equals(order.getStatus())) {
            throw new ServiceException("单据已归档，禁止常规操作");
        }

        // 3. 校验处理人
        Long currentUserId = SecurityUtils.getUserId();
        if (!Objects.equals(currentUserId, order.getCurrentHandlerId())) {
            throw new ServiceException("您没有权限操作此单据");
        }
    }

    /**
     * 通用 CAS 状态更新
     * @param oldStatus 期望的旧状态 (用于乐观锁)
     * @param newStatus 新状态
     * @param nextHandlerId 新处理人 (可为null)
     */
    private boolean casUpdateStatus(Long id, String oldStatus, String newStatus, Long nextHandlerId) {
        Long currentUserId = SecurityUtils.getUserId();
        LambdaUpdateWrapper<QuoteOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(QuoteOrder::getStatus, newStatus)
                .set(QuoteOrder::getCurrentHandlerId, nextHandlerId)
                .eq(QuoteOrder::getId, id)
                .eq(QuoteOrder::getStatus, oldStatus)
                .eq(QuoteOrder::getCurrentHandlerId, currentUserId); // 确保还是我在处理

        return quoteOrderMapper.update(null, wrapper) > 0;
    }
}
