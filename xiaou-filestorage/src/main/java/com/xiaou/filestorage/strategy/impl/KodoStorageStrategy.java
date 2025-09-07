package com.xiaou.filestorage.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.xiaou.filestorage.dto.FileUploadResult;
import com.xiaou.filestorage.strategy.AbstractFileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 七牛云Kodo存储策略实现
 *
 * @author xiaou
 */
@Slf4j
@Component
public class KodoStorageStrategy extends AbstractFileStorageStrategy {

    private Auth auth;
    private UploadManager uploadManager;
    private BucketManager bucketManager;
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String domain;
    private Region region;

    @Override
    public String getStorageType() {
        return "KODO";
    }

    @Override
    protected boolean doInitialize(Map<String, Object> configParams) {
        try {
            this.accessKey = getConfigParam("accessKey");
            this.secretKey = getConfigParam("secretKey");
            this.bucketName = getConfigParam("bucketName");
            this.domain = getConfigParam("domain");
            String regionStr = getConfigParam("region", "z0"); // 默认华东

            if (StrUtil.hasBlank(accessKey, secretKey, bucketName)) {
                log.error("Kodo配置参数不完整");
                return false;
            }

            // 创建认证对象
            this.auth = Auth.create(accessKey, secretKey);
            
            // 设置区域
            this.region = getRegionByString(regionStr);
            Configuration cfg = new Configuration(region);
            cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
            
            // 创建上传管理器和存储管理器
            this.uploadManager = new UploadManager(cfg);
            this.bucketManager = new BucketManager(auth, cfg);
            
            return true;
        } catch (Exception e) {
            log.error("初始化Kodo客户端失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected boolean doTestConnection() {
        try {
            // 尝试获取空间的域名列表来测试连接
            String[] domains = bucketManager.domainList(bucketName);
            return domains != null;
        } catch (Exception e) {
            log.error("测试Kodo连接失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String storagePath) {
        try {
            if (StrUtil.isBlank(storagePath)) {
                storagePath = generateStoragePath(file.getOriginalFilename(), "default", "upload");
            }

            // 生成上传凭证
            String upToken = auth.uploadToken(bucketName);
            
            // 上传文件
            Response response = uploadManager.put(file.getInputStream(), storagePath, upToken, null, null);
            
            if (response.isOK()) {
                // 解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                
                // 生成访问URL
                String accessUrl = getFileUrl(storagePath, null);
                
                return FileUploadResult.success(storagePath, accessUrl, file.getSize());
            } else {
                return FileUploadResult.failure("上传失败: " + response.bodyString());
            }
            
        } catch (Exception e) {
            log.error("Kodo上传文件失败: {}", e.getMessage(), e);
            return FileUploadResult.failure("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResult uploadFile(InputStream inputStream, String fileName, String storagePath, String contentType) {
        try {
            if (StrUtil.isBlank(storagePath)) {
                storagePath = generateStoragePath(fileName, "default", "upload");
            }

            // 生成上传凭证
            String upToken = auth.uploadToken(bucketName);
            
            // 上传文件
            Response response = uploadManager.put(inputStream, storagePath, upToken, null, contentType);
            
            if (response.isOK()) {
                // 解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                
                // 生成访问URL
                String accessUrl = getFileUrl(storagePath, null);
                
                // 获取文件大小
                Long fileSize = getFileSize(storagePath);
                
                return FileUploadResult.success(storagePath, accessUrl, fileSize);
            } else {
                return FileUploadResult.failure("上传失败: " + response.bodyString());
            }
            
        } catch (Exception e) {
            log.error("Kodo上传文件流失败: {}", e.getMessage(), e);
            return FileUploadResult.failure("上传文件流失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String storagePath) {
        try {
            // 七牛云不直接支持下载流，需要通过URL下载
            String downloadUrl = getFileUrl(storagePath, 1); // 1小时有效期
            if (downloadUrl == null) {
                return null;
            }
            
            // 这里需要通过HTTP客户端下载，简化实现
            java.net.URL url = new java.net.URL(downloadUrl);
            return url.openStream();
            
        } catch (Exception e) {
            log.error("Kodo下载文件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteFile(String storagePath) {
        try {
            Response response = bucketManager.delete(bucketName, storagePath);
            return response.isOK();
        } catch (QiniuException e) {
            if (e.response.statusCode == 612) {
                // 文件不存在，视为删除成功
                return true;
            }
            log.error("Kodo删除文件失败: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Kodo删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsFile(String storagePath) {
        try {
            bucketManager.stat(bucketName, storagePath);
            return true;
        } catch (QiniuException e) {
            if (e.response.statusCode == 612) {
                // 文件不存在
                return false;
            }
            log.error("Kodo检查文件存在性失败: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Kodo检查文件存在性失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String storagePath, Integer expireHours) {
        try {
            if (StrUtil.isNotBlank(domain)) {
                String baseUrl = domain + "/" + storagePath;
                
                if (expireHours != null && expireHours > 0) {
                    // 生成私有空间的下载链接
                    long expireInSeconds = System.currentTimeMillis() / 1000 + expireHours * 3600;
                    return auth.privateDownloadUrl(baseUrl, expireInSeconds);
                } else {
                    // 公开空间直接返回URL
                    return baseUrl;
                }
            } else {
                log.error("Kodo未配置访问域名");
                return null;
            }
        } catch (Exception e) {
            log.error("Kodo生成文件URL失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean copyFile(String sourceStoragePath, String targetStoragePath) {
        try {
            Response response = bucketManager.copy(bucketName, sourceStoragePath, bucketName, targetStoragePath);
            return response.isOK();
        } catch (Exception e) {
            log.error("Kodo复制文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Long getFileSize(String storagePath) {
        try {
            com.qiniu.storage.model.FileInfo fileInfo = bucketManager.stat(bucketName, storagePath);
            return fileInfo.fsize;
        } catch (Exception e) {
            log.error("Kodo获取文件大小失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据字符串获取区域
     */
    private Region getRegionByString(String regionStr) {
        switch (regionStr.toLowerCase()) {
            case "z0":
            case "huadong":
                return Region.region0();
            case "z1":
            case "huabei":
                return Region.region1();
            case "z2":
            case "huanan":
                return Region.region2();
            case "na0":
            case "beimei":
                return Region.regionNa0();
            case "as0":
            case "xinjiapo":
                return Region.regionAs0();
            default:
                return Region.region0(); // 默认华东
        }
    }
} 