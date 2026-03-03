package com.mogo.project.modules.system.service;

import com.mogo.project.modules.system.model.entity.SysDictData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 字典数据表 服务类
 * </p>
 *
 * @author Gemini
 * @since 2025-12-18
 */
public interface ISysDictDataService extends IService<SysDictData> {

    List<SysDictData> selectDictDataByType(String dictType);

}
