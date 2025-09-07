package com.xiaou.filestorage.service;

import com.xiaou.filestorage.domain.StorageConfig;

import java.util.List;
import java.util.Map;

/**
 * 存储配置服务接口
 *
 * @author xiaou
 */
public interface StorageConfigService {

    /**
     * 创建存储配置
     *
     * @param storageConfig 存储配置
     * @return 创建是否成功
     */
    boolean createConfig(StorageConfig storageConfig);

    /**
     * 更新存储配置
     *
     * @param storageConfig 存储配置
     * @return 更新是否成功
     */
    boolean updateConfig(StorageConfig storageConfig);

    /**
     * 删除存储配置
     *
     * @param id 配置ID
     * @return 删除是否成功
     */
    boolean deleteConfig(Long id);

    /**
     * 根据ID查询存储配置
     *
     * @param id 配置ID
     * @return 存储配置
     */
    StorageConfig getConfigById(Long id);

    /**
     * 查询所有存储配置
     *
     * @param storageType 存储类型
     * @param isEnabled   是否启用
     * @return 存储配置列表
     */
    List<StorageConfig> listConfigs(String storageType, Integer isEnabled);

    /**
     * 获取默认存储配置
     *
     * @return 默认存储配置
     */
    StorageConfig getDefaultConfig();

    /**
     * 设置默认存储配置
     *
     * @param id 配置ID
     * @return 设置是否成功
     */
    boolean setDefaultConfig(Long id);

    /**
     * 启用/禁用存储配置
     *
     * @param id        配置ID
     * @param isEnabled 是否启用
     * @return 操作是否成功
     */
    boolean toggleConfig(Long id, Integer isEnabled);

    /**
     * 测试存储配置
     *
     * @param id 配置ID
     * @return 测试结果
     */
    Map<String, Object> testConfig(Long id);

    /**
     * 获取支持的存储类型列表
     *
     * @return 存储类型列表
     */
    List<String> getSupportedStorageTypes();
} 