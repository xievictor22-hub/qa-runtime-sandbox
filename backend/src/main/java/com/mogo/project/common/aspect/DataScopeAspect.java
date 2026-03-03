package com.mogo.project.common.aspect;

import com.mogo.project.common.annotation.DataScope;
import com.mogo.project.common.entity.BaseEntity;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.system.model.entity.SysRole;
import com.mogo.project.modules.system.model.entity.SysUser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Aspect
@Component
/**
 * 数据权限隔离，根据用户的DATA_SCOPE_对搜索进行筛选
 */
public class DataScopeAspect {

    public static final String DATA_SCOPE_ALL = "1";
    public static final String DATA_SCOPE_CUSTOM = "2";
    public static final String DATA_SCOPE_DEPT = "3";
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";
    public static final String DATA_SCOPE_SELF = "5";

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope) {
        clearDataScope(point);
        handleDataScope(point, controllerDataScope);
    }

    protected void handleDataScope(final JoinPoint joinPoint, DataScope controllerDataScope) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth.getPrincipal()==null ||"anonymousUser".equals(auth.getPrincipal()) ) return;// 不注入 dataScope

        // 1. 获取当前用户
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(auth.getPrincipal() instanceof LoginUser )) {
            return;
        }
        SysUser currentUser = loginUser.getSysUser();

        // 超级管理员(ID=1)不做限制
        if (currentUser.getId() == 1L) return;

        StringBuilder sqlString = new StringBuilder();

        // 2. 遍历用户的所有角色，拼接 SQL
        // 逻辑：只要有一个角色拥有大权限，就取最大的并集
        for (SysRole role : currentUser.getRoles()) { // 注意：你需要确保LoginUser里注入了Roles
            String dataScope = role.getDataScope();

            // 全部数据权限
            if (DATA_SCOPE_ALL.equals(dataScope)) {
                return; // 不拼任何 SQL，即查全部
            }
            // 本部门数据权限
            else if (DATA_SCOPE_DEPT.equals(dataScope)) {
                sqlString.append(String.format(
                        " OR %s.dept_id = %d ",
                        controllerDataScope.deptAlias(), currentUser.getDeptId()));
            }
            else if (DATA_SCOPE_CUSTOM.equals(dataScope)) {
                // 技巧：利用 role_id 查中间表
                // 这里的 SQL 逻辑是： OR d.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = ? )
                sqlString.append(String.format(
                        " OR %s.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = %d ) ",
                        controllerDataScope.deptAlias(),
                        role.getId()
                ));
            }
            // 本部门及以下
            else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope)) {
                sqlString.append(String.format(
                        " OR %s.dept_id IN ( SELECT id FROM sys_dept WHERE id = %d OR find_in_set( %d , ancestors ) )",
                        controllerDataScope.deptAlias(), currentUser.getDeptId(), currentUser.getDeptId()));
            }
            // 仅本人数据
            else if (DATA_SCOPE_SELF.equals(dataScope)) {
                if (StringUtils.hasText(controllerDataScope.userAlias())) {
                    sqlString.append(String.format(
                            " OR %s.user_id = %d ",
                            controllerDataScope.userAlias(), currentUser.getId()));
                } else {
                    // 如果没指定 userAlias，默认按 deptAlias = 0 (查不到数据) 容错
                    sqlString.append(String.format(
                            " OR %s.dept_id = 0 ", controllerDataScope.deptAlias()));
                }
            }
        }

        // 3. 将 SQL 注入到参数中
        if (StringUtils.hasText(sqlString.toString())) {
            Object params = joinPoint.getArgs()[0]; // 假设第一个参数是 DTO 或 Entity
            if (params instanceof BaseEntity baseEntity) {
                baseEntity.getParams().put("dataScope", " AND (" + sqlString.substring(4) + ")");
            }
        }
    }

    private void clearDataScope(final JoinPoint joinPoint) {
        // 清理旧参数，防止污染
        Object params = joinPoint.getArgs()[0];
        if (params instanceof BaseEntity baseEntity) {
            baseEntity.getParams().put("dataScope", "");
        }
    }
}