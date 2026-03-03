package com.mogo.project.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.common.annotation.DataScope;
import com.mogo.project.common.constant.UserConstants;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.system.model.entity.SysDept;
import com.mogo.project.modules.system.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> {

    private final SysDeptMapper sysDeptMapper;


    @DataScope(deptAlias = "d")
    public List<SysDept> getDeptTree(SysDept dept) {
        List<SysDept> sysDepts = sysDeptMapper.selectDeptList(dept);
        return buildTree(sysDepts);
    }

    public boolean checkDeptNameUnique(SysDept dept) {
        long count = this.count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptName, dept.getDeptName())
                .eq(SysDept::getParentId, dept.getParentId())
                .ne(dept.getId() != null, SysDept::getId, dept.getId()));
        return count > 0;
    }

    public boolean hasChildByDeptId(Long deptId) {
        return this.count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId)
                .eq(SysDept::getDeleteFlag, 0)) > 0;
    }

    public boolean checkDeptExistUser(Long deptId) {
        // 这里需要注入 SysUserMapper 检查该部门下是否有用户
        // 暂时简单返回 false，后续完善
        return false;
    }

    // --- 递归构建树 (逻辑与 Menu 类似) ---
    private List<SysDept> buildTree(List<SysDept> depts) {
        List<SysDept> returnList = new ArrayList<>();
        List<Long> tempList = depts.stream().map(SysDept::getId).toList();
        for (SysDept dept : depts) {
            // 如果是顶级节点 (parentId=0 或 父节点不在当前列表中)
            if (dept.getParentId() == 0 || !tempList.contains(dept.getParentId())) {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty()) return depts;
        return returnList;
    }

    private void recursionFn(List<SysDept> list, SysDept t) {
        List<SysDept> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysDept tChild : childList) {
            if (hasChild(list, tChild)) recursionFn(list, tChild);
        }
    }

    private List<SysDept> getChildList(List<SysDept> list, SysDept t) {
        return list.stream().filter(n -> Objects.equals(n.getParentId(), t.getId())).collect(Collectors.toList());
    }

    private boolean hasChild(List<SysDept> list, SysDept t) {
        return getChildList(list, t).size() > 0;
    }

    public void insertDept(SysDept dept) {
        // 1. 校验父级部门是否允许新增子部门 (例如父级不能是已停用的)
        SysDept info = sysDeptMapper.selectById(dept.getParentId());
        if (info != null && UserConstants.DEPT_DISABLE.equals(info.getStatus())) {
            throw new ServiceException("部门停用，不允许新增");
        }
        // 2. 计算 Ancestors 核心逻辑
        if (info != null) {
            // 如果有父级，祖级列表 = 父级的祖级 + "," + 父级ID
            dept.setAncestors(info.getAncestors() + "," + dept.getParentId());
        } else {
            // 如果是顶级部门 (parentId = 0)，祖级列表就是 "0"
            dept.setAncestors("0");
        }
         sysDeptMapper.insert(dept);
    }

    public void updateDept(SysDept dept) {
        // 1. 获取旧的部门信息
        SysDept oldDept = sysDeptMapper.selectById(dept.getId());
        // 2. 获取新的父级信息
        SysDept newParentDept = sysDeptMapper.selectById(dept.getParentId());

        // 3. 如果父级发生了变化
        if (oldDept != null && newParentDept != null && !oldDept.getParentId().equals(dept.getParentId())) {
            // 计算当前部门新的 ancestors
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getId();
            dept.setAncestors(newAncestors);

            // 【关键】级联更新子孙节点
            updateDeptChildren(dept.getId(), newAncestors, oldDept.getAncestors());
        }

        // 4. 常规更新
         sysDeptMapper.updateById(dept);
    }

    /**
     * 修改子元素关系
     * @param deptId 当前部门ID
     * @param newAncestors 新的祖级列表
     * @param oldAncestors 旧的祖级列表
     */
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        // 1. 查出该部门下的所有子部门 (利用旧的 ancestors 匹配)
        // select * from sys_dept where find_in_set(#{deptId}, ancestors)
        List<SysDept> children = sysDeptMapper.selectChildrenDeptById(deptId);

        for (SysDept child : children) {
            // 2. 替换祖级列表中的旧前缀
            // 例如：旧路径 0,100,101 -> 新路径 0,200,201
            // 子节点路径 0,100,101,105 -> 0,200,201,105
            String verifyAncestors = child.getAncestors().replaceFirst(oldAncestors, newAncestors);
            child.setAncestors(verifyAncestors);

            // 3. 更新子节点
            sysDeptMapper.updateById(child);
        }
    }
}