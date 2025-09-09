package com.xiaou.filestorage.strategy;

import com.xiaou.filestorage.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件存储策略接口
 *
 * @author xiaou
 */
public interface FileStorageStrategy {

    /**
     * 获取存储类型
     *
     * @return 存储类型标识
     */
    String getStorageType();

    /**
     * 初始化存储配置
     *
     * @param configParams 配置参数
     * @return 初始化是否成功
     */
    boolean initialize(Map<String, Object> configParams);

    /**
     * 测试存储连接
     *
     * @return 连接是否正常
     */
    boolean testConnection();

    /**
     * 上传文件
     *
     * @param file       文件
     * @param storagePath 存储路径
     * @return 上传结果
     */
    FileUploadResult uploadFile(MultipartFile file, String storagePath);

    /**
     * 上传文件流
     *
     * @param inputStream 文件流
     * @param fileName    文件名
     * @param storagePath 存储路径
     * @param contentType 内容类型
     * @return 上传结果
     */
    FileUploadResult uploadFile(InputStream inputStream, String fileName, String storagePath, String contentType);

    /**
     * 下载文件
     *
     * @param storagePath 存储路径
     * @return 文件流
     */
    InputStream downloadFile(String storagePath);

    /**
     * 删除文件
     *
     * @param storagePath 存储路径
     * @return 删除是否成功
     */
    boolean deleteFile(String storagePath);

    /**
     * 文件是否存在
     *
     * @param storagePath 存储路径
     * @return 文件是否存在
     */
    boolean existsFile(String storagePath);

    /**
     * 获取文件访问URL
     *
     * @param storagePath 存储路径
     * @param expireHours 过期时间(小时), null表示永久有效
     * @return 访问URL
     */
    String getFileUrl(String storagePath, Integer expireHours);

    /**
     * 复制文件
     *
     * @param sourceStoragePath 源存储路径
     * @param targetStoragePath 目标存储路径
     * @return 复制是否成功
     */
    boolean copyFile(String sourceStoragePath, String targetStoragePath);

    /**
     * 获取文件大小
     *
     * @param storagePath 存储路径
     * @return 文件大小(字节)
     */
    Long getFileSize(String storagePath);
} 