package com.xiaou.filestorage.service;

import com.xiaou.filestorage.domain.FileMigration;

import java.util.List;
import java.util.Map;

/**
 * 文件迁移服务接口
 *
 * @author xiaou
 */
public interface FileMigrationService {

    /**
     * 创建迁移任务
     *
     * @param sourceStorageId 源存储配置ID
     * @param targetStorageId 目标存储配置ID
     * @param migrationType   迁移类型
     * @param filterParams    筛选参数
     * @param taskName        任务名称
     * @return 迁移任务ID
     */
    Long createMigrationTask(Long sourceStorageId, Long targetStorageId, String migrationType, 
                           Map<String, Object> filterParams, String taskName);

    /**
     * 执行迁移任务
     *
     * @param migrationId 迁移任务ID
     * @return 执行是否成功
     */
    boolean executeMigration(Long migrationId);

    /**
     * 停止迁移任务
     *
     * @param migrationId 迁移任务ID
     * @return 停止是否成功
     */
    boolean stopMigration(Long migrationId);

    /**
     * 查询迁移任务状态
     *
     * @param migrationId 迁移任务ID
     * @return 任务信息
     */
    FileMigration getMigrationTask(Long migrationId);

    /**
     * 查询迁移任务列表
     *
     * @param status 任务状态
     * @param limit  限制数量
     * @return 任务列表
     */
    List<FileMigration> listMigrationTasks(String status, Integer limit);

    /**
     * 删除迁移任务
     *
     * @param migrationId 迁移任务ID
     * @return 删除是否成功
     */
    boolean deleteMigrationTask(Long migrationId);

    /**
     * 获取迁移进度
     *
     * @param migrationId 迁移任务ID
     * @return 进度信息
     */
    Map<String, Object> getMigrationProgress(Long migrationId);
} 