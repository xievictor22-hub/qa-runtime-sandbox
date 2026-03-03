package com.mogo.project.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mogo.project.modules.system.mapper.SysDictDataMapper;
import com.mogo.project.modules.system.model.entity.SysDictData;
import com.mogo.project.modules.system.service.ISysDictDataService;
import com.mogo.project.common.util.SecurityUtils; // 假设你封装了权限检查工具
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据字典类型获取字典数据
     * 1. 优先查缓存
     * 2. 缓存没有查数据库并写入缓存
     * 3. 拿到数据后，根据当前用户权限进行过滤
     */
    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        String cacheKey = "sys_dict:" + dictType;

        // 1. 查询缓存
        List<SysDictData> dictList = (List<SysDictData>) redisTemplate.opsForValue().get(cacheKey);

        // 2. 缓存未命中，查询数据库
        if (dictList == null) {
            dictList = baseMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getStatus, "0") // 只查正常状态
                    .eq(SysDictData::getDictType, dictType)
                    .orderByAsc(SysDictData::getDictSort)); // 排序

            // 写入缓存 (这里演示未设置过期时间，修改字典时需主动删除key)
            if (dictList != null) {
                redisTemplate.opsForValue().set(cacheKey, dictList);
            }
        }

        if (dictList == null) return List.of();

        // 3. 【核心逻辑】权限过滤
        // 如果 dictList 中的某项配置了 authCode，则需检查当前用户是否有该权限
        return dictList.stream().filter(item -> {
            // 如果 authCode 为空，说明是公开选项，保留
            if (StrUtil.isBlank(item.getAuthCode())) {
                return true;
            }
            // 如果有配置权限标识，调用工具类检查权限

            return SecurityUtils.hasPermission(item.getAuthCode());
        }).collect(Collectors.toList());
    }
}