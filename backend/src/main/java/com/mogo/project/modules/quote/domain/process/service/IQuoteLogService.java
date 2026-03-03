package com.mogo.project.modules.quote.domain.process.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.quote.core.enums.LogAction;
import com.mogo.project.modules.quote.domain.process.entity.QuoteLog;

import java.util.List;

/**
 * 报价履历服务接口
 */
public interface IQuoteLogService extends IService<QuoteLog> {


    /**
     * 搜索日志
     * @param id 报价单id
     * @return 日志列表
     */
    List<QuoteLog> selectLogListByQuoteId(Long id);

    /**
     * 记录通用日志 (操作人 = 当前登录用户，接收人 = NULL)
     * @param quoteId 报价单ID
     * @param action 动作枚举
     * @param comment 备注内容
     */
    void record(Long quoteId, LogAction action, String comment);

    /**
     * 记录流转日志 (指定接收人)
     * @param quoteId 报价单ID
     * @param action 动作枚举
     * @param receiverId 接收人ID (可为null)
     * @param comment 备注内容
     */
    void record(Long quoteId, LogAction action, Long receiverId, String comment);


}