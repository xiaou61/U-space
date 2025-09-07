package com.xiaou.filestorage.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 文件操作事件发布器
 *
 * @author xiaou
 */
@Slf4j
@Component
public class FileOperationEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布文件操作事件
     *
     * @param event 文件操作事件
     */
    public void publishEvent(FileOperationEvent event) {
        try {
            applicationEventPublisher.publishEvent(event);
            log.debug("发布文件操作事件: type={}, fileId={}, fileName={}", 
                event.getEventType(), event.getFileId(), event.getFileName());
        } catch (Exception e) {
            log.error("发布文件操作事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发布上传事件
     *
     * @param fileId       文件ID
     * @param fileName     文件名
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @param fileSize     文件大小
     */
    public void publishUploadEvent(Long fileId, String fileName, String moduleName, String businessType, Long fileSize) {
        FileOperationEvent event = FileOperationEvent.upload(fileId, fileName, moduleName, businessType, fileSize);
        publishEvent(event);
    }

    /**
     * 发布下载事件
     *
     * @param fileId      文件ID
     * @param fileName    文件名
     * @param operationIp 操作IP
     */
    public void publishDownloadEvent(Long fileId, String fileName, String operationIp) {
        FileOperationEvent event = FileOperationEvent.download(fileId, fileName, operationIp);
        publishEvent(event);
    }

    /**
     * 发布访问事件
     *
     * @param fileId      文件ID
     * @param fileName    文件名
     * @param operationIp 操作IP
     * @param moduleName  模块名称
     */
    public void publishAccessEvent(Long fileId, String fileName, String operationIp, String moduleName) {
        FileOperationEvent event = FileOperationEvent.access(fileId, fileName, operationIp, moduleName);
        publishEvent(event);
    }

    /**
     * 发布删除事件
     *
     * @param fileId     文件ID
     * @param fileName   文件名
     * @param moduleName 模块名称
     */
    public void publishDeleteEvent(Long fileId, String fileName, String moduleName) {
        FileOperationEvent event = FileOperationEvent.delete(fileId, fileName, moduleName);
        publishEvent(event);
    }
} 