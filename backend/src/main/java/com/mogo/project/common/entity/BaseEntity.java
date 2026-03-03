package com.mogo.project.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 实体父类
 * 包含所有表共有的字段
 */
@Data
public class BaseEntity implements Serializable {

    @TableId(type = IdType.AUTO) // 假设主键自增，如果是分布式ID可用 ASSIGN_ID
    private Long id;

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    // 逻辑删除 (0:未删除, 1:已删除)
    @TableLogic
    private Integer deleteFlag;
//    private Integer isDeleted;

    private String remark;

    /** 请求参数 (用于数据权限传参) */
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();

}