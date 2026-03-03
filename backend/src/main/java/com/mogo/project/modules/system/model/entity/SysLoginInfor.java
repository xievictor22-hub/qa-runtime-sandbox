package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统访问记录
 */
@Data
@TableName("sys_logininfor")
public class SysLoginInfor {

    private Long id;

    /**
    * 用户账号
    */
    private String username;

    /**
    * 登录IP地址
    */
    private String ipaddr;

    /**
    * 登录状态（1成功 0失败）
    */
    private String status;

    /**
    * 提示信息
    */
    private String msg;

    /**
    * 访问时间
    */
    private LocalDateTime accessTime;


}