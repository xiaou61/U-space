package com.xiaou.filestorage.controller.pub;

import com.xiaou.common.core.domain.Result;
import com.xiaou.filestorage.dto.FileUploadResult;
import com.xiaou.filestorage.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件操作控制器(公开接口)
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 单文件上传
     *
     * @param file         文件
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @return 上传结果
     */
    @PostMapping("/upload/single")
    public Result<FileUploadResult> uploadSingle(@RequestParam("file") MultipartFile file,
                                                @RequestParam("moduleName") String moduleName,
                                                @RequestParam(value = "businessType", required = false, defaultValue = "default") String businessType) {
        try {
            FileUploadResult result = fileStorageService.uploadSingle(file, moduleName, businessType);
            if (result.isSuccess()) {
                return Result.success(result);
            } else {
                return Result.error(result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量文件上传
     *
     * @param files        文件数组
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @return 上传结果列表
     */
    @PostMapping("/upload/batch")
    public Result<List<FileUploadResult>> uploadBatch(@RequestParam("files") MultipartFile[] files,
                                                     @RequestParam("moduleName") String moduleName,
                                                     @RequestParam(value = "businessType", required = false, defaultValue = "default") String businessType) {
        try {
            List<FileUploadResult> results = fileStorageService.uploadBatch(files, moduleName, businessType);
            return Result.success(results);
        } catch (Exception e) {
            log.error("批量文件上传失败: {}", e.getMessage(), e);
            return Result.error("批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 文件下载
     *
     * @param id 文件ID
     * @return 文件流
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("id") Long id) {
        try {
            // 获取文件信息
            Map<String, Object> fileInfo = fileStorageService.getFileInfo(id);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            // 获取文件流
            InputStream inputStream = fileStorageService.downloadFile(id);
            if (inputStream == null) {
                return ResponseEntity.notFound().build();
            }

            // 设置响应头
            String originalName = (String) fileInfo.get("originalName");
            String contentType = (String) fileInfo.get("contentType");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"));
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(originalName, StandardCharsets.UTF_8));

            return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("文件下载失败: fileId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取文件信息
     *
     * @param id 文件ID
     * @return 文件信息
     */
    @GetMapping("/info/{id}")
    public Result<Map<String, Object>> getFileInfo(@PathVariable("id") Long id) {
        try {
            Map<String, Object> fileInfo = fileStorageService.getFileInfo(id);
            if (fileInfo != null) {
                return Result.success(fileInfo);
            } else {
                return Result.error("文件不存在");
            }
        } catch (Exception e) {
            log.error("获取文件信息失败: fileId={}, error={}", id, e.getMessage(), e);
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件访问URL
     *
     * @param id          文件ID
     * @param expireHours 过期时间(小时)
     * @return 访问URL
     */
    @GetMapping("/url/{id}")
    public Result<String> getFileUrl(@PathVariable("id") Long id,
                                    @RequestParam(value = "expireHours", required = false) Integer expireHours) {
        try {
            String url = fileStorageService.getFileUrl(id, expireHours);
            if (url != null) {
                return Result.success(url);
            } else {
                return Result.error("文件不存在或获取URL失败");
            }
        } catch (Exception e) {
            log.error("获取文件URL失败: fileId={}, error={}", id, e.getMessage(), e);
            return Result.error("获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取文件URL
     *
     * @param fileIds 文件ID列表
     * @return 文件ID与URL映射
     */
    @PostMapping("/urls")
    public Result<Map<Long, String>> getFileUrls(@RequestBody List<Long> fileIds) {
        try {
            Map<Long, String> urls = fileStorageService.getFileUrls(fileIds);
            return Result.success(urls);
        } catch (Exception e) {
            log.error("批量获取文件URL失败: error={}", e.getMessage(), e);
            return Result.error("批量获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件(逻辑删除)
     *
     * @param id         文件ID
     * @param moduleName 模块名称
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteFile(@PathVariable("id") Long id,
                                     @RequestParam("moduleName") String moduleName) {
        try {
            boolean deleted = fileStorageService.deleteFile(id, moduleName);
            if (deleted) {
                return Result.success(true);
            } else {
                return Result.error("文件删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败: fileId={}, error={}", id, e.getMessage(), e);
            return Result.error("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 查询文件列表
     *
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @param pageNum      页码
     * @param pageSize     页大小
     * @return 文件列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> listFiles(@RequestParam("moduleName") String moduleName,
                                                 @RequestParam(value = "businessType", required = false) String businessType,
                                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                 @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = fileStorageService.listFiles(moduleName, businessType, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询文件列表失败: error={}", e.getMessage(), e);
            return Result.error("查询文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param fileIds 文件ID列表
     * @return 文件ID与存在状态映射
     */
    @PostMapping("/exists")
    public Result<Map<Long, Boolean>> checkFilesExists(@RequestBody List<Long> fileIds) {
        try {
            Map<Long, Boolean> existsMap = fileStorageService.checkFilesExists(fileIds);
            return Result.success(existsMap);
        } catch (Exception e) {
            log.error("检查文件存在性失败: error={}", e.getMessage(), e);
            return Result.error("检查文件存在性失败: " + e.getMessage());
        }
    }
} 