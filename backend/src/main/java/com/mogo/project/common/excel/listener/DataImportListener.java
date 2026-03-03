package com.mogo.project.common.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

/**
 * 通用 Excel 读取监听器
 * 核心思想：每读取到 N 条数据，就调用一次 Service 的 saveBatch 方法，防止内存溢出
 * @param <T> Excel对应的VO类型
 */
@Slf4j
public class DataImportListener<T> implements ReadListener<T> {

    /**
     * 每隔 100 条存储数据库，实际开发中可以调整为 1000-3000
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 临时存储数据的列表
     */
    private List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * 这是一个函数式接口，用于回调 Service 的保存方法
     * 既然是通用监听器，就不能把 UserService 写死在这里
     */
    private final Consumer<List<T>> consumer;

    /**
     * 构造函数：必须传入数据处理逻辑（通常是 service::saveBatch）
     */
    public DataImportListener(Consumer<List<T>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        cachedDataList.add(data);
        // 达到阈值，执行持久化
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // Excel解析完毕，处理剩余的数据
        if (!cachedDataList.isEmpty()) {
            saveData();
        }
        log.info("所有数据解析完成！");
    }

    private void saveData() {
        log.info("开始保存 {} 条数据到数据库", cachedDataList.size());
        // 回调外部传入的保存逻辑
        consumer.accept(cachedDataList);
        // 保存完清理列表，释放内存
        cachedDataList.clear();
    }
}