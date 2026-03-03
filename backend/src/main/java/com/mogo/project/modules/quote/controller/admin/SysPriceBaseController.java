package com.mogo.project.modules.quote.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.entity.PageResult;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.quote.domain.basePrice.convert.QuoteImportSourceConvert;
import com.mogo.project.modules.quote.domain.basePrice.convert.QuoteProductLibraryConvert;
import com.mogo.project.modules.quote.domain.basePrice.dto.ProductLibraryProcessTreeQueryDto;
import com.mogo.project.modules.quote.domain.basePrice.dto.ProductLibraryQueryDto;
import com.mogo.project.modules.quote.domain.basePrice.dto.QuoteImportSourceQueryDto;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteProductLibrary;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteVersionLog;
import com.mogo.project.modules.quote.domain.basePrice.vo.ProductLibraryProcessTreeVO;
import com.mogo.project.modules.quote.domain.basePrice.vo.ProductLibraryVo;
import com.mogo.project.modules.quote.domain.basePrice.vo.QuoteImportSourceVo;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteImportSourceService;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteProductLibraryService;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteVersionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "价格底表管理")
@RestController
@RequestMapping("/quote/base")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SysPriceBaseController {


    private final IQuoteProductLibraryService quoteProductLibraryService;
    private final IQuoteImportSourceService quoteImportSourceService;
    private final IQuoteVersionLogService  quoteVersionLogService;
    private final QuoteImportSourceConvert quoteImportSourceConvert;
    private final QuoteProductLibraryConvert quoteProductLibraryConvert;

    /**
     * 查询制品的报价底表信息  暂时使用该方法，之后采用redis
     * @param query
     * @return
     */
    @PreAuthorize("hasAuthority('quote:base:query')")
    @Anonymous
    @GetMapping("/list")
    @Operation(summary = "查询价格导入表存根")
    public ApiResponse<PageResult<QuoteImportSourceVo>> list(
            @Validated QuoteImportSourceQueryDto query) {
        Page<QuoteImportSource> page = query.toMpPage();
        String keyword = query.getKeyword();
        LambdaQueryWrapper<QuoteImportSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getProjectType()!=null, QuoteImportSource::getProjectType, query.getProjectType())
                .eq(StringUtils.hasText(query.getVersion()), QuoteImportSource::getVersion, query.getVersion())
                .like(StringUtils.hasText(query.getProcess1()), QuoteImportSource::getProcess1, query.getProcess1())
                .like(StringUtils.hasText(query.getProcess2()), QuoteImportSource::getProcess2, query.getProcess2())
                .like(StringUtils.hasText(query.getProcess3()), QuoteImportSource::getProcess3, query.getProcess3())
                .like(StringUtils.hasText(query.getProcess4()), QuoteImportSource::getProcess4, query.getProcess4())
                .like(StringUtils.hasText(query.getUnit()), QuoteImportSource::getUnit, query.getUnit())
                .and(StringUtils.hasText(keyword), w -> w
                        .like(QuoteImportSource::getProcess1, keyword)
                        .or()
                        .like(QuoteImportSource::getProcess2, keyword)
                        .or()
                        .like(QuoteImportSource::getProcess3, keyword)
                        .or()
                        .like(QuoteImportSource::getProcess4, keyword)
                        .or()
                        .like(QuoteImportSource::getUnit, keyword)

                )
                .orderByDesc(QuoteImportSource::getId);
        Page<QuoteImportSource> quoteImportSourcePage = quoteImportSourceService.page(page, wrapper);
        return ApiResponse.success(PageResult.buildList(quoteImportSourcePage,quoteImportSourceConvert::toVoList));
    }


    /**
     * 查询制品的报价底表信息  暂时使用该方法，之后采用redis
     *
     * @param query
     * @return
     */
    @GetMapping("/listProductLibrary")
    @Operation(summary = "查询制品价格底表")
    public ApiResponse<PageResult<ProductLibraryVo>> listProductLibrary(
            @Validated ProductLibraryQueryDto query) {
        Page<QuoteProductLibrary> page = query.toMpPage();
        String keyword = query.getKeyword();//模糊搜索
        LambdaQueryWrapper<QuoteProductLibrary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq( QuoteProductLibrary::getProjectType, query.getProjectType())
                .eq(QuoteProductLibrary::getVersion, query.getVersion())
                .like(StringUtils.hasText(query.getProcess1()), QuoteProductLibrary::getProcess1, query.getProcess1())
                .like(StringUtils.hasText(query.getProcess2()), QuoteProductLibrary::getProcess2, query.getProcess2())
                .like(StringUtils.hasText(query.getProcess3()), QuoteProductLibrary::getProcess3, query.getProcess3())
                .like(StringUtils.hasText(query.getProcess4()), QuoteProductLibrary::getProcess4, query.getProcess4())
                // 2. 【核心】处理 keyword 全局模糊搜索
                .and(StringUtils.hasText(keyword), w -> w
                        .like(QuoteProductLibrary::getProcess1, keyword)
                        .or()
                        .like(QuoteProductLibrary::getProcess2, keyword)
                        .or()
                        .like(QuoteProductLibrary::getProcess3, keyword)
                        .or()
                        .like(QuoteProductLibrary::getProcess4, keyword)
                )
                .orderByDesc(QuoteProductLibrary::getId);
        IPage<QuoteProductLibrary> pageResult = quoteProductLibraryService.page(page, wrapper);
        PageResult<ProductLibraryVo> productLibraryVoPageResult = PageResult.buildList(pageResult, quoteProductLibraryConvert::toVoList);
        return ApiResponse.success(productLibraryVoPageResult);
    }


    @PreAuthorize("hasAuthority('quote:base:query')")
    @GetMapping("/getBasePriceVersions")
    @Operation(summary = "查询价格底表版本数据")
    public ApiResponse<Set<String>> getBasePriceVersions(){
        LambdaQueryWrapper<QuoteVersionLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(QuoteVersionLog::getVersion).orderByDesc(QuoteVersionLog::getCreateTime);
        List<QuoteVersionLog> list = quoteVersionLogService.list(queryWrapper);
        Set<String> collect = list.stream().map(QuoteVersionLog::getVersion).collect(Collectors.toSet());
        return ApiResponse.success(collect);
    }

    @PreAuthorize("hasAuthority('quote:detail:list')")
    @GetMapping("/getProcessTree")
    @Operation(summary = "查询价格底表选项数据")
    public ApiResponse<List<ProductLibraryProcessTreeVO>> getProcessTree(@Validated ProductLibraryProcessTreeQueryDto query){
        return ApiResponse.success(quoteProductLibraryService.getTree(query));
    }
}