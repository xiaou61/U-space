package com.xiaou.monitor.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SQL调用树节点
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlNode {
    
    /**
     * 节点唯一标识
     */
    @Builder.Default
    private String nodeId = UUID.randomUUID().toString();
    
    /**
     * 父节点ID
     */
    private String parentId;
    
    /**
     * SQL语句
     */
    private String sql;
    
    /**
     * 格式化后的SQL
     */
    private String formattedSql;
    
    /**
     * SQL类型 (SELECT/INSERT/UPDATE/DELETE)
     */
    private String sqlType;
    
    /**
     * 调用深度
     */
    private int depth;
    
    /**
     * 线程名称
     */
    private String threadName;
    
    /**
     * Service类名
     */
    private String serviceName;
    
    /**
     * Service方法名
     */
    private String methodName;
    
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    
    /**
     * 执行耗时（毫秒）
     */
    private long executionTime;
    
    /**
     * 是否为慢SQL
     */
    private boolean slowSql;
    
    /**
     * 影响行数
     */
    private int affectedRows;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * SQL参数
     */
    private List<Object> parameters;
    
    /**
     * 子节点
     */
    @Builder.Default
    private List<SqlNode> children = new ArrayList<>();
    
    /**
     * Mapper方法ID
     */
    private String mapperId;
    
    /**
     * 跟踪ID - 用于关联同一次请求的多个SQL
     */
    private String traceId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户类型 (admin/user)
     */
    private String userType;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 请求IP
     */
    private String requestIp;
    
    /**
     * 请求URI
     */
    private String requestUri;
    
    /**
     * HTTP方法
     */
    private String httpMethod;
    
    /**
     * 添加子节点
     */
    public void addChild(SqlNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.setParentId(this.nodeId);
    }
    
    /**
     * 是否为慢SQL（根据阈值判断）
     */
    public boolean isSlowSql(long threshold) {
        return executionTime > threshold;
    }
    
    /**
     * 获取总节点数（包含子节点）
     */
    public int getTotalNodeCount() {
        int count = 1;
        if (children != null) {
            for (SqlNode child : children) {
                count += child.getTotalNodeCount();
            }
        }
        return count;
    }
    
    /**
     * 获取最大深度
     */
    public int getMaxDepth() {
        if (children == null || children.isEmpty()) {
            return depth;
        }
        
        int maxChildDepth = depth;
        for (SqlNode child : children) {
            maxChildDepth = Math.max(maxChildDepth, child.getMaxDepth());
        }
        return maxChildDepth;
    }
    
    /**
     * 获取所有慢SQL节点
     */
    public List<SqlNode> getSlowSqlNodes(long threshold) {
        List<SqlNode> slowNodes = new ArrayList<>();
        
        if (isSlowSql(threshold)) {
            slowNodes.add(this);
        }
        
        if (children != null) {
            for (SqlNode child : children) {
                slowNodes.addAll(child.getSlowSqlNodes(threshold));
            }
        }
        
        return slowNodes;
    }
    
    /**
     * 获取所有错误SQL节点
     */
    public List<SqlNode> getErrorSqlNodes() {
        List<SqlNode> errorNodes = new ArrayList<>();
        
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            errorNodes.add(this);
        }
        
        if (children != null) {
            for (SqlNode child : children) {
                errorNodes.addAll(child.getErrorSqlNodes());
            }
        }
        
        return errorNodes;
    }
    
    /**
     * 计算总执行时间（包含子节点）
     */
    public long getTotalExecutionTime() {
        long totalTime = executionTime;
        
        if (children != null) {
            for (SqlNode child : children) {
                totalTime += child.getTotalExecutionTime();
            }
        }
        
        return totalTime;
    }
    
    /**
     * 是否有错误
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }
    
    /**
     * 设置执行结果
     */
    public void setExecutionResult(LocalDateTime endTime, int affectedRows, String errorMessage) {
        this.endTime = endTime;
        this.affectedRows = affectedRows;
        this.errorMessage = errorMessage;
        
        if (startTime != null && endTime != null) {
            this.executionTime = Duration.between(startTime, endTime).toMillis();
        }
    }
} 