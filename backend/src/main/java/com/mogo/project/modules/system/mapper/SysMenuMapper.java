package com.mogo.project.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.system.model.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询权限列表 (用于Spring Security)
     * 关联查询: sys_user -> sys_user_role -> sys_role_menu -> sys_menu
     */
    @Select("""
        SELECT DISTINCT m.perms 
        FROM sys_menu m
        LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id
        LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id
        LEFT JOIN sys_role r ON r.id = ur.role_id
        WHERE ur.user_id = #{userId} 
          AND m.status = 1 
          AND r.status = 1
          AND m.perms IS NOT NULL
          AND m.perms != ''
    """)
    List<String> selectPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单树 (仅查询目录和菜单，不包含按钮)
     */
    @Select("""
        SELECT DISTINCT m.id, m.parent_id, m.menu_name, m.path, m.component,
                        m.visible, m.status, m.perms, m.menu_type, m.icon, m.sort, m.create_time
        FROM sys_menu m
        LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id
        LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id
        LEFT JOIN sys_role r ON ur.role_id = r.id
        WHERE ur.user_id = #{userId}
          AND m.menu_type IN ('M', 'C')
          AND m.status = 1
          AND r.status = 1
          and m.delete_flag = 0
        ORDER BY m.sort
    """)
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 查询所有菜单 (管理员用)
     */
    @Select("SELECT * FROM sys_menu WHERE menu_type IN ('M', 'C') AND status = 1 and delete_flag = 0 ORDER BY sort")
    List<SysMenu> selectMenuTreeAll();

}