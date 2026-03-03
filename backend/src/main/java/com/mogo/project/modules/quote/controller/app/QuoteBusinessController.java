package com.mogo.project.modules.quote.controller.app;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.quote.domain.process.dto.QuoteProcessDto;
import com.mogo.project.modules.quote.domain.order.entity.QuoteBusinessItem;
import com.mogo.project.modules.quote.domain.order.service.IQuoteBusinessService;
import com.mogo.project.modules.quote.domain.process.service.IQuoteProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "报价单-业务调整")
@RestController
@RequestMapping("/quote/business") // -> quote:business
@RequiredArgsConstructor
public class QuoteBusinessController {

    private final IQuoteBusinessService businessService;
    private final IQuoteProcessService processService; // 跨域调用流程服务

    @Anonymous
    @PreAuthorize("hasAuthority('quote:business:query')")
    @Operation(summary = "获取业务明细")
    @GetMapping("/{quoteId}/items")
    public ApiResponse<List<QuoteBusinessItem>> getItems(@PathVariable Long quoteId) {
        return ApiResponse.success(businessService.listCurrentVersion(quoteId));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:business:save')")
    @Operation(summary = "保存业务价格")
    @Log(title = "保存业务价格", businessType = BusinessType.UPDATE)
    @PutMapping("/items/batch-save")
    public ApiResponse<Void> saveItems(@RequestBody List<QuoteBusinessItem> items) {
        businessService.batchUpdateBusinessItems(items);
        return ApiResponse.success();
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:business:finish')")
    @Log(title = "完成报价单", businessType = BusinessType.UPDATE)
    @Operation(summary = "确认完成")
    @PostMapping("/finish")
    public ApiResponse<Void> finish(@RequestBody QuoteProcessDto dto) {
        processService.finishBusiness(dto.getId(), dto.getAuditComment());
        return ApiResponse.success();
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:business:re-adjust')")
    @Operation(summary = "重新调整")
    @PostMapping("/re-adjust/{id}")
    public ApiResponse<Void> reAdjust(@PathVariable Long id) {
        processService.reAdjustBusiness(id);
        return ApiResponse.success();
    }
}