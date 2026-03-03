package com.mogo.project.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.system.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("""
        select r.* from sys_role r left join sys_user_role ur on r.id = ur.role_id where ur.user_id= #{userId}
    """)
    List<SysRole> selectUserRoles(Long userId);
}