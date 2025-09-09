package com.xiaou.filestorage.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 存储配置实体
 *
 * @author xiaou
 */
@Data
public class StorageConfig {

    /**
     * 配置ID
     */
    private Long id;

    /**
     * 存储类型: LOCAL,OSS,COS,KODO,OBS
     */
    private String storageType;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置参数JSON
     */
    private String configParams;

    /**
     * 是否默认存储: 0=否, 1=是
     */
    private Integer isDefault;

    /**
     * 是否启用: 0=禁用, 1=启用
     */
    private Integer isEnabled;

    /**
     * 测试状态: 0=失败, 1=成功, null=未测试
     */
    private Integer testStatus;

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