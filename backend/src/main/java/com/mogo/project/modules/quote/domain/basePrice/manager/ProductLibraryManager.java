package com.mogo.project.modules.quote.domain.basePrice.manager;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.mogo.project.common.constant.CacheConstants;
import com.mogo.project.modules.quote.domain.basePrice.dto.ProductLibraryProcessTreeQueryDto;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteProductLibrary;
import com.mogo.project.modules.quote.domain.basePrice.mapper.QuoteProductLibraryMapper;
import com.mogo.project.modules.quote.domain.basePrice.vo.ProductLibraryProcessTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manager 层：负责通用业务处理、缓存处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductLibraryManager {

    private final QuoteProductLibraryMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取全量树（带缓存）
     * 这里的逻辑从 Service 移动到了 Manager
     * 区分了版本与项目类型
     */
    @Cacheable(value = CacheConstants.QUOTE_CACHE,
            key = "'process_tree:'+#queryDto.version+':'+#queryDto.projectType",
            unless = "#result == null || #result.isEmpty()")// 如果结果为空就不缓存，防止缓存空树
    public List<ProductLibraryProcessTreeVO> getProcessTreeCache(ProductLibraryProcessTreeQueryDto queryDto) {
        log.info(">>> 缓存未命中或已过期，开始从数据库重新构建制品树...");
        // 1. 查询去重数据
        QueryWrapper<QuoteProductLibrary> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT process1, process2, process3, process4")
                .lambda()
                .eq(QuoteProductLibrary::getDeleteFlag, 0)
                .eq(QuoteProductLibrary::getVersion, queryDto.getVersion())
                .eq(QuoteProductLibrary::getProjectType, queryDto.getProjectType())
                .orderByAsc(QuoteProductLibrary::getProcess1);
        List<QuoteProductLibrary> list = mapper.selectList(wrapper);

        // 2. 内存构建树逻辑
        return buildTreeInMemory(list);
    }

    /**
     * 通过注解清除缓存
     */
    @CacheEvict(value = CacheConstants.QUOTE_CACHE, key = "'process_tree:'+#version+':'+#projectType")
    public void clearProcessTreeCache(String version,Integer projectType) {
        log.info(">>> 主动清除制品树缓存:version={},projectType={}", version, projectType);
    }

    /**
     * 只删除特定版本
     * @param version
     */
    public void clearProcessTreeCache(String version) {
        if (!StringUtils.hasText(version)) return;
        //通配符匹配redis中的该版本所有选项树的缓存
        String pattern = CacheConstants.QUOTE_CACHE+"::process_tree:"+version+":*";
        Set<String> keys = redisTemplate.keys(pattern);
        // 3. 执行删除
        if ( !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info(">>> 模糊清除版本 [{}] 下的所有缓存，共删除 {} 个 Key", version, keys.size());
        } else {
            log.info(">>> 版本 [{}] 下无缓存可删", version);
        }
    }

    /**
     * 【重载 3】全量清除：清空整个制品库缓存 (可选)
     * 场景：数据迁移、系统重置、后台手动刷新
     */
    @CacheEvict(value = CacheConstants.QUOTE_CACHE, allEntries = true)
    public void clearProcessTreeCache() {
        log.info(">>> [缓存清除] 全量模式: 清空所有制品树缓存");
    }



    // 私有方法：构建树逻辑封装
    private List<ProductLibraryProcessTreeVO> buildTreeInMemory(List<QuoteProductLibrary> list) {
        List<ProductLibraryProcessTreeVO> tree = new ArrayList<>();

        // Group by Process1
        Map<String, List<QuoteProductLibrary>> p1Map = list.stream()
                .filter(i -> StringUtils.hasText(i.getProcess1()))
                .collect(Collectors.groupingBy(QuoteProductLibrary::getProcess1));

        p1Map.forEach((p1, list1) -> {
            ProductLibraryProcessTreeVO node1 = new ProductLibraryProcessTreeVO(p1);

            // Group by Process2
            Map<String, List<QuoteProductLibrary>> p2Map = list1.stream()
                    .filter(i -> StringUtils.hasText(i.getProcess2()))
                    .collect(Collectors.groupingBy(QuoteProductLibrary::getProcess2));

            if (!p2Map.isEmpty()) {
                p2Map.forEach((p2, list2) -> {
                    ProductLibraryProcessTreeVO node2 = new ProductLibraryProcessTreeVO(p2);

                    // Group by Process3
                    Map<String, List<QuoteProductLibrary>> p3Map = list2.stream()
                            .filter(i -> StringUtils.hasText(i.getProcess3()))
                            .collect(Collectors.groupingBy(QuoteProductLibrary::getProcess3));

                    if (!p3Map.isEmpty()) {
                        p3Map.forEach((p3, list3) -> {
                            ProductLibraryProcessTreeVO node3 = new ProductLibraryProcessTreeVO(p3);
                            // Process4 (Leaf)
                            list3.stream()
                                    .map(QuoteProductLibrary::getProcess4)
                                    .filter(StringUtils::hasText)
                                    .distinct()
                                    .forEach(p4 -> node3.addChild(new ProductLibraryProcessTreeVO(p4)));
                            node2.addChild(node3);
                        });
                    }
                    node1.addChild(node2);
                });
            }
            tree.add(node1);
        });
        return tree;
    }
}
