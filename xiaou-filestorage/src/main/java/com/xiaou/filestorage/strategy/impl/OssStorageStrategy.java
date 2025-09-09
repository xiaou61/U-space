package com.xiaou.filestorage.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.xiaou.filestorage.dto.FileUploadResult;
import com.xiaou.filestorage.strategy.AbstractFileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * 阿里云OSS存储策略实现
 *
 * @author xiaou
 */
@Slf4j
@Component
public class OssStorageStrategy extends AbstractFileStorageStrategy {

    private OSS ossClient;
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private String domain;

    @Override
    public String getStorageType() {
        return "OSS";
    }

    @Override
    protected boolean doInitialize(Map<String, Object> configParams) {
        try {
            this.endpoint = getConfigParam("endpoint");
            this.bucketName = getConfigParam("bucketName");
            this.accessKeyId = getConfigParam("accessKeyId");
            this.accessKeySecret = getConfigParam("accessKeySecret");
            this.domain = getConfigParam("domain");

            if (StrUtil.hasBlank(endpoint, bucketName, accessKeyId, accessKeySecret)) {
                log.error("OSS配置参数不完整");
                return false;
            }

            // 创建OSS客户端
            this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            return true;
        } catch (Exception e) {
            log.error("初始化OSS客户端失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected boolean doTestConnection() {
        try {
            // 检查bucket是否存在
            return ossClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            log.error("测试OSS连接失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String storagePath) {
        try {
            if (StrUtil.isBlank(storagePath)) {
                storagePath = generateStoragePath(file.getOriginalFilename(), "default", "upload");
            }

            // 设置文件元信息
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            // 上传文件
            PutObjectResult result = ossClient.putObject(bucketName, storagePath, file.getInputStream(), metadata);
            
            // 生成访问URL
            String accessUrl = getFileUrl(storagePath, null);
            
            return FileUploadResult.success(storagePath, accessUrl, file.getSize());
            
        } catch (Exception e) {
            log.error("OSS上传文件失败: {}", e.getMessage(), e);
            return FileUploadResult.failure("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResult uploadFile(InputStream inputStream, String fileName, String storagePath, String contentType) {
        try {
            if (StrUtil.isBlank(storagePath)) {
                storagePath = generateStoragePath(fileName, "default", "upload");
            }

            // 设置文件元信息
            ObjectMetadata metadata = new ObjectMetadata();
            if (StrUtil.isNotBlank(contentType)) {
                metadata.setContentType(contentType);
            }
            
            // 上传文件
            PutObjectResult result = ossClient.putObject(bucketName, storagePath, inputStream, metadata);
            
            // 生成访问URL
            String accessUrl = getFileUrl(storagePath, null);
            
            // 获取文件大小
            Long fileSize = getFileSize(storagePath);
            
            return FileUploadResult.success(storagePath, accessUrl, fileSize);
            
        } catch (Exception e) {
            log.error("OSS上传文件流失败: {}", e.getMessage(), e);
            return FileUploadResult.failure("上传文件流失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return null;
            }
            return ossClient.getObject(bucketName, storagePath).getObjectContent();
        } catch (Exception e) {
            log.error("OSS下载文件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteFile(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return true;
            }
            ossClient.deleteObject(bucketName, storagePath);
            return true;
        } catch (Exception e) {
            log.error("OSS删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsFile(String storagePath) {
        try {
            return ossClient.doesObjectExist(bucketName, storagePath);
        } catch (Exception e) {
            log.error("OSS检查文件存在性失败: {}", e.getMessage(), e);
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
                Date expiration = new Date(System.currentTimeMillis() + expireHours * 3600 * 1000L);
                URL url = ossClient.generatePresignedUrl(bucketName, storagePath, expiration);
                return url.toString();
            } else {
                // 生成永久访问URL (需要bucket为公共读权限)
                return String.format("https://%s.%s/%s", bucketName, endpoint.replace("http://", "").replace("https://", ""), storagePath);
            }
        } catch (Exception e) {
            log.error("OSS生成文件URL失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean copyFile(String sourceStoragePath, String targetStoragePath) {
        try {
            if (!existsFile(sourceStoragePath)) {
                return false;
            }
            
            ossClient.copyObject(bucketName, sourceStoragePath, bucketName, targetStoragePath);
            return true;
            
        } catch (Exception e) {
            log.error("OSS复制文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Long getFileSize(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return null;
            }
            return ossClient.getObjectMetadata(bucketName, storagePath).getContentLength();
        } catch (Exception e) {
            log.error("OSS获取文件大小失败: {}", e.getMessage(), e);
            return null;
        }
    }
} 