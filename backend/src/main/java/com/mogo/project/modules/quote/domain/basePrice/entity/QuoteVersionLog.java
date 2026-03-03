package com.mogo.project.modules.quote.domain.basePrice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 底价表版本日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_version_log")
public class QuoteVersionLog extends BaseEntity {

    /** 版本号(如 A0018) */
    private String version;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 包含数据行数 */
    private Integer recordCount;

    /** 发布类型(1:新版本, 2:增量补丁) */
    private Integer publishType;

    /** 状态: 1-活跃, 0-已废弃 */
    private Integer status;

    /** 是否最新版本 */
    private Integer isLatest;

    /** 发布日志 */
    private String changeDesc;
}