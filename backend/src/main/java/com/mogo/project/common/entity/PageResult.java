package com.mogo.project.common.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "列表数据")
    private List<T> rows;

    @Schema(description = "消息状态码")
    private int code = 200;

    @Schema(description = "消息内容")
    private String msg = "查询成功";

    /**
     * 构造函数
     */
    public PageResult(List<T> rows, long total) {
        this.rows = rows;
        this.total = total;
    }

    /**
     * 【基础版】直接构建（类型一致时使用）
     */
    public static <T> PageResult<T> build(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    /**
     * 【进阶版】构建并转换类型 (PO -> VO)
     * 使用示例: PageResult.build(page, UserVO::new)
     * 或者配合 MapStruct: PageResult.build(page, userMapper::toVo)
     */
    public static <T, R> PageResult<R> build(IPage<T> page, Function<T, R> mapper) {
        List<R> collect = page.getRecords().stream()
                .map(mapper)
                .collect(Collectors.toList());
        return new PageResult<>(collect, page.getTotal());
    }
    //function 的参数为入参与出参
    public static <T, R> PageResult<R> buildList(IPage<T> page, java.util.function.Function<List<T>,List<R>> mapper) {
        List<R> collect = mapper.apply(page.getRecords());
        return new PageResult<>(collect, page.getTotal());
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0);
    }
}