package com.mogo.project.modules.auth.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.system.model.entity.SysRole;
import com.mogo.project.modules.system.model.entity.SysUser;
import com.mogo.project.modules.system.mapper.SysMenuMapper;
import com.mogo.project.modules.system.mapper.SysRoleMapper;
import com.mogo.project.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户验证处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (sysUser == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }

        // 2. 校验用户状态 (被禁用或被删除)
        // 这些逻辑其实 LoginUser 内部也有判断，但在这里拦截可以给出更明确的报错信息
        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("对不起，您的账号：" + username + " 已被停用");
        }

        // 3. 查询用户权限 (角色+菜单)
        // 目前暂时给空列表，后续完善 RBAC 时补充
         List<String> permissions = sysMenuMapper.selectPermsByUserId (sysUser.getId());

        //添加用户角色列表
        List<SysRole> sysRoles = sysRoleMapper.selectUserRoles(sysUser.getId());
        sysUser.setRoles(sysRoles);

        return new LoginUser(sysUser,permissions );
    }
}