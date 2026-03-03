package com.mogo.project.modules.system.controller;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.system.model.entity.SysDictData;
import com.mogo.project.modules.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 字典数据表 前端控制器
 * </p>
 *
 * @author Gemini
 * @since 2025-12-18
 */
@RestController
@RequestMapping("/system/dict/data")
@Anonymous
@RequiredArgsConstructor
public class SysDictDataController {


    private final ISysDictDataService dictDataService;

    /**
     * 根据字典类型查询字典数据信息
     * GET /system/dict/data/type/{dictType}
     */
    @Anonymous
    @GetMapping(value = "/type/{dictType}")
    public ApiResponse<List<SysDictData>> dictType(@PathVariable String dictType) {
        List<SysDictData> data = dictDataService.selectDictDataByType(dictType);
        return ApiResponse.success(data);
    }

}
