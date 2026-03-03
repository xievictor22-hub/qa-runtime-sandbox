package com.mogo.project.modules.quote.domain.order.vo;

import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuoteOrderVo extends QuoteOrder {

    /**
     * 【核心】动作权限集合
     * 包含: submit, cost-edit, audit-pass, business-edit, finish, reopen ...
     */
    private Set<String> actionPermissions;

    private String createByName; // 辅助字段

    private String currentHandlerName; // 辅助字段
}