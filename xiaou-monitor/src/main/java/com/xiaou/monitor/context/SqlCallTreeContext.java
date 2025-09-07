package com.xiaou.monitor.context;

import com.xiaou.monitor.domain.SqlNode;
import com.xiaou.monitor.domain.SqlStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SQL调用树上下文管理器
 * 线程安全的调用树构建和存储管理
 * 
 * @author xiaou
 */
@Slf4j
@Component
public class SqlCallTreeContext {
    
    /**
     * 线程本地存储 - 调用栈
     */
    private final ThreadLocal<Stack<SqlNode>> callStack = ThreadLocal.withInitial(Stack::new);
    
    /**
     * 线程本地存储 - 根节点列表
     */
    private final ThreadLocal<List<SqlNode>> rootNodes = ThreadLocal.withInitial(ArrayList::new);
    
    /**
     * 线程本地存储 - 会话ID
     */
    private final ThreadLocal<String> sessionId = ThreadLocal.withInitial(() -> 
        Thread.currentThread().getName() + "_" + System.currentTimeMillis());
    
    /**
     * 全局会话存储 - 存储所有完成的调用树
     */
    private final Map<String, List<SqlNode>> globalSessions = new ConcurrentHashMap<>();
    
    /**
     * 全局统计信息
     */
    private final AtomicLong totalSqlCount = new AtomicLong(0);
    private final AtomicLong slowSqlCount = new AtomicLong(0);
    private final AtomicLong errorSqlCount = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    /**
     * 配置参数
     */
    private volatile long slowSqlThreshold = 1000; // 慢SQL阈值（毫秒）
    private volatile boolean traceEnabled = true; // 追踪开关
    private volatile int maxSessionCount = 100; // 最大会话数量
    
    /**
     * 进入SQL调用
     */
    public SqlNode enter(SqlNode sqlNode) {
        if (!traceEnabled) {
            return sqlNode;
        }
        
        try {
            Stack<SqlNode> stack = callStack.get();
            
            // 设置深度
            sqlNode.setDepth(stack.size() + 1);
            
            // 建立父子关系
            if (!stack.isEmpty()) {
                SqlNode parent = stack.peek();
                parent.addChild(sqlNode);
                sqlNode.setParentId(parent.getNodeId());
            } else {
                // 根节点
                rootNodes.get().add(sqlNode);
            }
            
            // 压入栈
            stack.push(sqlNode);
            
            log.debug("进入SQL调用 - 节点ID: {}, 深度: {}, SQL: {}", 
                     sqlNode.getNodeId(), sqlNode.getDepth(), sqlNode.getSql());
            
            return sqlNode;
            
        } catch (Exception e) {
            log.error("进入SQL调用时发生异常", e);
            return sqlNode;
        }
    }
    
    /**
     * 退出SQL调用
     */
    public void exit(SqlNode sqlNode, int affectedRows, String errorMessage) {
        if (!traceEnabled) {
            return;
        }
        
        try {
            // 设置结束时间和结果
            sqlNode.setExecutionResult(LocalDateTime.now(), affectedRows, errorMessage);
            
            // 标记慢SQL
            if (sqlNode.getExecutionTime() > slowSqlThreshold) {
                sqlNode.setSlowSql(true);
                slowSqlCount.incrementAndGet();
            }
            
            // 标记错误SQL
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                errorSqlCount.incrementAndGet();
            }
            
            // 更新统计
            totalSqlCount.incrementAndGet();
            totalExecutionTime.addAndGet(sqlNode.getExecutionTime());
            
            // 弹出栈
            Stack<SqlNode> stack = callStack.get();
            if (!stack.isEmpty()) {
                SqlNode poppedNode = stack.pop();
                
                // 验证节点一致性
                if (!poppedNode.getNodeId().equals(sqlNode.getNodeId())) {
                    log.warn("SQL调用栈不一致 - 期望: {}, 实际: {}", 
                            sqlNode.getNodeId(), poppedNode.getNodeId());
                }
                
                // 如果栈为空，说明调用树完成，保存到全局会话
                if (stack.isEmpty()) {
                    saveSession();
                }
            }
            
            log.debug("退出SQL调用 - 节点ID: {}, 耗时: {}ms, 影响行数: {}", 
                     sqlNode.getNodeId(), sqlNode.getExecutionTime(), affectedRows);
                     
        } catch (Exception e) {
            log.error("退出SQL调用时发生异常", e);
        }
    }
    
    /**
     * 保存会话到全局存储
     */
    private void saveSession() {
        try {
            List<SqlNode> currentRootNodes = rootNodes.get();
            if (!currentRootNodes.isEmpty()) {
                String currentSessionId = sessionId.get();
                
                // 限制会话数量，移除最旧的会话
                if (globalSessions.size() >= maxSessionCount) {
                    String oldestSession = globalSessions.keySet().iterator().next();
                    globalSessions.remove(oldestSession);
                    log.debug("移除最旧会话: {}", oldestSession);
                }
                
                // 保存当前会话
                globalSessions.put(currentSessionId, new ArrayList<>(currentRootNodes));
                log.debug("保存SQL调用树会话 - 会话ID: {}, 根节点数: {}", 
                         currentSessionId, currentRootNodes.size());
                
                // 清理当前线程数据
                currentRootNodes.clear();
                
                // 重新生成会话ID
                sessionId.set(Thread.currentThread().getName() + "_" + System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("保存会话时发生异常", e);
        }
    }
    
    /**
     * 获取当前调用深度
     */
    public int getCurrentDepth() {
        return callStack.get().size();
    }
    
    /**
     * 获取当前线程的根节点
     */
    public List<SqlNode> getCurrentRootNodes() {
        return new ArrayList<>(rootNodes.get());
    }
    
    /**
     * 获取所有会话
     */
    public Map<String, List<SqlNode>> getAllSessions() {
        return new HashMap<>(globalSessions);
    }
    
    /**
     * 获取指定会话
     */
    public List<SqlNode> getSession(String sessionKey) {
        return globalSessions.get(sessionKey);
    }
    
    /**
     * 清理会话数据
     */
    public void clearSessions() {
        globalSessions.clear();
        rootNodes.get().clear();
        callStack.get().clear();
        sessionId.set(Thread.currentThread().getName() + "_" + System.currentTimeMillis());
        log.info("已清理所有SQL调用树会话数据");
    }
    
    /**
     * 清理过期会话（超过指定时间的会话）
     */
    public void clearExpiredSessions(long maxAgeMillis) {
        long currentTime = System.currentTimeMillis();
        List<String> expiredSessions = new ArrayList<>();
        
        for (String sessionKey : globalSessions.keySet()) {
            String[] parts = sessionKey.split("_");
            if (parts.length >= 2) {
                try {
                    long sessionTime = Long.parseLong(parts[parts.length - 1]);
                    if (currentTime - sessionTime > maxAgeMillis) {
                        expiredSessions.add(sessionKey);
                    }
                } catch (NumberFormatException e) {
                    log.warn("无法解析会话时间: {}", sessionKey);
                    expiredSessions.add(sessionKey); // 无法解析的也清理掉
                }
            }
        }
        
        expiredSessions.forEach(globalSessions::remove);
        if (!expiredSessions.isEmpty()) {
            log.info("清理过期会话数量: {}", expiredSessions.size());
        }
    }
    
    /**
     * 获取统计信息
     */
    public SqlStatistics getStatistics() {
        SqlStatistics statistics = new SqlStatistics();
        statistics.setExecutionCount(totalSqlCount.get());
        statistics.setSlowSqlCount(slowSqlCount.get());
        statistics.setErrorCount(errorSqlCount.get());
        statistics.setTotalExecutionTime(totalExecutionTime.get());
        statistics.setAvgExecutionTime(totalSqlCount.get() > 0 ? 
            (double) totalExecutionTime.get() / totalSqlCount.get() : 0.0);
        statistics.setSqlTemplate("总体统计");
        return statistics;
    }
    
    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        totalSqlCount.set(0);
        slowSqlCount.set(0);
        errorSqlCount.set(0);
        totalExecutionTime.set(0);
        log.info("已重置SQL统计信息");
    }
    
    /**
     * 强制完成当前调用树（用于异常情况）
     */
    public void forceCompleteCurrentTree() {
        try {
            Stack<SqlNode> stack = callStack.get();
            while (!stack.isEmpty()) {
                SqlNode node = stack.pop();
                if (node.getEndTime() == null) {
                    node.setExecutionResult(LocalDateTime.now(), 0, "强制完成");
                }
            }
            
            // 保存会话
            if (!rootNodes.get().isEmpty()) {
                saveSession();
            }
            
            log.debug("强制完成当前调用树");
            
        } catch (Exception e) {
            log.error("强制完成调用树时发生异常", e);
        }
    }
    
    // Getter和Setter方法
    public boolean isTraceEnabled() {
        return traceEnabled;
    }
    
    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
        log.info("SQL追踪状态: {}", traceEnabled ? "启用" : "禁用");
    }
    
    public long getSlowSqlThreshold() {
        return slowSqlThreshold;
    }
    
    public void setSlowSqlThreshold(long slowSqlThreshold) {
        this.slowSqlThreshold = slowSqlThreshold;
        log.info("慢SQL阈值设置为: {}ms", slowSqlThreshold);
    }
    
    public int getMaxSessionCount() {
        return maxSessionCount;
    }
    
    public void setMaxSessionCount(int maxSessionCount) {
        this.maxSessionCount = maxSessionCount;
        log.info("最大会话数设置为: {}", maxSessionCount);
    }
} 