package com.mogo.project.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.modules.system.model.dto.LoginInforQueryDto; // 需自行创建DTO
import com.mogo.project.modules.system.model.entity.SysLoginInfor;
import com.mogo.project.modules.system.mapper.SysLoginInforMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SysLoginInforService {

    @Resource
    private SysLoginInforMapper sysLoginInforMapper;

    /**
     * 异步记录登录日志
     */
    @Async("mogoTaskExecutor")
    public void recordLogininfor(String username, String status, String message, String ip) {
        try {
            SysLoginInfor loginInfor = new SysLoginInfor();
            loginInfor.setUsername(username);
            loginInfor.setIpaddr(ip);
            loginInfor.setStatus(status); // 1成功 0失败
            loginInfor.setMsg(message);
            loginInfor.setAccessTime(LocalDateTime.now());

            sysLoginInforMapper.insert(loginInfor);
        } catch (Exception e) {
            log.error("记录登录日志失败:{}", e.getMessage());
        }
    }

    // 分页查询 (DTO 请参考操作日志自行创建，字段：ipaddr, username, status)
    public IPage<SysLoginInfor> selectPage(LoginInforQueryDto dto) {
        Page<SysLoginInfor> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<SysLoginInfor> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getUsername()), SysLoginInfor::getUsername, dto.getUsername())
                .eq(StringUtils.hasText(dto.getStatus()), SysLoginInfor::getStatus, dto.getStatus())
                .orderByDesc(SysLoginInfor::getAccessTime);
        return sysLoginInforMapper.selectPage(page, wrapper);
    }

    // 清空
    public void cleanLoginInfor() {
        sysLoginInforMapper.delete(new LambdaQueryWrapper<>());
    }

    /**
     * 批量删除
     */
    public void deleteLoginInforByIds(List<Long> ids) {
        sysLoginInforMapper.deleteBatchIds(ids);
    }
}