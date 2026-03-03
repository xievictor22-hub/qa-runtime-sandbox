package com.mogo.project.modules.quote.domain.process.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 履历表
 */
@Data
@Builder
@TableName("quote_log")
@NoArgsConstructor
@AllArgsConstructor
public class QuoteLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long quoteId;
    /** 动作: CREATE, SUBMIT, AUDIT_PASS, AUDIT_REJECT, ASSIGN */
    private String action;
    private Long operatorId;
    private String operatorName;
    private Long receiverId;
    private String receiverName;
    private String comment;
    private LocalDateTime createTime;
}