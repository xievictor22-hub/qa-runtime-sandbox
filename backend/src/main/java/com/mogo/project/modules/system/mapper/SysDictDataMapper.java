package com.mogo.project.modules.system.mapper;

import com.mogo.project.modules.system.model.entity.SysDictData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 字典数据表 Mapper 接口
 * </p>
 *
 * @author Gemini
 * @since 2025-12-18
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    List<SysDictData> selectDictDataByType(String dictType);
}
