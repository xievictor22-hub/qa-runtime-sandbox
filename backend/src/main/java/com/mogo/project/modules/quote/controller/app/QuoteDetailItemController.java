package com.mogo.project.modules.quote.controller.app;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.response.ApiResponse;

import com.mogo.project.modules.quote.domain.order.entity.QuoteDetailItem;
import com.mogo.project.modules.quote.domain.order.vo.QuoteDetailItemVo;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "报价单-制品子件管理")
@RestController
@RequestMapping("/quote/item") // 权限映射 -> quote:item:xxx
@RequiredArgsConstructor
public class QuoteDetailItemController {

    private final IQuoteDetailItemService itemService;

    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:update')")
    @Operation(summary = "查询子件列表")
    @GetMapping("/list/{detailId}") // 映射权限 -> quote:item:list
    public ApiResponse<List<QuoteDetailItemVo>> list(@PathVariable Long detailId) {
        // 根据父行ID查询所有子件
        return ApiResponse.success(itemService.listByDetailId(detailId));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:update')")
    @Log(title = "添加组价明细", businessType = BusinessType.INSERT)
    @Operation(summary = "添加子件")
    @PostMapping("/add") // 映射权限 -> quote:item:add
    public ApiResponse<Void> add(@RequestBody QuoteDetailItem quoteDetailItem) {
        // 核心逻辑：添加子件 -> 自动计算并更新父级Detail的总价
        itemService.addItem(quoteDetailItem);
        return ApiResponse.success(null);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:update')")
    @Log(title = "修改组价明细", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改子件")
    @PutMapping("/update") // 映射权限 -> quote:item:update
    public ApiResponse<Void> update(@RequestBody QuoteDetailItem item) {
        // 核心逻辑：修改子件单价与数量 -> 自动计算并更新父级Detail的总价
        itemService.updateItem(item);
        return ApiResponse.success(null);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:update')")
    @Log(title = "删除组价明细", businessType = BusinessType.DELETE)
    @Operation(summary = "删除子件")
    @DeleteMapping("/{id}") // 映射权限 -> quote:item:remove
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // 核心逻辑：删除子件 -> 自动计算并更新父级Detail的总价
        itemService.deleteItem(id);
        return ApiResponse.success(null);
    }
}