package com.xiaou.filestorage.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
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
 * 腾讯云COS存储策略实现
 *
 * @author xiaou
 */
@Slf4j
@Component
public class CosStorageStrategy extends AbstractFileStorageStrategy {

    private COSClient cosClient;
    private String region;
    private String bucketName;
    private String secretId;
    private String secretKey;
    private String domain;

    @Override
    public String getStorageType() {
        return "COS";
    }

    @Override
    protected boolean doInitialize(Map<String, Object> configParams) {
        try {
            this.region = getConfigParam("region");
            this.bucketName = getConfigParam("bucketName");
            this.secretId = getConfigParam("secretId");
            this.secretKey = getConfigParam("secretKey");
            this.domain = getConfigParam("domain");

            if (StrUtil.hasBlank(region, bucketName, secretId, secretKey)) {
                log.error("COS配置参数不完整");
                return false;
            }

            // 创建COS凭证
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            
            // 设置bucket的地域
            Region cosRegion = new Region(region);
            ClientConfig clientConfig = new ClientConfig(cosRegion);
            
            // 创建COS客户端
            this.cosClient = new COSClient(cred, clientConfig);
            
            return true;
        } catch (Exception e) {
            log.error("初始化COS客户端失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected boolean doTestConnection() {
        try {
            // 检查bucket是否存在
            return cosClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            log.error("测试COS连接失败: {}", e.getMessage(), e);
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
            
            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, storagePath, file.getInputStream(), metadata);
            
            // 上传文件
            PutObjectResult result = cosClient.putObject(putObjectRequest);
            
            // 生成访问URL
            String accessUrl = getFileUrl(storagePath, null);
            
            return FileUploadResult.success(storagePath, accessUrl, file.getSize());
            
        } catch (Exception e) {
            log.error("COS上传文件失败: {}", e.getMessage(), e);
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
            
            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, storagePath, inputStream, metadata);
            
            // 上传文件
            PutObjectResult result = cosClient.putObject(putObjectRequest);
            
            // 生成访问URL
            String accessUrl = getFileUrl(storagePath, null);
            
            // 获取文件大小
            Long fileSize = getFileSize(storagePath);
            
            return FileUploadResult.success(storagePath, accessUrl, fileSize);
            
        } catch (Exception e) {
            log.error("COS上传文件流失败: {}", e.getMessage(), e);
            return FileUploadResult.failure("上传文件流失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return null;
            }
            return cosClient.getObject(bucketName, storagePath).getObjectContent();
        } catch (Exception e) {
            log.error("COS下载文件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteFile(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return true;
            }
            cosClient.deleteObject(bucketName, storagePath);
            return true;
        } catch (Exception e) {
            log.error("COS删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsFile(String storagePath) {
        try {
            return cosClient.doesObjectExist(bucketName, storagePath);
        } catch (Exception e) {
            log.error("COS检查文件存在性失败: {}", e.getMessage(), e);
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
                URL url = cosClient.generatePresignedUrl(bucketName, storagePath, expiration);
                return url.toString();
            } else {
                // 生成永久访问URL (需要bucket为公共读权限)
                return String.format("https://%s.cos.%s.myqcloud.com/%s", bucketName, region, storagePath);
            }
        } catch (Exception e) {
            log.error("COS生成文件URL失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean copyFile(String sourceStoragePath, String targetStoragePath) {
        try {
            if (!existsFile(sourceStoragePath)) {
                return false;
            }
            
            String sourceBucketRegion = String.format("%s.cos.%s.myqcloud.com", bucketName, region);
            String sourceKey = String.format("/%s/%s", bucketName, sourceStoragePath);
            
            cosClient.copyObject(sourceBucketRegion, sourceKey, bucketName, targetStoragePath);
            return true;
            
        } catch (Exception e) {
            log.error("COS复制文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Long getFileSize(String storagePath) {
        try {
            if (!existsFile(storagePath)) {
                return null;
            }
            return cosClient.getObjectMetadata(bucketName, storagePath).getContentLength();
        } catch (Exception e) {
            log.error("COS获取文件大小失败: {}", e.getMessage(), e);
            return null;
        }
    }
} 