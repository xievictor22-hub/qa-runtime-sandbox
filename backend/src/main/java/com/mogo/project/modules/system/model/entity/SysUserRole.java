package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    private Long userId;
    private Long roleId;
}