package com.xiaou.filestorage.event;

import lombok.Data;

import java.util.Date;

/**
 * 文件操作事件
 *
 * @author xiaou
 */
@Data
public class FileOperationEvent {

    /**
     * 事件类型
     */
    public enum EventType {
        UPLOAD("上传"), 
        DOWNLOAD("下载"), 
        DELETE("删除"), 
        ACCESS("访问"),
        MIGRATE("迁移");
        
        private final String description;
        
        EventType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作IP
     */
    private String operationIp;

    /**
     * 存储配置ID
     */
    private Long storageConfigId;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 操作时间
     */
    private Date operationTime;

    /**
     * 扩展信息
     */
    private String extraInfo;

    /**
     * 创建上传事件
     */
    public static FileOperationEvent upload(Long fileId, String fileName, String moduleName, String businessType, Long fileSize) {
        FileOperationEvent event = new FileOperationEvent();
        event.setEventType(EventType.UPLOAD);
        event.setFileId(fileId);
        event.setFileName(fileName);
        event.setModuleName(moduleName);
        event.setBusinessType(businessType);
        event.setFileSize(fileSize);
        event.setOperationTime(new Date());
        return event;
    }

    /**
     * 创建下载事件
     */
    public static FileOperationEvent download(Long fileId, String fileName, String operationIp) {
        FileOperationEvent event = new FileOperationEvent();
        event.setEventType(EventType.DOWNLOAD);
        event.setFileId(fileId);
        event.setFileName(fileName);
        event.setOperationIp(operationIp);
        event.setOperationTime(new Date());
        return event;
    }

    /**
     * 创建访问事件
     */
    public static FileOperationEvent access(Long fileId, String fileName, String operationIp, String moduleName) {
        FileOperationEvent event = new FileOperationEvent();
        event.setEventType(EventType.ACCESS);
        event.setFileId(fileId);
        event.setFileName(fileName);
        event.setOperationIp(operationIp);
        event.setModuleName(moduleName);
        event.setOperationTime(new Date());
        return event;
    }

    /**
     * 创建删除事件
     */
    public static FileOperationEvent delete(Long fileId, String fileName, String moduleName) {
        FileOperationEvent event = new FileOperationEvent();
        event.setEventType(EventType.DELETE);
        event.setFileId(fileId);
        event.setFileName(fileName);
        event.setModuleName(moduleName);
        event.setOperationTime(new Date());
        return event;
    }
} 