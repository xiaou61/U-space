package com.xiaou.filestorage.mapper;

import com.xiaou.filestorage.domain.FileStorage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件存储记录数据访问接口
 *
 * @author xiaou
 */
@Mapper
public interface FileStorageMapper {

    /**
     * 插入文件存储记录
     *
     * @param fileStorage 存储记录
     * @return 影响行数
     */
    int insert(FileStorage fileStorage);

    /**
     * 根据ID查询存储记录
     *
     * @param id 记录ID
     * @return 存储记录
     */
    FileStorage selectById(@Param("id") Long id);

    /**
     * 根据文件ID查询存储记录
     *
     * @param fileId 文件ID
     * @return 存储记录列表
     */
    List<FileStorage> selectByFileId(@Param("fileId") Long fileId);

    /**
     * 根据存储配置ID查询存储记录
     *
     * @param storageConfigId 存储配置ID
     * @return 存储记录列表
     */
    List<FileStorage> selectByStorageConfigId(@Param("storageConfigId") Long storageConfigId);

    /**
     * 查询主存储记录
     *
     * @param fileId 文件ID
     * @return 主存储记录
     */
    FileStorage selectPrimaryByFileId(@Param("fileId") Long fileId);

    /**
     * 更新存储记录
     *
     * @param fileStorage 存储记录
     * @return 影响行数
     */
    int updateById(FileStorage fileStorage);

    /**
     * 删除存储记录
     *
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据文件ID删除存储记录
     *
     * @param fileId 文件ID
     * @return 影响行数
     */
    int deleteByFileId(@Param("fileId") Long fileId);

    /**
     * 更新同步状态
     *
     * @param id         记录ID
     * @param syncStatus 同步状态
     * @return 影响行数
     */
    int updateSyncStatus(@Param("id") Long id, @Param("syncStatus") Integer syncStatus);

    /**
     * 批量更新主存储状态
     *
     * @param fileId    文件ID
     * @param isPrimary 是否主存储
     * @return 影响行数
     */
    int updatePrimaryStatus(@Param("fileId") Long fileId, @Param("isPrimary") Integer isPrimary);
} 