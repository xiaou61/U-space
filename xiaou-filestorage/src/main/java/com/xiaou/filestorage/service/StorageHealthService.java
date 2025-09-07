package com.xiaou.filestorage.service;

import com.xiaou.filestorage.domain.StorageConfig;

/**
 * 存储健康检查服务接口
 *
 * @author xiaou
 */
public interface StorageHealthService {

    /**
     * 检查存储配置是否健康
     *
     * @param storageConfig 存储配置
     * @return 是否健康
     */
    boolean isStorageHealthy(StorageConfig storageConfig);

    /**
     * 获取下一个可用的存储配置
     *
     * @param excludeConfigId 排除的配置ID
     * @return 可用的存储配置，没有则返回null
     */
    StorageConfig getNextAvailableStorage(Long excludeConfigId);

    /**
     * 标记存储配置为不健康
     *
     * @param configId 配置ID
     */
    void markStorageUnhealthy(Long configId);

    /**
     * 标记存储配置为健康
     *
     * @param configId 配置ID
     */
    void markStorageHealthy(Long configId);

    /**
     * 检查并更新所有存储的健康状态
     */
    void checkAllStorageHealth();
} 