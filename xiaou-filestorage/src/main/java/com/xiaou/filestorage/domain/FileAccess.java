package com.xiaou.filestorage.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 文件访问记录实体
 *
 * @author xiaou
 */
@Data
public class FileAccess {

    /**
     * 访问记录ID
     */
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date accessTime;

    /**
     * 访问IP
     */
    private String accessIp;

    /**
     * 访问模块
     */
    private String moduleName;

    /**
     * 访问用户ID
     */
    private Long userId;

    /**
     * 访问类型: VIEW,DOWNLOAD
     */
    private String accessType;
} 