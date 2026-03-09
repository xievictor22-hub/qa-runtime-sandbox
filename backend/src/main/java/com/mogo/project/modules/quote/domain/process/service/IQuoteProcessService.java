package com.mogo.project.modules.quote.domain.process.service;

/**
 * 业务流程服务类
 */
public interface IQuoteProcessService {
    /**
     * 报价员提交报价审核
     * @param quoteId 报价单id
     * @param nextHandlerId 审核接收人
     * @param comment 备注
     */
    void submitAudit(Long quoteId, Long nextHandlerId, String comment);

    /**
     * 审核员审核报价
     * @param quoteId 报价单id
     * @param nextHandlerId 业务接收人
     * @param comment 备注
     */
    void auditPass(Long quoteId, Long nextHandlerId, String comment);

    /**
     * 审核员退回报价员
     * @param quoteId 报价单id
     * @param comment 备注
     */
    void auditReject(Long quoteId, String comment);

    /**
     * 报价员创建新的报价版本，原报价直接复制
     * @param quoteId 报价单id
     */
    void createNewQuoteVersion(Long quoteId);

    /**
     * 业务员调整报价完毕
     * @param quoteId 报价单id
     * @param comment 备注
     */
    void finishBusiness(Long quoteId, String comment);

    /**
     * 业务员重新创建调整报价信息
     * @param quoteId 报价单id
     */
    void reAdjustBusiness(Long quoteId);

    /**
     * 撤回到报价阶段
     * @param quoteId 报价单id
     */
    void withdrawToQuote(Long quoteId);

}
