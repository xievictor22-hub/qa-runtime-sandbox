package com.mogo.project.modules.quote.domain.basePrice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 * 关联选项对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductLibraryProcessTreeVO {
    private String value; // 节点值 (如 "开料")
    private String label; // 显示名称
    private List<ProductLibraryProcessTreeVO> children; // 子节点

    public ProductLibraryProcessTreeVO(String name) {
        this.value = name;
        this.label = name;
    }
    // 辅助方法：添加子节点
    public void addChild(ProductLibraryProcessTreeVO child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
}
