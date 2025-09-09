package com.xiaou.filestorage.mapper;

import com.xiaou.filestorage.domain.StorageConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 存储配置数据访问接口
 *
 * @author xiaou
 */
@Mapper
public interface StorageConfigMapper {

    /**
     * 插入存储配置
     *
     * @param storageConfig 存储配置
     * @return 影响行数
     */
    int insert(StorageConfig storageConfig);

    /**
     * 根据ID查询存储配置
     *
     * @param id 配置ID
     * @return 存储配置
     */
    StorageConfig selectById(@Param("id") Long id);

    /**
     * 根据配置名称查询存储配置
     *
     * @param configName 配置名称
     * @return 存储配置
     */
    StorageConfig selectByConfigName(@Param("configName") String configName);

    /**
     * 查询默认存储配置
     *
     * @return 默认存储配置
     */
    StorageConfig selectDefault();

    /**
     * 查询所有启用的存储配置
     *
     * @return 存储配置列表
     */
    List<StorageConfig> selectEnabled();

    /**
     * 查询所有存储配置
     *
     * @param storageType 存储类型
     * @param isEnabled   是否启用
     * @return 存储配置列表
     */
    List<StorageConfig> selectByCondition(@Param("storageType") String storageType,
                                          @Param("isEnabled") Integer isEnabled);

    /**
     * 更新存储配置
     *
     * @param storageConfig 存储配置
     * @return 影响行数
     */
    int updateById(StorageConfig storageConfig);

    /**
     * 删除存储配置
     *
     * @param id 配置ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新默认配置状态
     *
     * @param id        配置ID
     * @param isDefault 是否默认
     * @return 影响行数
     */
    int updateDefaultStatus(@Param("id") Long id, @Param("isDefault") Integer isDefault);

    /**
     * 清除所有默认配置状态
     *
     * @return 影响行数
     */
    int clearAllDefaultStatus();

    /**
     * 更新启用状态
     *
     * @param id        配置ID
     * @param isEnabled 是否启用
     * @return 影响行数
     */
    int updateEnabledStatus(@Param("id") Long id, @Param("isEnabled") Integer isEnabled);

    /**
     * 更新测试状态
     *
     * @param id         配置ID
     * @param testStatus 测试状态
     * @return 影响行数
     */
    int updateTestStatus(@Param("id") Long id, @Param("testStatus") Integer testStatus);
} 