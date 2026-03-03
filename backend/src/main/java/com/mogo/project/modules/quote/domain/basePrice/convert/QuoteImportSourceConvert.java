package com.mogo.project.modules.quote.domain.basePrice.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.modules.quote.core.convert.IBaseMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import com.mogo.project.modules.quote.domain.basePrice.vo.QuoteImportSourceVo;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
//        ,unmappedTargetPolicy = ReportingPolicy.IGNORE // 忽略未映射的字段
)
public interface QuoteImportSourceConvert extends IBaseMapper<QuoteImportSourceVo, QuoteImportSource> {

    @Override
    @Mapping(target = "isFolding", source = ".", qualifiedByName = "mapIsFoldingToString")
    QuoteImportSourceVo toVo(QuoteImportSource entity);

    @Override
    @Mapping(target = "isFolding", source = ".", qualifiedByName = "mapIsFoldingToString")
     List<QuoteImportSourceVo> toVoList(List<QuoteImportSource> entityList);

    @Override
    @InheritInverseConfiguration(name = "toVo")
    @Mapping(target = "isFolding", source = "isFolding", qualifiedByName = "mapStringToIsFolding")
    QuoteImportSource toEntity(QuoteImportSourceVo vo);

    default Page<QuoteImportSourceVo> toVoPage(Page<QuoteImportSource> page) {
        if (page == null) {
            return null;
        }

        // 创建新的 Page 对象，使用原分页信息
        Page<QuoteImportSourceVo> voPage = new Page<>();
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());
        voPage.setPages(page.getPages());

        // 转换 records 列表
        if (page.getRecords() != null) {
            List<QuoteImportSourceVo> voList = toVoList(page.getRecords());
            voPage.setRecords(voList);
        }
        return voPage;
    }

    /**
     * 将数字类型的 isFolding 转换为前端显示的字符串
     */
    @Named("mapIsFoldingToString")
    default String mapIsFoldingToString(QuoteImportSource entity) {
        if (entity == null) {
            return "无";
        }

        Integer isFolding = entity.getIsFolding();
        if (isFolding == null) {
            return "无";
        }

        switch (isFolding) {
            case 0:
                return "无";
            case 1:
                return entity.getProcess1() != null ? entity.getProcess1() : "无";
            case 2:
                return entity.getProcess2() != null ? entity.getProcess2() : "无";
            case 3:
                return entity.getProcess3() != null ? entity.getProcess3() : "无";
            case 4:
                return entity.getProcess4() != null ? entity.getProcess4() : "无";
            default:
                return "无";
        }
    }

    /**
     * 将前端字符串转换为数字类型的 isFolding
     * 注意：这个方法可能会丢失信息，因为字符串无法确定原始的数字值
     * 可以根据实际业务需求调整
     */
    @Named("mapStringToIsFolding")
    default Integer mapStringToIsFolding(String isFoldingStr) {
        if (isFoldingStr == null || "无".equals(isFoldingStr)) {
            return 0;
        }
        // 这里需要根据实际业务逻辑来处理反向转换
        // 由于前端显示的可能是 process1-4 的值，反向转换会有信息丢失
        // 如果业务需要保存原始值，可能需要额外的处理逻辑
        return 0; // 默认返回 0，根据实际情况调整
    }
}