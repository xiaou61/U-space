package com.xiaou.filestorage.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 文件迁移任务实体
 *
 * @author xiaou
 */
@Data
public class FileMigration {

    /**
     * 迁移任务ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 源存储配置ID
     */
    private Long sourceStorageId;

    /**
     * 目标存储配置ID
     */
    private Long targetStorageId;

    /**
     * 迁移类型: FULL,INCREMENTAL,TIME_RANGE,FILE_TYPE
     */
    private String migrationType;

    /**
     * 筛选参数JSON
     */
    private String filterParams;

    /**
     * 总文件数
     */
    private Integer totalFiles;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 任务状态: PENDING,RUNNING,COMPLETED,FAILED,STOPPED
     */
    private String status;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
} 