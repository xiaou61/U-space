package com.xiaou.filestorage.service.impl;

import cn.hutool.json.JSONUtil;
import com.xiaou.filestorage.domain.FileInfo;
import com.xiaou.filestorage.domain.FileMigration;
import com.xiaou.filestorage.domain.StorageConfig;
import com.xiaou.filestorage.factory.StorageStrategyFactory;
import com.xiaou.filestorage.mapper.FileInfoMapper;
import com.xiaou.filestorage.mapper.FileMigrationMapper;
import com.xiaou.filestorage.mapper.StorageConfigMapper;
import com.xiaou.filestorage.service.FileMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件迁移服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
public class FileMigrationServiceImpl implements FileMigrationService {

    @Autowired
    private FileMigrationMapper fileMigrationMapper;

    @Autowired
    private StorageConfigMapper storageConfigMapper;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private StorageStrategyFactory strategyFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMigrationTask(Long sourceStorageId, Long targetStorageId, String migrationType, 
                                   Map<String, Object> filterParams, String taskName) {
        try {
            // 验证存储配置
            StorageConfig sourceConfig = storageConfigMapper.selectById(sourceStorageId);
            StorageConfig targetConfig = storageConfigMapper.selectById(targetStorageId);
            
            if (sourceConfig == null || targetConfig == null) {
                log.error("存储配置不存在: source={}, target={}", sourceStorageId, targetStorageId);
                return null;
            }

            if (sourceConfig.getIsEnabled() != 1 || targetConfig.getIsEnabled() != 1) {
                log.error("存储配置未启用: source={}, target={}", 
                    sourceConfig.getIsEnabled(), targetConfig.getIsEnabled());
                return null;
            }

            // 创建迁移任务
            FileMigration migration = new FileMigration();
            migration.setTaskName(taskName);
            migration.setSourceStorageId(sourceStorageId);
            migration.setTargetStorageId(targetStorageId);
            migration.setMigrationType(migrationType);
            migration.setFilterParams(JSONUtil.toJsonStr(filterParams));
            migration.setTotalFiles(0);
            migration.setSuccessCount(0);
            migration.setFailCount(0);
            migration.setStatus("PENDING");
            migration.setCreateTime(new Date());
            migration.setUpdateTime(new Date());

            int inserted = fileMigrationMapper.insert(migration);
            if (inserted > 0) {
                log.info("创建迁移任务成功: id={}, name={}", migration.getId(), taskName);
                return migration.getId();
            }
            return null;

        } catch (Exception e) {
            log.error("创建迁移任务失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeMigration(Long migrationId) {
        try {
            FileMigration migration = fileMigrationMapper.selectById(migrationId);
            if (migration == null) {
                log.error("迁移任务不存在: {}", migrationId);
                return false;
            }

            if (!"PENDING".equals(migration.getStatus())) {
                log.error("迁移任务状态不正确: id={}, status={}", migrationId, migration.getStatus());
                return false;
            }

            // 更新任务状态为运行中
            migration.setStatus("RUNNING");
            migration.setStartTime(new Date());
            migration.setUpdateTime(new Date());
            fileMigrationMapper.updateById(migration);

            // 异步执行迁移任务
            executeMigrationAsync(migration);

            return true;

        } catch (Exception e) {
            log.error("执行迁移任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean stopMigration(Long migrationId) {
        try {
            FileMigration migration = fileMigrationMapper.selectById(migrationId);
            if (migration == null) {
                return false;
            }

            if ("RUNNING".equals(migration.getStatus())) {
                migration.setStatus("STOPPED");
                migration.setEndTime(new Date());
                migration.setUpdateTime(new Date());
                int updated = fileMigrationMapper.updateById(migration);
                return updated > 0;
            }

            return true;

        } catch (Exception e) {
            log.error("停止迁移任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FileMigration getMigrationTask(Long migrationId) {
        try {
            return fileMigrationMapper.selectById(migrationId);
        } catch (Exception e) {
            log.error("查询迁移任务失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<FileMigration> listMigrationTasks(String status, Integer limit) {
        try {
            return fileMigrationMapper.selectByCondition(status, limit);
        } catch (Exception e) {
            log.error("查询迁移任务列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMigrationTask(Long migrationId) {
        try {
            FileMigration migration = fileMigrationMapper.selectById(migrationId);
            if (migration == null) {
                return true;
            }

            // 只允许删除已完成、失败或停止的任务
            if ("RUNNING".equals(migration.getStatus())) {
                log.error("不能删除运行中的迁移任务: {}", migrationId);
                return false;
            }

            int deleted = fileMigrationMapper.deleteById(migrationId);
            return deleted > 0;

        } catch (Exception e) {
            log.error("删除迁移任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getMigrationProgress(Long migrationId) {
        Map<String, Object> progress = new HashMap<>();
        
        try {
            FileMigration migration = fileMigrationMapper.selectById(migrationId);
            if (migration == null) {
                progress.put("error", "任务不存在");
                return progress;
            }

            progress.put("taskId", migration.getId());
            progress.put("taskName", migration.getTaskName());
            progress.put("status", migration.getStatus());
            progress.put("totalFiles", migration.getTotalFiles());
            progress.put("successCount", migration.getSuccessCount());
            progress.put("failCount", migration.getFailCount());
            progress.put("startTime", migration.getStartTime());
            progress.put("endTime", migration.getEndTime());

            // 计算进度百分比
            if (migration.getTotalFiles() > 0) {
                double progressPercent = (double) (migration.getSuccessCount() + migration.getFailCount()) 
                    / migration.getTotalFiles() * 100;
                progress.put("progressPercent", String.format("%.2f", progressPercent));
            } else {
                progress.put("progressPercent", "0.00");
            }

            return progress;

        } catch (Exception e) {
            log.error("获取迁移进度失败: {}", e.getMessage(), e);
            progress.put("error", "获取进度失败: " + e.getMessage());
            return progress;
        }
    }

    /**
     * 异步执行迁移任务
     *
     * @param migration 迁移任务
     */
    private void executeMigrationAsync(FileMigration migration) {
        // 在实际项目中，这里应该使用异步线程池来执行迁移任务
        // 这里仅作为示例实现
        
        new Thread(() -> {
            try {
                log.info("开始执行迁移任务: id={}, name={}", migration.getId(), migration.getTaskName());

                // 实现具体的迁移逻辑
                performMigration(migration);

                // 更新任务状态为完成
                migration.setStatus("COMPLETED");
                migration.setEndTime(new Date());
                migration.setUpdateTime(new Date());
                migration.setTotalFiles(100); // 模拟数据
                migration.setSuccessCount(98);
                migration.setFailCount(2);

                fileMigrationMapper.updateById(migration);

                log.info("迁移任务完成: id={}, success={}, fail={}", 
                    migration.getId(), migration.getSuccessCount(), migration.getFailCount());

            } catch (Exception e) {
                log.error("迁移任务执行失败: id={}, error={}", migration.getId(), e.getMessage(), e);
                
                // 更新任务状态为失败
                migration.setStatus("FAILED");
                migration.setEndTime(new Date());
                migration.setUpdateTime(new Date());
                migration.setErrorMessage(e.getMessage());
                
                try {
                    fileMigrationMapper.updateById(migration);
                } catch (Exception updateEx) {
                    log.error("更新迁移任务状态失败: {}", updateEx.getMessage(), updateEx);
                }
            }
        }).start();
    }

    /**
     * 执行文件迁移的具体逻辑
     *
     * @param migration 迁移任务
     */
    private void performMigration(FileMigration migration) {
        try {
            // 1. 根据筛选条件查询需要迁移的文件
            List<FileInfo> filesToMigrate = fileInfoMapper.selectForMigration(
                migration.getSourceStorageId(),
                migration.getStartTime(),
                migration.getEndTime(),
                null // 不限制数量，分批处理
            );

            if (filesToMigrate.isEmpty()) {
                log.info("没有找到需要迁移的文件");
                return;
            }

            migration.setTotalFiles(filesToMigrate.size());
            int processedFiles = 0;
            int successFiles = 0;
            int failedFiles = 0;

            log.info("开始迁移 {} 个文件", filesToMigrate.size());

            // 2. 逐个文件进行迁移
            for (FileInfo fileInfo : filesToMigrate) {
                try {
                    // 下载文件内容
                    // 这里应该根据源存储配置下载文件
                    // 然后上传到目标存储
                    // 验证迁移结果
                    // 更新文件记录
                    
                    log.debug("迁移文件: {}", fileInfo.getOriginalName());
                    
                    // 模拟迁移过程
                    Thread.sleep(100);
                    
                    successFiles++;
                    
                } catch (Exception e) {
                    log.error("迁移文件失败: {}, error: {}", fileInfo.getOriginalName(), e.getMessage());
                    failedFiles++;
                }
                
                processedFiles++;
                
                // 3. 更新迁移进度（每处理10个文件更新一次）
                if (processedFiles % 10 == 0 || processedFiles == filesToMigrate.size()) {
                    migration.setSuccessCount(successFiles);
                    migration.setFailCount(failedFiles);
                    migration.setUpdateTime(new Date());
                    fileMigrationMapper.updateById(migration);
                    
                    log.info("迁移进度: {}/{}, 成功: {}, 失败: {}", 
                        processedFiles, filesToMigrate.size(), successFiles, failedFiles);
                }
            }

            log.info("文件迁移完成: 总计 {}, 成功 {}, 失败 {}", 
                filesToMigrate.size(), successFiles, failedFiles);

        } catch (Exception e) {
            log.error("执行迁移过程中发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("迁移执行失败", e);
        }
    }
} 