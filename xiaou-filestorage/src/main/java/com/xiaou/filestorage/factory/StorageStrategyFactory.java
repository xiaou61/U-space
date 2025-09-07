package com.xiaou.filestorage.factory;

import com.xiaou.filestorage.strategy.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 存储策略工厂
 *
 * @author xiaou
 */
@Slf4j
@Component
public class StorageStrategyFactory {

    /**
     * 存储策略映射
     */
    private final Map<String, FileStorageStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 已初始化的策略实例
     */
    private final Map<Long, FileStorageStrategy> initializedStrategies = new ConcurrentHashMap<>();

    /**
     * 构造函数，自动注册所有存储策略
     *
     * @param strategies 所有存储策略实现
     */
    @Autowired
    public StorageStrategyFactory(List<FileStorageStrategy> strategies) {
        for (FileStorageStrategy strategy : strategies) {
            strategyMap.put(strategy.getStorageType(), strategy);
            log.info("注册存储策略: {}", strategy.getStorageType());
        }
    }

    /**
     * 获取存储策略
     *
     * @param storageType 存储类型
     * @return 存储策略
     */
    public FileStorageStrategy getStrategy(String storageType) {
        return strategyMap.get(storageType);
    }

    /**
     * 创建并初始化存储策略实例
     *
     * @param configId     配置ID
     * @param storageType  存储类型
     * @param configParams 配置参数
     * @return 初始化后的存储策略
     */
    public FileStorageStrategy createAndInitialize(Long configId, String storageType, Map<String, Object> configParams) {
        // 检查是否已有初始化的实例
        if (initializedStrategies.containsKey(configId)) {
            return initializedStrategies.get(configId);
        }

        // 获取策略模板
        FileStorageStrategy template = strategyMap.get(storageType);
        if (template == null) {
            log.error("不支持的存储类型: {}", storageType);
            return null;
        }

        try {
            // 创建新实例
            FileStorageStrategy instance = template.getClass().getDeclaredConstructor().newInstance();
            
            // 初始化配置
            boolean initialized = instance.initialize(configParams);
            if (!initialized) {
                log.error("初始化存储策略失败: configId={}, storageType={}", configId, storageType);
                return null;
            }

            // 缓存初始化后的实例
            initializedStrategies.put(configId, instance);
            log.info("成功创建并初始化存储策略: configId={}, storageType={}", configId, storageType);
            
            return instance;
            
        } catch (Exception e) {
            log.error("创建存储策略实例失败: configId={}, storageType={}, error={}", configId, storageType, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取已初始化的存储策略
     *
     * @param configId 配置ID
     * @return 存储策略
     */
    public FileStorageStrategy getInitializedStrategy(Long configId) {
        return initializedStrategies.get(configId);
    }

    /**
     * 移除已初始化的存储策略
     *
     * @param configId 配置ID
     */
    public void removeInitializedStrategy(Long configId) {
        FileStorageStrategy removed = initializedStrategies.remove(configId);
        if (removed != null) {
            log.info("移除存储策略实例: configId={}", configId);
        }
    }

    /**
     * 获取所有支持的存储类型
     *
     * @return 存储类型列表
     */
    public List<String> getSupportedStorageTypes() {
        return strategyMap.keySet().stream().collect(Collectors.toList());
    }

    /**
     * 检查是否支持指定的存储类型
     *
     * @param storageType 存储类型
     * @return 是否支持
     */
    public boolean isSupported(String storageType) {
        return strategyMap.containsKey(storageType);
    }

    /**
     * 清除所有已初始化的策略实例
     */
    public void clearAllInitializedStrategies() {
        initializedStrategies.clear();
        log.info("清除所有已初始化的存储策略实例");
    }
} 