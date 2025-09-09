package com.xiaou.filestorage.mapper;

import com.xiaou.filestorage.domain.FileMigration;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 文件迁移数据访问接口
 *
 * @author xiaou
 */
@Mapper
public interface FileMigrationMapper {

    /**
     * 插入迁移任务
     *
     * @param fileMigration 迁移任务
     * @return 影响行数
     */
    int insert(FileMigration fileMigration);

    /**
     * 根据ID查询迁移任务
     *
     * @param id 任务ID
     * @return 迁移任务
     */
    FileMigration selectById(@Param("id") Long id);

    /**
     * 按条件查询迁移任务列表
     *
     * @param status 任务状态
     * @param limit  限制数量
     * @return 迁移任务列表
     */
    List<FileMigration> selectByCondition(@Param("status") String status, @Param("limit") Integer limit);

    /**
     * 查询指定存储的迁移任务
     *
     * @param sourceStorageId 源存储ID
     * @param targetStorageId 目标存储ID
     * @return 迁移任务列表
     */
    List<FileMigration> selectByStorageIds(@Param("sourceStorageId") Long sourceStorageId,
                                          @Param("targetStorageId") Long targetStorageId);

    /**
     * 更新迁移任务
     *
     * @param fileMigration 迁移任务
     * @return 影响行数
     */
    int updateById(FileMigration fileMigration);

    /**
     * 删除迁移任务
     *
     * @param id 任务ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新迁移进度
     *
     * @param id           任务ID
     * @param successCount 成功数量
     * @param failCount    失败数量
     * @return 影响行数
     */
    int updateProgress(@Param("id") Long id,
                      @Param("successCount") Integer successCount,
                      @Param("failCount") Integer failCount);

    /**
     * 更新任务状态
     *
     * @param id     任务ID
     * @param status 任务状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 统计各状态的任务数量
     *
     * @return 统计结果
     */
    @MapKey("status")
    List<Map<String, Object>> countByStatus();
} 