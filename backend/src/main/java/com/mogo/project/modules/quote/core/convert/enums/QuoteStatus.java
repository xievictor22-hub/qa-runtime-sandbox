package com.mogo.project.modules.quote.core.convert.enums;

/**
 * 报价单状态信息
 */
public interface QuoteStatus {
    String DRAFT = "0";             // 待报价
    String PENDING_AUDIT = "1";     // 待审核
    String PENDING_BUSINESS = "2";  // 待业务调整
    String COMPLETED = "3";         // 已完成
    String REJECT_TO_QUOTER = "4";  // 待重新报价 (审核退回)
    String RE_ADJUST_BUSINESS = "5";// 待业务重新调整 (已完成后重开)
}