package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role_dept")
public class SysRoleDept implements Serializable {
    private Long roleId;
    private Long deptId;
}