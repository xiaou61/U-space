package com.xiaou.monitor.controller;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.Result;
import com.xiaou.monitor.context.SqlCallTreeContext;
import com.xiaou.monitor.domain.SqlNode;
import com.xiaou.monitor.domain.SqlStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SQL调用树控制器
 * 提供SQL调用树的查询、管理和分析接口
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/api/sql-tree")
public class SqlTreeController {
    
    @Autowired
    private SqlCallTreeContext sqlCallTreeContext;
    
    /**
     * 获取当前线程的SQL调用树
     */
    @GetMapping("/current")
    @RequireAdmin
    public Result<List<SqlNode>> getCurrentTree() {
        try {
            List<SqlNode> rootNodes = sqlCallTreeContext.getCurrentRootNodes();
            return Result.success(rootNodes);
        } catch (Exception e) {
            log.error("获取当前SQL调用树失败", e);
            return Result.error("获取当前SQL调用树失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有会话的SQL调用树
     */
    @GetMapping("/sessions")
    @RequireAdmin
    public Result<Map<String, List<SqlNode>>> getAllSessions() {
        try {
            Map<String, List<SqlNode>> sessions = sqlCallTreeContext.getAllSessions();
            return Result.success(sessions);
        } catch (Exception e) {
            log.error("获取所有会话失败", e);
            return Result.error("获取所有会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定会话的SQL调用树
     */
    @GetMapping("/session/{sessionKey}")
    @RequireAdmin
    public Result<List<SqlNode>> getSessionTree(@PathVariable String sessionKey) {
        try {
            List<SqlNode> sessionTree = sqlCallTreeContext.getSession(sessionKey);
            if (sessionTree != null) {
                return Result.success(sessionTree);
            } else {
                return Result.error("会话不存在: " + sessionKey);
            }
        } catch (Exception e) {
            log.error("获取指定会话失败", e);
            return Result.error("获取指定会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理所有调用树数据
     */
    @PostMapping("/clear")
    @RequireAdmin
    public Result<Map<String, Object>> clearAllTrees() {
        try {
            sqlCallTreeContext.clearSessions();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "所有SQL调用树数据已清理");
            response.put("timestamp", LocalDateTime.now());
            return Result.success(response);
        } catch (Exception e) {
            log.error("清理调用树数据失败", e);
            return Result.error("清理调用树数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    @RequireAdmin
    public Result<Map<String, Object>> getStatistics() {
        try {
            SqlStatistics stats = sqlCallTreeContext.getStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("totalSqlCount", stats.getExecutionCount());
            response.put("slowSqlCount", stats.getSlowSqlCount());
            response.put("errorSqlCount", stats.getErrorCount());
            response.put("averageExecutionTime", stats.getAvgExecutionTime());
            response.put("slowSqlThreshold", sqlCallTreeContext.getSlowSqlThreshold());
            response.put("traceEnabled", sqlCallTreeContext.isTraceEnabled());
            response.put("maxSessionCount", sqlCallTreeContext.getMaxSessionCount());
            response.put("currentSessionCount", sqlCallTreeContext.getAllSessions().size());
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 配置追踪参数
     */
    @PostMapping("/config")
    @RequireAdmin
    public Result<Map<String, Object>> updateConfig(@RequestBody Map<String, Object> config) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (config.containsKey("slowSqlThreshold")) {
                long threshold = ((Number) config.get("slowSqlThreshold")).longValue();
                sqlCallTreeContext.setSlowSqlThreshold(threshold);
                response.put("slowSqlThreshold", threshold);
            }
            
            if (config.containsKey("traceEnabled")) {
                boolean enabled = (Boolean) config.get("traceEnabled");
                sqlCallTreeContext.setTraceEnabled(enabled);
                response.put("traceEnabled", enabled);
            }
            
            if (config.containsKey("maxSessionCount")) {
                int maxCount = ((Number) config.get("maxSessionCount")).intValue();
                sqlCallTreeContext.setMaxSessionCount(maxCount);
                response.put("maxSessionCount", maxCount);
            }
            
            response.put("success", true);
            response.put("message", "配置更新成功");
            response.put("timestamp", LocalDateTime.now());
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("更新配置失败", e);
            return Result.error("更新配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析慢SQL
     */
    @GetMapping("/analysis/slow-sql")
    @RequireAdmin
    public Result<List<SqlNode>> getSlowSqlAnalysis() {
        try {
            Map<String, List<SqlNode>> sessions = sqlCallTreeContext.getAllSessions();
            List<SqlNode> slowSqlNodes = new ArrayList<>();
            
            for (List<SqlNode> sessionNodes : sessions.values()) {
                collectSlowSqlNodes(sessionNodes, slowSqlNodes, sqlCallTreeContext.getSlowSqlThreshold());
            }
            
            // 按执行时间降序排序
            slowSqlNodes.sort((a, b) -> Long.compare(b.getExecutionTime(), a.getExecutionTime()));
            
            return Result.success(slowSqlNodes);
        } catch (Exception e) {
            log.error("分析慢SQL失败", e);
            return Result.error("分析慢SQL失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析错误SQL
     */
    @GetMapping("/analysis/error-sql")
    @RequireAdmin
    public Result<List<SqlNode>> getErrorSqlAnalysis() {
        try {
            Map<String, List<SqlNode>> sessions = sqlCallTreeContext.getAllSessions();
            List<SqlNode> errorSqlNodes = new ArrayList<>();
            
            for (List<SqlNode> sessionNodes : sessions.values()) {
                collectErrorSqlNodes(sessionNodes, errorSqlNodes);
            }
            
            // 按时间倒序排序
            errorSqlNodes.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
            
            return Result.success(errorSqlNodes);
        } catch (Exception e) {
            log.error("分析错误SQL失败", e);
            return Result.error("分析错误SQL失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出数据
     */
    @GetMapping("/export")
    @RequireAdmin
    public Result<Map<String, Object>> exportData() {
        try {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("sessions", sqlCallTreeContext.getAllSessions());
            exportData.put("statistics", sqlCallTreeContext.getStatistics());
            exportData.put("config", getConfigInfo());
            exportData.put("exportTime", LocalDateTime.now());
            exportData.put("version", "1.0");
            
            return Result.success(exportData);
        } catch (Exception e) {
            log.error("导出数据失败", e);
            return Result.error("导出数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 系统状态检查
     */
    @GetMapping("/health")
    @RequireAdmin
    public Result<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("traceEnabled", sqlCallTreeContext.isTraceEnabled());
            health.put("slowSqlThreshold", sqlCallTreeContext.getSlowSqlThreshold());
            health.put("maxSessionCount", sqlCallTreeContext.getMaxSessionCount());
            health.put("currentSessionCount", sqlCallTreeContext.getAllSessions().size());
            health.put("timestamp", LocalDateTime.now());
            
            return Result.success(health);
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Result.error("健康检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置统计信息
     */
    @PostMapping("/reset-statistics")
    @RequireAdmin
    public Result<Map<String, Object>> resetStatistics() {
        try {
            sqlCallTreeContext.resetStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "统计信息已重置");
            response.put("timestamp", LocalDateTime.now());
            return Result.success(response);
        } catch (Exception e) {
            log.error("重置统计信息失败", e);
            return Result.error("重置统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理过期会话
     */
    @PostMapping("/cleanup")
    @RequireAdmin
    public Result<Map<String, Object>> cleanupExpiredSessions(@RequestBody Map<String, Object> params) {
        try {
            // 默认清理1小时前的会话
            long maxAgeMillis = 60 * 60 * 1000; // 1小时
            
            if (params.containsKey("maxAgeMinutes")) {
                int maxAgeMinutes = ((Number) params.get("maxAgeMinutes")).intValue();
                maxAgeMillis = maxAgeMinutes * 60 * 1000L;
            }
            
            int beforeCount = sqlCallTreeContext.getAllSessions().size();
            sqlCallTreeContext.clearExpiredSessions(maxAgeMillis);
            int afterCount = sqlCallTreeContext.getAllSessions().size();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "过期会话清理完成");
            response.put("beforeCount", beforeCount);
            response.put("afterCount", afterCount);
            response.put("cleanedCount", beforeCount - afterCount);
            response.put("timestamp", LocalDateTime.now());
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("清理过期会话失败", e);
            return Result.error("清理过期会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 强制完成当前调用树
     */
    @PostMapping("/force-complete")
    @RequireAdmin
    public Result<Map<String, Object>> forceCompleteCurrentTree() {
        try {
            sqlCallTreeContext.forceCompleteCurrentTree();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "当前调用树已强制完成");
            response.put("timestamp", LocalDateTime.now());
            return Result.success(response);
        } catch (Exception e) {
            log.error("强制完成调用树失败", e);
            return Result.error("强制完成调用树失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索所有会话中包含指定条件的SQL节点
     */
    @PostMapping("/sessions/search")
    @RequireAdmin
    public Result<Map<String, List<SqlNode>>> searchAllSessions(@RequestBody Map<String, String> params) {
        try {
            String searchTerm = params.get("searchTerm");
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                // 如果搜索条件为空，返回所有会话
                Map<String, List<SqlNode>> allSessions = sqlCallTreeContext.getAllSessions();
                return Result.success(allSessions);
            }
            
            Map<String, List<SqlNode>> allSessions = sqlCallTreeContext.getAllSessions();
            Map<String, List<SqlNode>> matchingSessions = new HashMap<>();
            
            String lowerSearchTerm = searchTerm.toLowerCase().trim();
            
            // 遍历所有会话，找到包含匹配SQL节点的会话
            for (Map.Entry<String, List<SqlNode>> entry : allSessions.entrySet()) {
                String sessionKey = entry.getKey();
                List<SqlNode> sessionNodes = entry.getValue();
                
                // 检查这个会话是否包含匹配的SQL节点
                if (sessionContainsMatchingNodes(sessionNodes, lowerSearchTerm)) {
                    matchingSessions.put(sessionKey, sessionNodes);
                }
            }
            
            return Result.success(matchingSessions);
            
        } catch (Exception e) {
            log.error("搜索会话失败", e);
            return Result.error("搜索会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 递归收集慢SQL节点
     */
    private void collectSlowSqlNodes(List<SqlNode> nodes, List<SqlNode> slowSqlNodes, long threshold) {
        for (SqlNode node : nodes) {
            if (node.isSlowSql(threshold)) {
                slowSqlNodes.add(node);
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                collectSlowSqlNodes(node.getChildren(), slowSqlNodes, threshold);
            }
        }
    }
    
    /**
     * 递归收集错误SQL节点
     */
    private void collectErrorSqlNodes(List<SqlNode> nodes, List<SqlNode> errorSqlNodes) {
        for (SqlNode node : nodes) {
            if (node.hasError()) {
                errorSqlNodes.add(node);
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                collectErrorSqlNodes(node.getChildren(), errorSqlNodes);
            }
        }
    }
    
    /**
     * 获取配置信息
     */
    private Map<String, Object> getConfigInfo() {
        Map<String, Object> config = new HashMap<>();
        config.put("traceEnabled", sqlCallTreeContext.isTraceEnabled());
        config.put("slowSqlThreshold", sqlCallTreeContext.getSlowSqlThreshold());
        config.put("maxSessionCount", sqlCallTreeContext.getMaxSessionCount());
        return config;
    }
    
    /**
     * 检查会话是否包含匹配的SQL节点（递归检查）
     */
    private boolean sessionContainsMatchingNodes(List<SqlNode> nodes, String searchTerm) {
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        
        for (SqlNode node : nodes) {
            // 检查当前节点是否匹配
            if (matchesSearchTerm(node, searchTerm)) {
                return true;
            }
            
            // 递归检查子节点
            if (sessionContainsMatchingNodes(node.getChildren(), searchTerm)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查节点是否匹配搜索条件
     */
    private boolean matchesSearchTerm(SqlNode node, String searchTerm) {
        if (node == null || searchTerm == null) {
            return false;
        }
        
        return (node.getServiceName() != null && node.getServiceName().toLowerCase().contains(searchTerm)) ||
               (node.getMethodName() != null && node.getMethodName().toLowerCase().contains(searchTerm)) ||
               (node.getUserId() != null && node.getUserId().toString().contains(searchTerm)) ||
               (node.getUsername() != null && node.getUsername().toLowerCase().contains(searchTerm)) ||
               (node.getUserType() != null && node.getUserType().toLowerCase().contains(searchTerm)) ||
               (node.getSql() != null && node.getSql().toLowerCase().contains(searchTerm)) ||
               (node.getSqlType() != null && node.getSqlType().toLowerCase().contains(searchTerm)) ||
               (node.getMapperId() != null && node.getMapperId().toLowerCase().contains(searchTerm));
    }
    

} 