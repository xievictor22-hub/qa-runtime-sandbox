package com.mogo.project.modules.quote.domain.process.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.common.util.SecurityUtils;

import com.mogo.project.modules.quote.core.enums.LogAction;
import com.mogo.project.modules.quote.domain.process.mapper.QuoteLogMapper;
import com.mogo.project.modules.quote.domain.process.entity.QuoteLog;
import com.mogo.project.modules.quote.domain.process.service.IQuoteLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报价履历服务实现类
 */
@Service
@RequiredArgsConstructor
public class QuoteLogServiceImpl extends ServiceImpl<QuoteLogMapper, QuoteLog> implements IQuoteLogService {


    private final QuoteLogMapper quoteLogMapper;

    @Override
    public List<QuoteLog> selectLogListByQuoteId(Long id) {
        return quoteLogMapper.selectLogListByQuoteId(id);
    }

    /**
     * 记录通用日志 (操作人 = 当前登录用户，接收人 = NULL)
     *
     * @param quoteId 报价单ID
     * @param action  动作枚举
     * @param comment 备注内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(Long quoteId, LogAction action, String comment) {
        this.record(quoteId, action, null, comment);
    }

    /**
     * 记录流转日志 (指定接收人)
     *
     * @param quoteId    报价单ID
     * @param action     动作枚举
     * @param receiverId 接收人ID (可为null)
     * @param comment    备注内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(Long quoteId, LogAction action, Long receiverId, String comment) {
        // 统一获取当前操作人信息
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = "系统自动";

        try {
            // 防止定时任务或系统调用时没有登录上下文导致报错
            if (operatorId != null) {
                operatorName = SecurityUtils.getLoginUser().getSysUser().getNickname();
            }
        } catch (Exception e) {
            // 忽略获取用户失败，保持默认值
        }

        // 构建日志对象
        QuoteLog log = QuoteLog.builder()
                .quoteId(quoteId)
                .action(action.name()) // 枚举转字符串
                .operatorId(operatorId)
                .operatorName(operatorName)
                .receiverId(receiverId)
                // .receiverName() // 接收人名字通常前端关联显示，或者这里额外查一次库
                .comment(comment)
                .createTime(LocalDateTime.now())
                .build();

        // 插入数据库
        baseMapper.insert(log);
    }

}