package com.mogo.project.modules.quote.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.domain.order.manager.QuoteDetailImportManager;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailService;
import com.mogo.project.modules.quote.domain.order.service.IQuoteManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "报价单-明细主行管理")
@RestController
@RequestMapping("/quote/detail") // 权限映射 -> quote:detail:xxx
@RequiredArgsConstructor
public class QuoteDetailController {

    private final IQuoteDetailService detailService;
    private final IQuoteManageService manageService;
    private final QuoteDetailImportManager quoteDetailImportManager;

    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:import')")
    @Operation(summary = "导入明细(覆盖上传)")
    @Log(title = "导入报价单明细", businessType = BusinessType.IMPORT)
    @PostMapping("/{quoteId}/import") // 映射权限 -> quote:detail:import
    public ApiResponse<String> importExcel(@PathVariable Long quoteId,
                                           @RequestPart("file") MultipartFile file) {
        // Service内部会校验：只有[待报价]或[待重新报价]状态可导入
        quoteDetailImportManager.importQuoteDetail(quoteId, file);
        return ApiResponse.success("导入成功");
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:list')")
    @Operation(summary = "查询明细列表(当前版本)")
    @GetMapping("/{quoteId}/list") // 映射权限 -> quote:detail:list
    public ApiResponse<List<QuoteDetail>> list(@PathVariable Long quoteId) {
        // 1. 获取主表信息，确定当前版本号
        QuoteOrder order = manageService.getByIdSafe(quoteId);
        // 2. 查询对应版本的明细
        return ApiResponse.success(detailService.list(new LambdaQueryWrapper<QuoteDetail>()
                .eq(QuoteDetail::getQuoteId, quoteId)
                .eq(QuoteDetail::getDetailVersion, order.getCurrentQuoteVersion()) // 关键：只查当前版本
                .orderByAsc(QuoteDetail::getRowNum)));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:list')")
    @Operation(summary = "查询明细列表(当前版本)")
    @GetMapping("/{detailId}") // 映射权限 -> quote:detail:list
    public ApiResponse<QuoteDetail> selectOne(@PathVariable Long detailId) {
        // 2. 查询

        return ApiResponse.success(detailService.getById(detailId));
    }


    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:update')")
    @Operation(summary = "修改单行明细")
    @Log(title = "修改报价单明细", businessType = BusinessType.UPDATE)
    @PutMapping("/update") // 映射权限 -> quote:detail:update
    public ApiResponse<Boolean> update(@RequestBody QuoteDetail detail) {
        // Service层建议添加逻辑：校验是否处于可编辑状态
        return ApiResponse.success(detailService.updateById(detail));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:detail:remove')")
    @Log(title = "产出报价单明细", businessType = BusinessType.DELETE)
    @Operation(summary = "删除单行明细")
    @DeleteMapping("/{id}") // 映射权限 -> quote:detail:remove
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        // Service层需实现级联删除：删除detail时，同时删除关联的detail_item
        return ApiResponse.success(detailService.removeDetailAndItems(id));
    }
}