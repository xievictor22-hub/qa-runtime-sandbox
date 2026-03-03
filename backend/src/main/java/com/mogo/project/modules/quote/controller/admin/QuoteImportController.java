package com.mogo.project.modules.quote.controller.admin;



import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.quote.domain.basePrice.dto.QuoteBasePriceImportExcelDto;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "报价导入管理")
@RestController
@RequestMapping("/quote/import")
@RequiredArgsConstructor
public class QuoteImportController {

    private final IQuoteImportService importService;

    @Anonymous
    @PreAuthorize("hasAuthority('quote:import:upload')")
    @Operation(summary = "上传报价Excel")
    @Log(title = "报价导入", businessType = BusinessType.IMPORT)
    @PostMapping("/upload")
    public ApiResponse<Void> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("version") String version,
            @RequestParam(value = "description", required = false) String description
    ) {
        importService.importQuoteExcel(file, version, description);
        return ApiResponse.success();
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:import:upload')")
    @Operation(summary = "下载报价Excel")
    @Log(title = "报价导出", businessType = BusinessType.EXPORT)
    @PostMapping("/download/{version}")
    public void downLoad(@PathVariable("version") String version, HttpServletResponse response) {
        importService.downLoadQuoteExcel(version,response);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:import:upload')")
    @Operation(summary = "添加单个条目")
    @Log(title = "添加单个条目", businessType = BusinessType.IMPORT)
    @PostMapping("/add/{version}")
    public ApiResponse<Void> add(@RequestBody @Validated QuoteBasePriceImportExcelDto quoteBasePriceImportExcelDto, @PathVariable String version) {
        importService.add(quoteBasePriceImportExcelDto,version);
        return ApiResponse.success();
    }

}