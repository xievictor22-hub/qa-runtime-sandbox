package com.mogo.project.common.constant;

public interface CacheConstants {
    /**
     * 制品库缓存前缀
     */
    String QUOTE_CACHE = "quote_product_library";

    /**
     * 制品库树形结构 Key
     */
    String PROCESS_TREE_KEY = "'process_tree'"; // 注意：SpEL 表达式需要单引号

    // 用于 Java 代码手动操作的 Key (不带 SpEL 单引号)
    String PROCESS_TREE_KEY_RAW = "process_tree";
}
