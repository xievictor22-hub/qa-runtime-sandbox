package com.mogo.project.modules.quote.domain.basePrice.service;



import com.mogo.project.modules.quote.domain.basePrice.dto.QuoteBasePriceImportExcelDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IQuoteImportService {
    /**
     * 导入报价 Excel
     * @param file Excel文件
     * @param version 版本号
     * @param description 描述
     */
    void importQuoteExcel(MultipartFile file, String version, String description);

    void downLoadQuoteExcel(String version, HttpServletResponse response);

    void add(QuoteBasePriceImportExcelDto quoteBasePriceImportExcelDto,String version);
}