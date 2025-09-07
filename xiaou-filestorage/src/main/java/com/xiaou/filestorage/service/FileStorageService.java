package com.xiaou.filestorage.service;

import com.xiaou.filestorage.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件存储服务接口
 *
 * @author xiaou
 */
public interface FileStorageService {

    /**
     * 单文件上传
     *
     * @param file         文件
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @return 上传结果
     */
    FileUploadResult uploadSingle(MultipartFile file, String moduleName, String businessType);

    /**
     * 批量文件上传
     *
     * @param files        文件数组
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @return 上传结果列表
     */
    List<FileUploadResult> uploadBatch(MultipartFile[] files, String moduleName, String businessType);

    /**
     * 获取文件下载流
     *
     * @param fileId 文件ID
     * @return 文件流
     */
    InputStream downloadFile(Long fileId);

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    Map<String, Object> getFileInfo(Long fileId);

    /**
     * 获取文件访问URL
     *
     * @param fileId      文件ID
     * @param expireHours 过期时间(小时)
     * @return 访问URL
     */
    String getFileUrl(Long fileId, Integer expireHours);

    /**
     * 批量获取文件URL
     *
     * @param fileIds 文件ID列表
     * @return 文件ID与URL映射
     */
    Map<Long, String> getFileUrls(List<Long> fileIds);

    /**
     * 删除文件(逻辑删除)
     *
     * @param fileId     文件ID
     * @param moduleName 模块名称(权限验证)
     * @return 删除是否成功
     */
    boolean deleteFile(Long fileId, String moduleName);

    /**
     * 查询文件列表
     *
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @param pageNum      页码
     * @param pageSize     页大小
     * @return 文件列表
     */
    Map<String, Object> listFiles(String moduleName, String businessType, Integer pageNum, Integer pageSize);

    /**
     * 检查文件是否存在
     *
     * @param fileIds 文件ID列表
     * @return 文件ID与存在状态映射
     */
    Map<Long, Boolean> checkFilesExists(List<Long> fileIds);
} 