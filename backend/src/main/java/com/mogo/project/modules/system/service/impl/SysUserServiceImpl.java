package com.mogo.project.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.common.annotation.DataScope;
import com.mogo.project.modules.auth.service.TokenService;
import com.mogo.project.modules.system.model.entity.SysDept;
import com.mogo.project.modules.system.model.entity.SysUser;
import com.mogo.project.modules.system.model.entity.SysUserRole;
import com.mogo.project.modules.system.mapper.SysDeptMapper;
import com.mogo.project.modules.system.mapper.SysUserMapper;
import com.mogo.project.modules.system.mapper.SysUserRoleMapper;
import com.mogo.project.modules.system.service.SysUserService;
import com.mogo.project.modules.system.model.dto.UserQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final TokenService tokenService;
    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;


    //已配置为默认，其实可以不填
    @DataScope(deptAlias = "d", userAlias = "u")// d 和 u 要对应 SQL 里的别名
    @Override
    public IPage<SysUser> getUserPage(UserQueryDto queryDto) {
        // 1. 构建分页对象
        Page<SysUser> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        // 2. 构建查询条件

        // 3. 执行查询
        IPage<SysUser> userPage = sysUserMapper.getUserPage(page, queryDto);
        List<SysUser> userList = userPage.getRecords();
        if (userList.isEmpty()) {
            return userPage;
        }
        List<SysDept> allDepts = sysDeptMapper.selectList(null);
        Map<Long, SysDept> deptMap = allDepts.stream()
                .collect(Collectors.toMap(SysDept::getId, Function.identity()));

        // 3. 内存组装路径
        for (SysUser u : userList) {
            //删除密码
            u.setPassword(null);
            if (u.getDeptId() == null || !deptMap.containsKey(u.getDeptId())) {
                u.setDeptNamePath("未分配部门");
                continue;
            }
            SysDept currentDept = deptMap.get(u.getDeptId());
            u.setDeptNamePath(buildDeptPath(currentDept, deptMap));
        }

        return userPage;
    }

    @Override
    public boolean saveUser(SysUser sysUser) {
        // 1. 校验用户名是否重复
        if (checkPhoneUnique(sysUser)) {
            throw new RuntimeException("该手机号已存在");
        }
        // 2. 密码加密 (如果是新增，设置默认密码 123456)
        if (!StringUtils.hasText(sysUser.getPassword())) {
            sysUser.setPassword("123456");
        }
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));

        // 3. 设置默认状态
        if (sysUser.getStatus() == null) sysUser.setStatus(1); // 1: 正常

        return this.save(sysUser);
    }

    @Override
    public boolean updateUser(SysUser sysUser) {
        // 修改时一般不修改密码，密码修改走单独接口
        sysUser.setPassword(null);
        //验证手机号唯一
        String phone = sysUser.getPhone();
        if(phone != null && !phone.isEmpty()){
            if (checkPhoneUnique(sysUser)) {
                throw new RuntimeException("该手机号已存在");
            }
        }
        boolean result = this.updateById(sysUser);
        if (result) {
            // ★★★ 核心：更新完成后，刷新该用户的 Redis 缓存 ★★★
            // 注意：这里需要传入 username，如果 user 对象里只有 id，需要先查出来
            // 如果是管理端修改，user 对象里通常有 username
            tokenService.refreshUserCache(sysUser.getUsername());
        }
        return result;
    }

    private boolean checkPhoneUnique(SysUser sysUser) {
        return this.count(new LambdaQueryWrapper<SysUser>().ne(sysUser.getId()!=null, SysUser::getId,sysUser.getId()).eq(SysUser::getPhone, sysUser.getPhone()))>0;
    }
    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        // 查询 sys_user_role 表
        return sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 1. 先删除该用户原有的所有角色关联
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 2. 批量插入新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            roleIds.forEach(roleId -> {
                sysUserRoleMapper.insert(new SysUserRole(userId, roleId));
            });
        }
    }

    @Override
    public List<SysUser> getAllUserList() {
        return  sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1).select(SysUser::getId, SysUser::getUsername)
        );
    }

    @Override
    public List<SysUser> selectUserListByRoleKey(String roleKey) {
        return sysUserMapper.selectUserListByRoleKey(roleKey);
    }

    /**
     * 辅助方法：构建路径字符串
     * @param currentDept 当前部门
     * @param deptMap 所有部门缓存
     * @return "MOGO总公司 / 生产中心 / 屏风一部"
     */
    private String buildDeptPath(SysDept currentDept, Map<Long, SysDept> deptMap) {
        if (currentDept == null) return "";

        // 1. 解析 ancestors (例如: "0,100,101")
        String ancestors = currentDept.getAncestors();
        List<String> pathIds = new ArrayList<>(Arrays.asList(ancestors.split(",")));

        // 2. 移除 "0" (顶级占位符)
        pathIds.remove("0");

        // 3. 加入当前部门ID
        pathIds.add(currentDept.getId().toString());

        // 4. ID 转 名称
        StringBuilder pathBuilder = new StringBuilder();
        for (String idStr : pathIds) {
            if (idStr.isEmpty()) continue;
            long id = Long.parseLong(idStr);
            SysDept node = deptMap.get(id);
            if (node != null) {
                if (pathBuilder.length() > 0) {
                    pathBuilder.append(" / "); // 分隔符，前端也可以根据需要改 "-"
                }
                pathBuilder.append(node.getDeptName());
            }
        }
        return pathBuilder.toString();
    }
}
