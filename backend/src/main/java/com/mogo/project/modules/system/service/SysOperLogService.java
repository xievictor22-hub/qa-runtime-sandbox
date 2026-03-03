package com.mogo.project.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.modules.system.model.dto.OperLogQueryDto;
import com.mogo.project.modules.system.model.entity.SysOperLog;
import com.mogo.project.modules.system.mapper.SysOperLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class SysOperLogService {

    @Resource
    private SysOperLogMapper sysOperLogMapper;

    /**
     * 异步保存操作日志
     * 这里的 "taskExecutor" 对应 AsyncConfig 里配置的线程池
     */
    @Async("mogoTaskExecutor")
    public void recordOper(SysOperLog operLog) {
        try {
            // 模拟耗时操作，验证是否阻塞主线程
            // Thread.sleep(1000);
            sysOperLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("操作日志插入失败:{}", e.getMessage());
        }
    }

    /**
     * 分页查询操作日志
     */
    public IPage<SysOperLog> selectOperLogPage(OperLogQueryDto dto) {
        Page<SysOperLog> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getTitle()), SysOperLog::getTitle, dto.getTitle())
                .like(StringUtils.hasText(dto.getOperName()), SysOperLog::getOperName, dto.getOperName())
                .eq(dto.getBusinessType() != null, SysOperLog::getBusinessType, dto.getBusinessType())
                .eq(dto.getStatus() != null, SysOperLog::getStatus, dto.getStatus())
                .orderByDesc(SysOperLog::getOperTime); // 按时间倒序

        return sysOperLogMapper.selectPage(page, wrapper);
    }

    /**
     * 清空日志
     */
    public void cleanOperLog() {
        sysOperLogMapper.delete(new LambdaQueryWrapper<>());
    }

    /**
     * 批量删除
     */
    public void deleteOperLogByIds(List<Long> ids) {
        sysOperLogMapper.deleteBatchIds(ids);
    }
}