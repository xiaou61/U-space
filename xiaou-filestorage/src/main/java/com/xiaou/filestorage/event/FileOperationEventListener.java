package com.xiaou.filestorage.event;

import com.xiaou.filestorage.domain.FileAccess;
import com.xiaou.filestorage.mapper.FileAccessMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 文件操作事件监听器
 *
 * @author xiaou
 */
@Slf4j
@Component
public class FileOperationEventListener {

    @Autowired
    private FileAccessMapper fileAccessMapper;

    /**
     * 监听文件操作事件，记录访问日志
     *
     * @param event 文件操作事件
     */
    @EventListener
    @Async
    public void handleFileOperationEvent(FileOperationEvent event) {
        try {
            // 记录文件访问日志
            recordFileAccess(event);
            
            // 根据事件类型执行不同的处理
            switch (event.getEventType()) {
                case UPLOAD:
                    handleUploadEvent(event);
                    break;
                case DOWNLOAD:
                    handleDownloadEvent(event);
                    break;
                case ACCESS:
                    handleAccessEvent(event);
                    break;
                case DELETE:
                    handleDeleteEvent(event);
                    break;
                case MIGRATE:
                    handleMigrateEvent(event);
                    break;
                default:
                    break;
            }
            
        } catch (Exception e) {
            log.error("处理文件操作事件失败: eventType={}, fileId={}, error={}", 
                event.getEventType(), event.getFileId(), e.getMessage(), e);
        }
    }

    /**
     * 记录文件访问日志
     *
     * @param event 文件操作事件
     */
    private void recordFileAccess(FileOperationEvent event) {
        try {
            if (event.getFileId() == null) {
                return;
            }
            
            FileAccess fileAccess = new FileAccess();
            fileAccess.setFileId(event.getFileId());
            fileAccess.setAccessTime(event.getOperationTime());
            fileAccess.setAccessIp(event.getOperationIp());
            fileAccess.setModuleName(event.getModuleName());
            fileAccess.setUserId(event.getUserId());
            
            // 根据事件类型设置访问类型
            switch (event.getEventType()) {
                case DOWNLOAD:
                    fileAccess.setAccessType("DOWNLOAD");
                    break;
                case ACCESS:
                    fileAccess.setAccessType("VIEW");
                    break;
                default:
                    fileAccess.setAccessType(event.getEventType().name());
                    break;
            }
            
            fileAccessMapper.insert(fileAccess);
            
        } catch (Exception e) {
            log.error("记录文件访问日志失败: fileId={}, error={}", event.getFileId(), e.getMessage(), e);
        }
    }

    /**
     * 处理上传事件
     *
     * @param event 上传事件
     */
    private void handleUploadEvent(FileOperationEvent event) {
        log.info("文件上传完成: fileId={}, fileName={}, size={}", 
            event.getFileId(), event.getFileName(), event.getFileSize());
    }

    /**
     * 处理下载事件
     *
     * @param event 下载事件
     */
    private void handleDownloadEvent(FileOperationEvent event) {
        log.debug("文件下载: fileId={}, fileName={}, ip={}", 
            event.getFileId(), event.getFileName(), event.getOperationIp());
    }

    /**
     * 处理访问事件
     *
     * @param event 访问事件
     */
    private void handleAccessEvent(FileOperationEvent event) {
        log.debug("文件访问: fileId={}, fileName={}, ip={}", 
            event.getFileId(), event.getFileName(), event.getOperationIp());
    }

    /**
     * 处理删除事件
     *
     * @param event 删除事件
     */
    private void handleDeleteEvent(FileOperationEvent event) {
        log.info("文件删除: fileId={}, fileName={}, module={}", 
            event.getFileId(), event.getFileName(), event.getModuleName());
    }

    /**
     * 处理迁移事件
     *
     * @param event 迁移事件
     */
    private void handleMigrateEvent(FileOperationEvent event) {
        log.info("文件迁移: fileId={}, fileName={}, info={}", 
            event.getFileId(), event.getFileName(), event.getExtraInfo());
    }
} 