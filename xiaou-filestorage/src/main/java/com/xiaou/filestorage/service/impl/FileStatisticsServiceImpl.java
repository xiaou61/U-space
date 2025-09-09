package com.xiaou.filestorage.service.impl;

import com.xiaou.filestorage.mapper.FileAccessMapper;
import com.xiaou.filestorage.mapper.FileInfoMapper;
import com.xiaou.filestorage.mapper.StorageConfigMapper;
import com.xiaou.filestorage.service.FileStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件统计服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
public class FileStatisticsServiceImpl implements FileStatisticsService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileAccessMapper fileAccessMapper;

    @Autowired
    private StorageConfigMapper storageConfigMapper;

    @Override
    public Map<String, Object> getFileStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 总文件数
            Long totalFiles = fileInfoMapper.countByCondition(null, null, 1);
            statistics.put("totalFiles", totalFiles != null ? totalFiles : 0);

            // 已删除文件数
            Long deletedFiles = fileInfoMapper.countByCondition(null, null, 0);
            statistics.put("deletedFiles", deletedFiles != null ? deletedFiles : 0);

            // 总存储大小
            Long totalSize = fileInfoMapper.sumFileSizeByCondition(null, null, 1);
            statistics.put("totalStorageSize", totalSize != null ? totalSize : 0);

            // 今日新增文件数
            LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            Date today = Date.from(startOfToday.atZone(ZoneId.systemDefault()).toInstant());
            
            Long todayFiles = fileInfoMapper.countByTimeRange(today, new Date());
            statistics.put("todayFiles", todayFiles != null ? todayFiles : 0);

            // 存储配置数量
            Long storageConfigs = (long) storageConfigMapper.selectByCondition(null, 1).size();
            statistics.put("storageConfigs", storageConfigs);

            statistics.put("success", true);
            
        } catch (Exception e) {
            log.error("获取文件统计信息失败: {}", e.getMessage(), e);
            statistics.put("success", false);
            statistics.put("error", e.getMessage());
        }
        
        return statistics;
    }

    @Override
    public Map<String, Object> getStorageUsage() {
        Map<String, Object> usage = new HashMap<>();
        
        try {
            // 各模块存储使用情况
            List<Map<String, Object>> moduleStatsList = fileInfoMapper.countByModule();
            Map<String, Map<String, Object>> moduleUsage = new HashMap<>();
            for (Map<String, Object> stat : moduleStatsList) {
                String moduleName = (String) stat.get("moduleName");
                Map<String, Object> moduleData = new HashMap<>();
                moduleData.put("files", stat.get("fileCount"));
                moduleData.put("size", stat.get("totalSize"));
                moduleUsage.put(moduleName, moduleData);
            }
            usage.put("moduleUsage", moduleUsage);

            // 存储平台使用情况
            List<Map<String, Object>> storageStatsList = fileInfoMapper.countByStorageType();
            Map<String, Map<String, Object>> storageUsage = new HashMap<>();
            for (Map<String, Object> stat : storageStatsList) {
                String storageType = (String) stat.get("storageType");
                Map<String, Object> storageData = new HashMap<>();
                storageData.put("files", stat.get("fileCount"));
                storageData.put("size", stat.get("totalSize"));
                storageUsage.put(storageType, storageData);
            }
            usage.put("storageUsage", storageUsage);

            // 文件类型分布
            List<Map<String, Object>> typeStatsList = fileInfoMapper.countByFileType();
            Map<String, Object> typeDistribution = new HashMap<>();
            for (Map<String, Object> stat : typeStatsList) {
                String fileType = (String) stat.get("fileType");
                typeDistribution.put(fileType, stat.get("fileCount"));
            }
            usage.put("typeDistribution", typeDistribution);

            usage.put("success", true);
            
        } catch (Exception e) {
            log.error("获取存储使用情况失败: {}", e.getMessage(), e);
            usage.put("success", false);
            usage.put("error", e.getMessage());
        }
        
        return usage;
    }

    @Override
    public Map<String, Object> getModuleStatistics() {
        Map<String, Object> moduleStats = new HashMap<>();
        
        try {
            // 各模块文件数量和大小统计
            String[] modules = {"community", "interview", "user", "system"};
            
            for (String module : modules) {
                Long fileCount = fileInfoMapper.countByCondition(module, null, 1);
                Long storageSize = fileInfoMapper.sumFileSizeByCondition(module, null, 1);
                
                Map<String, Object> moduleData = new HashMap<>();
                moduleData.put("fileCount", fileCount != null ? fileCount : 0);
                moduleData.put("storageSize", storageSize != null ? storageSize : 0);
                
                moduleStats.put(module, moduleData);
            }

            moduleStats.put("success", true);
            
        } catch (Exception e) {
            log.error("获取模块统计信息失败: {}", e.getMessage(), e);
            moduleStats.put("success", false);
            moduleStats.put("error", e.getMessage());
        }
        
        return moduleStats;
    }

    @Override
    public Map<String, Object> getFileTypeStatistics() {
        Map<String, Object> typeStats = new HashMap<>();
        
        try {
            List<Map<String, Object>> typeStatsList = fileInfoMapper.countByFileType();
            
            Map<String, Integer> typeCount = new HashMap<>();
            Map<String, Long> typeSize = new HashMap<>();
            
            for (Map<String, Object> stat : typeStatsList) {
                String fileType = (String) stat.get("fileType");
                Integer fileCount = ((Number) stat.get("fileCount")).intValue();
                Long totalSize = ((Number) stat.get("totalSize")).longValue();
                
                typeCount.put(fileType, fileCount);
                typeSize.put(fileType, totalSize);
            }
            
            typeStats.put("typeCount", typeCount);
            typeStats.put("typeSize", typeSize);
            typeStats.put("success", true);
            
        } catch (Exception e) {
            log.error("获取文件类型统计失败: {}", e.getMessage(), e);
            typeStats.put("success", false);
            typeStats.put("error", e.getMessage());
        }
        
        return typeStats;
    }

    @Override
    public Map<String, Object> getUploadTrend(int days) {
        Map<String, Object> trend = new HashMap<>();
        
        try {
            // 计算日期范围
            LocalDateTime endDateTime = LocalDateTime.now();
            LocalDateTime startDateTime = endDateTime.minusDays(days);
            Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
            
            // 获取上传趋势数据
            List<Map<String, Object>> trendData = fileInfoMapper.countUploadTrend(startDate, endDate);
            
            Map<String, Integer> dailyUploads = new HashMap<>();
            Map<String, Long> dailySizes = new HashMap<>();
            
            for (Map<String, Object> data : trendData) {
                String uploadDate = (String) data.get("uploadDate");
                Integer fileCount = ((Number) data.get("fileCount")).intValue();
                Long totalSize = ((Number) data.get("totalSize")).longValue();
                
                dailyUploads.put(uploadDate, fileCount);
                dailySizes.put(uploadDate, totalSize);
            }
            
            trend.put("dailyUploads", dailyUploads);
            trend.put("dailySizes", dailySizes);
            trend.put("period", days);
            trend.put("success", true);
            
        } catch (Exception e) {
            log.error("获取上传趋势统计失败: {}", e.getMessage(), e);
            trend.put("success", false);
            trend.put("error", e.getMessage());
        }
        
        return trend;
    }

    @Override
    public Map<String, Object> getHotFiles(int limit) {
        Map<String, Object> hotFiles = new HashMap<>();
        
        try {
            // 获取热门文件（基于访问次数）
            Date startTime = Date.from(LocalDateTime.now().minusDays(30).atZone(ZoneId.systemDefault()).toInstant());
            Date endTime = new Date();
            
            List<Map<String, Object>> hotFileList = fileAccessMapper.selectHotFiles(startTime, endTime, limit);
            hotFiles.put("hotFileList", hotFileList);
            hotFiles.put("period", "最近30天");
            hotFiles.put("success", true);
            
        } catch (Exception e) {
            log.error("获取热门文件统计失败: {}", e.getMessage(), e);
            hotFiles.put("success", false);
            hotFiles.put("error", e.getMessage());
        }
        
        return hotFiles;
    }
} 