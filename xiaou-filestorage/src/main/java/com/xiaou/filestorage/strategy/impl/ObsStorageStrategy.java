package com.xiaou.filestorage.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.obs.services.ObsClient;
import com.obs.services.model.*;
import com.xiaou.filestorage.dto.FileUploadResult;
import com.xiaou.filestorage.strategy.AbstractFileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * 华为云OBS存储策略实现
 *
 * @author xiaou
 */
@Slf4j
@Component
public class ObsStorageStrategy extends AbstractFileStorageStrategy {

    private ObsClient obsClient;
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String secretAccessKey;
    private String domain;

    @Override
    public String getStorageType() {
        return "OBS";
    }

    @Override
    protected boolean doInitialize(Map<String, Object> configParams) {
        try {
            this.endpoint = getConfigParam("endpoint");
            this.bucketName = getConfigParam("bucketName");
            this.accessKeyId = getConfigParam("accessKeyId");
            this.secretAccessKey = getConfigParam("secretAccessKey");
            this.domain = getConfigParam("domain");

            if (StrUtil.hasBlank(endpoint, bucketName, accessKeyId, secretAccessKey)) {
                log.error("OBS配置参数不完整");
                return false;
            }

            // 创建OBS客户端
            this.obsClient = new ObsClient(accessKeyId, secretAccessKey, endpoint);
            
            return true;
        } catch (Exception e) {
            log.error("初始化OBS客户端失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected boolean doTestConnection() {
        try {
            // 检查bucket是否存在
            return obsClient.headBucket(bucketName);
        } catch (Exception e) {
            log.error("测试OBS连接失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String storagePath) {
        try {
            if (StrUtil.isBlank(storagePath)) {
                storagePath = generateStoragePath(file.getOriginalFilename(), "default", "upload");
            }

            // 创建上传请求
            PutObjectRequest request = new PutObjectRequest(bucketName, storagePath);
            request.setInput(file.getInputStream());
            request.getMetadata().setContentType(file.getContentType());
            request.getMetadata().setContentLength(file.getSize());
            
            // 上传文件
            PutObjectResult result = obsClient.putObject(request);
            
            // 生成访问URL
            String accessUrl = getFileUrl(storagePath, null);
            
            return FileUploadResult.success(storagePath, accessUrl, file.getSize());
            
        } catch (Exception e) {
            log.error("OBS上传文件失败: {}", e.getMessage(), e);
            return FileUploadResult.failure("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResult uploadFile(InputStream inputStream, String fileName, String storagePath, String contentType) {
        try {
            if (StrUtil.isBlank(storagePath)) {
                storagePath = generateStoragePath(fileName, "default", "upload");
            }

            // 创建上传请求
            PutObjectRequest request = new PutObjectRequest(bucketName, storagePath);
            request.setInput(inputStream);
            if (StrUtil.isNotBlank(contentType)) {
                request.getMetadata().setContentType(contentType);
            }
            
            // 上传文件
            PutObjectResult result = obsClient.putObject(request);
            
            // 生成访问URL
            String accessUrl = getFileUrl(storagePath, null);
            
            // 获取文件大小
            Long fileSize = getFileSize(storagePath);
            
            return FileUploadResult.success(storagePath, accessUrl, fileSize);
            
        } catch (Exception e) {
            log.error("OBS上传文件流失败: {}", e.getMessage(), e);
            return FileUploadResult.failure("上传文件流失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return null;
            }
            ObsObject obsObject = obsClient.getObject(bucketName, storagePath);
            return obsObject.getObjectContent();
        } catch (Exception e) {
            log.error("OBS下载文件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteFile(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return true;
            }
            obsClient.deleteObject(bucketName, storagePath);
            return true;
        } catch (Exception e) {
            log.error("OBS删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsFile(String storagePath) {
        try {
            return obsClient.doesObjectExist(bucketName, storagePath);
        } catch (Exception e) {
            log.error("OBS检查文件存在性失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String storagePath, Integer expireHours) {
        try {
            if (StrUtil.isNotBlank(domain)) {
                // 使用自定义域名
                return domain + "/" + storagePath;
            }
            
            if (expireHours != null && expireHours > 0) {
                // 生成临时访问URL
                TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.GET, expireHours * 3600);
                request.setBucketName(bucketName);
                request.setObjectKey(storagePath);
                
                TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
                return response.getSignedUrl();
            } else {
                // 生成永久访问URL (需要bucket为公共读权限)
                return String.format("https://%s.%s/%s", bucketName, endpoint.replace("https://", ""), storagePath);
            }
        } catch (Exception e) {
            log.error("OBS生成文件URL失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean copyFile(String sourceStoragePath, String targetStoragePath) {
        try {
            if (!existsFile(sourceStoragePath)) {
                return false;
            }
            
            obsClient.copyObject(bucketName, sourceStoragePath, bucketName, targetStoragePath);
            return true;
            
        } catch (Exception e) {
            log.error("OBS复制文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Long getFileSize(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return null;
            }
            return obsClient.getObjectMetadata(bucketName, storagePath).getContentLength();
        } catch (Exception e) {
            log.error("OBS获取文件大小失败: {}", e.getMessage(), e);
            return null;
        }
    }
} 