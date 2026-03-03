package com.mogo.project.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 根据角色ID查询部门树信息 (用于角色分配数据权限)
     */
    @Select("select d.id from sys_dept d left join sys_role_dept rd on d.id = rd.dept_id where rd.role_id = #{roleId}")
    List<Long> selectDeptListByRoleId(Long roleId);

    List<SysDept> selectDeptList(SysDept sysDept);


    List<SysDept> selectChildrenDeptById(Long deptId);
}