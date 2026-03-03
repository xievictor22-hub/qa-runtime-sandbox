package com.mogo.project.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.modules.system.model.dto.UserQueryDto;
import com.mogo.project.modules.system.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    IPage<SysUser> getUserPage(Page<SysUser> page, @Param("queryDto") UserQueryDto queryDto);

    List<SysUser> selectUserListByRoleKey(String roleKey);
}