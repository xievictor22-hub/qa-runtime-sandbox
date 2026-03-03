package com.mogo.project.common.entity;


import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.common.util.MStringUtils;
import com.mogo.project.common.util.SqlUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 分页查询基类 (进阶版)
 */
@Data
public class BasePageQuery {

    @Schema(description = "当前页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100") // 防止恶意大查询
    private Integer pageSize = 10;

    @Schema(description = "排序字段", example = "create_time")
    private String orderByColumn;

    @Schema(description = "排序方向", example = "desc")
    private String isAsc;

    /**
     * 请求参数 (用于数据权限、时间范围等扩展参数)
     * 前端传 params[beginTime], params[endTime]
     */
    @Schema(description = "请求参数")
    private Map<String, Object> params = new HashMap<>();

    /**
     * 转换为 MyBatis-Plus 分页对象
     */
    public <T> Page<T> toMpPage() {
        Page<T> page = new Page<>(pageNum, pageSize);

        // 优化排序逻辑：防止 SQL 注入 + 驼峰转下划线
        if (MStringUtils.isNotBlank(orderByColumn)) {
            String orderBy = SqlUtil.escapeOrderBySql(orderByColumn); // 核心安全检查
            // 简单处理：将 camelCase 转 underscore (如果数据库字段是下划线风格)
            orderBy = MStringUtils.toUnderScoreCase(orderBy);

            page.addOrder("asc".equals(isAsc)
                    ? OrderItem.asc(orderBy)
                    : OrderItem.desc(orderBy));
        }
        return page;
    }
}