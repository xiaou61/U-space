package com.xiaou.filestorage.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 文件存储记录实体
 *
 * @author xiaou
 */
@Data
public class FileStorage {

    /**
     * 存储记录ID
     */
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 存储配置ID
     */
    private Long storageConfigId;

    /**
     * 存储路径
     */
    private String storagePath;

    /**
     * 是否主存储: 0=备份, 1=主存储
     */
    private Integer isPrimary;

    /**
     * 同步状态: 0=同步中, 1=已同步, 2=同步失败
     */
    private Integer syncStatus;

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