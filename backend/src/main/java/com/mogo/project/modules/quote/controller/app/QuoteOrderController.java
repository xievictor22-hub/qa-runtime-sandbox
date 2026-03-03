package com.mogo.project.modules.quote.controller.app;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.quote.domain.basePrice.dto.QuoteExportDto;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.process.entity.QuoteLog;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailService;
import com.mogo.project.modules.quote.domain.process.service.IQuoteLogService;
import com.mogo.project.modules.quote.domain.order.service.IQuoteManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "报价单-基础管理")
@RestController
@RequestMapping("/quote/order") // -> quote:order
@RequiredArgsConstructor
public class QuoteOrderController {

    private final IQuoteManageService manageService;
    private final IQuoteLogService logService; // 假设日志查询独立
    private final IQuoteDetailService detailService;



    @Anonymous
    @PreAuthorize("hasAuthority('quote:order:create')")
    @Operation(summary = "创建")
    @PostMapping("/create")
    public ApiResponse<Boolean> create(@RequestBody QuoteOrder quote) {
        return ApiResponse.success(manageService.createQuote(quote));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:order:update')")
    @Operation(summary = "修改基础信息(备注/项目名)")
    @Log(title = "修改报价单基础信息", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public ApiResponse<Boolean> update(@RequestBody QuoteOrder quote) {
        return ApiResponse.success(manageService.updateById(quote));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:order:query')")
    @Operation(summary = "列表查询")
    @GetMapping("/list")
    public ApiResponse<IPage<QuoteOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            QuoteOrder query,
            @RequestParam(required = false) String queryType) {
        Page<QuoteOrder> page = new Page<>(pageNum, pageSize);
        if ("todo".equals(queryType)) return ApiResponse.success(manageService.selectTodoList(page, query));
        return ApiResponse.success(manageService.selectOrderPage(page, query));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:order:query')")
    @Operation(summary = "单个查询")
    @GetMapping("/query/{quoteId}")
    public ApiResponse<QuoteOrder> list(
            @PathVariable Long quoteId){
        return ApiResponse.success(manageService.getByIdSafe(quoteId));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:order:query')")
    @Operation(summary = "获取报价单详情")
    @GetMapping("/{id}")
    public ApiResponse<QuoteOrder> getInfo(@PathVariable Long id) {
        return ApiResponse.success(manageService.getDetailWithPerms(id));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:order:logs')")
    @Operation(summary = "履历")
    @GetMapping("/{id}/logs")
    public ApiResponse<List<QuoteLog>> getLogs(@PathVariable Long id) {
        return ApiResponse.success(logService.selectLogListByQuoteId(id));
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:order:export')")
    @Log(title = "导出报价单", businessType = BusinessType.EXPORT)
    @Operation(summary = "导出报价单")
    @PostMapping("/export/{id}") // -> quote:order:export
    public void export(@PathVariable Long id,
                       @RequestBody(required = false) List<String> selectedColumns,
                       HttpServletResponse response)  {

        QuoteOrder order = manageService.getByIdSafe(id);

        // 获取当前版本的明细
        List<QuoteDetail> details = detailService.list(new LambdaQueryWrapper<QuoteDetail>()
                .eq(QuoteDetail::getQuoteId, id)
                .eq(QuoteDetail::getDetailVersion, order.getCurrentQuoteVersion())
                .orderByAsc(QuoteDetail::getRowNum));

        // 转换 DTO
        List<QuoteExportDto> exportList = details.stream().map(d -> {
            QuoteExportDto dto = new QuoteExportDto();
            BeanUtils.copyProperties(d, dto);
            return dto;
        }).collect(Collectors.toList());

        // 导出逻辑
        InputStream templateStream = this.getClass().getResourceAsStream("/templates/quote_template.xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = null;
        try {
            fileName = URLEncoder.encode("报价单_" + order.getProjectName(), "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        ExcelWriter excelWriter = null;
        try {
            try {
                excelWriter = EasyExcel.write(response.getOutputStream(), QuoteExportDto.class)
                        .withTemplate(templateStream)
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            WriteSheet writeSheet = EasyExcel.writerSheet("报价明细")
                    .includeColumnFieldNames(selectedColumns != null && !selectedColumns.isEmpty() ? new HashSet<>(selectedColumns) : null)
                    .build();
            excelWriter.write(exportList, writeSheet);
        } finally {
            if (excelWriter != null) excelWriter.finish();
        }
    }
}
