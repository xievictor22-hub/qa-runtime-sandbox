package com.mogo.project.modules.quote.core.component;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mogo.project.common.util.SecurityUtils;
import com.mogo.project.modules.quote.core.convert.enums.QuoteStatus;
import com.mogo.project.modules.quote.domain.process.mapper.QuoteLogMapper;
import com.mogo.project.modules.quote.domain.process.entity.QuoteLog;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 核心，计算表单操作权限
 */
@Service
@RequiredArgsConstructor
public class QuotePermissionService {

    private final QuoteLogMapper quoteLogMapper;

    /**
     * 计算单据的所有动态权限
     */
    public Set<String> calcPermissions(QuoteOrder order) {
        Set<String> perms = new HashSet<>();
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin();

        // 1. 身份判定
        boolean isHandler = userId.equals(order.getCurrentHandlerId()); // 当前持有人
        //根据提交报价操作与报价单id查提交业务员id列表
        List<QuoteLog> quoteLogList = quoteLogMapper.selectList(new LambdaQueryWrapper<QuoteLog>()
                .eq(QuoteLog::getQuoteId, order.getId())
                .eq(QuoteLog::getAction, "COMPLETED")
                .select(QuoteLog::getOperatorId));
        //提交的业务员列表
        List<Long> handledSales = quoteLogList.stream().map(QuoteLog::getOperatorId).collect(Collectors.toList());
        boolean isLastFinisher = handledSales.contains(userId);// 历史完成人

        String status = order.getStatus();

        // 2. 状态机权限映射
        // --- 阶段1: 报价草稿/驳回 ---
        if (QuoteStatus.DRAFT.equals(status) || QuoteStatus.REJECT_TO_QUOTER.equals(status)) {
            if (isHandler || isAdmin) {
                perms.add("cost-edit");   // 允许编辑成本/导入
                perms.add("submit");      // 允许提交
                if (QuoteStatus.DRAFT.equals(status)) perms.add("delete");
                if (QuoteStatus.REJECT_TO_QUOTER.equals(status)) perms.add("new-version");
            }
        }

        // --- 阶段2: 审核中 ---
        else if (QuoteStatus.PENDING_AUDIT.equals(status)) {
            if (isHandler || isAdmin) {
                perms.add("audit-pass");
                perms.add("audit-reject");
            }
        }

        // --- 阶段3: 业务调整 ---
        else if (QuoteStatus.PENDING_BUSINESS.equals(status) || QuoteStatus.RE_ADJUST_BUSINESS.equals(status)) {
            if (isHandler || isAdmin) {
                perms.add("business-edit"); // 允许修改折扣
                perms.add("finish");        // 允许确认完成
            }
        }

        // --- 阶段4: 已完成 ---
        else if (QuoteStatus.COMPLETED.equals(status)) {
            // 只有 管理员 或 最后完成人 可重开
            if (isAdmin || isLastFinisher) {
                perms.add("reopen");
            }
        }

        // 全局通用
        perms.add("view-log"); // 既然能看详情，就能看履历

        return perms;
    }
}