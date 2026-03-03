package com.mogo.project.modules.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.modules.system.model.entity.SysMenu;
import com.mogo.project.modules.system.mapper.SysMenuMapper;
import com.mogo.project.modules.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {


    @Override
    public List<SysMenu> getMenuTree(String menuName, Integer status) {
        // 1. 查询所有符合条件的菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(menuName), SysMenu::getMenuName, menuName)
                .eq(status != null, SysMenu::getStatus, status)
                .eq(SysMenu::getDeleteFlag,0)
                .orderByAsc(SysMenu::getSort);
        List<SysMenu> menus = this.list(wrapper);

        // 2. 构建树形结构
        return buildTree(menus);
    }

    @Override
    public boolean checkMenuNameUnique(SysMenu menu) {
        long count = this.count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuName, menu.getMenuName())
                .eq(SysMenu::getParentId, menu.getParentId()) // 同一级目录下不重名
                .ne(menu.getId() != null, SysMenu::getId, menu.getId()));
        return count > 0;
    }

    @Override
    public boolean hasChildByMenuId(Long menuId) {
        return this.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId)) > 0;
    }

    /**
     * 递归构建树的方法
     */
    private List<SysMenu> buildTree(List<SysMenu> menus) {
        List<SysMenu> returnList = new ArrayList<>();
        List<Long> tempList = menus.stream().map(SysMenu::getId).toList();

        for (SysMenu menu : menus) {
            // 如果是顶级节点 (parentId 为 0，或者 parentId 不在当前列表中)
            if (menu.getParentId() == 0 || !tempList.contains(menu.getParentId())) {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) {
            return menus; // 如果没找到根节点，直接返回列表
        }
        return returnList;

        // 备选方案 需要引入 hutool-all
//        List<Tree<Long>> treeNodes = TreeUtil.build(menus, 0L, (treeNode, tree) -> {
//            tree.setId(treeNode.getId());
//            tree.setParentId(treeNode.getParentId());
//            tree.setName(treeNode.getMenuName());
//            tree.putExtra("path", treeNode.getPath());
//            tree.putExtra("component", treeNode.getComponent());
//            tree.putExtra("meta", Map.of("title", treeNode.getMenuName (), "icon", treeNode.getIcon()));
//        });
    }

    /**
     * 递归查找子节点
     */
    private void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        return list.stream()
                .filter(n -> Objects.equals(n.getParentId(), t.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return !getChildList(list, t).isEmpty();
    }


    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus;
        // 如果是超级管理员 (ID=1)，查询所有
        if (userId == 1L) {
            menus = baseMapper.selectMenuTreeAll();
        } else {
            menus = baseMapper.selectMenuTreeByUserId(userId);
        }
        return buildTree(menus);
    }
}