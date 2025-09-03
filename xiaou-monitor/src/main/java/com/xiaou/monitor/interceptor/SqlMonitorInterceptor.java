package com.xiaou.monitor.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.monitor.config.MonitorConfig;
import com.xiaou.monitor.context.MonitorContext;
import com.xiaou.monitor.context.MonitorContextHolder;
import com.xiaou.monitor.domain.SqlMonitorLog;
import com.xiaou.monitor.service.SqlMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * SQL监控拦截器
 * 拦截MyBatis的SQL执行，记录SQL执行信息
 *
 * @author xiaou
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlMonitorInterceptor implements Interceptor, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private SqlMonitorService sqlMonitorService;
    private MonitorConfig monitorConfig;

    /**
     * TEXT字段最大长度限制 (考虑utf8mb4编码，预留安全边界)
     */
    private static final int MAX_TEXT_LENGTH = 16000;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 延迟获取SqlMonitorService，避免循环依赖
     */
    private SqlMonitorService getSqlMonitorService() {
        if (sqlMonitorService == null && applicationContext != null) {
            try {
                sqlMonitorService = applicationContext.getBean(SqlMonitorService.class);
            } catch (Exception e) {
                log.warn("无法获取SqlMonitorService，SQL监控功能将被禁用", e);
            }
        }
        return sqlMonitorService;
    }

    /**
     * 延迟获取MonitorConfig
     */
    private MonitorConfig getMonitorConfig() {
        if (monitorConfig == null && applicationContext != null) {
            try {
                monitorConfig = applicationContext.getBean(MonitorConfig.class);
            } catch (Exception e) {
                log.warn("无法获取MonitorConfig，使用默认配置", e);
                // 创建默认配置
                monitorConfig = createDefaultConfig();
            }
        }
        return monitorConfig;
    }
    
    /**
     * 创建默认监控配置
     */
    private MonitorConfig createDefaultConfig() {
        MonitorConfig defaultConfig = new MonitorConfig(null);
        // 设置默认值会通过@ConfigurationProperties自动处理
        return defaultConfig;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MonitorConfig config = getMonitorConfig();
        
        // 检查是否启用监控
        if (config != null && !config.isEnabled()) {
            return invocation.proceed();
        }
        
        long startTime = System.currentTimeMillis();
        
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 获取SQL信息
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        String sqlId = mappedStatement.getId();
        
        // 过滤不需要监控的SQL
        if (shouldExcludeFromMonitoring(sqlId, config)) {
            if (config != null && config.isDebugEnabled()) {
                log.debug("跳过监控SQL: {}", sqlId);
            }
            return invocation.proceed();
        }
        
        // 构建监控日志对象
        SqlMonitorLog monitorLog = buildMonitorLog(mappedStatement, boundSql, parameter, sqlId);
        monitorLog.setExecuteTime(LocalDateTime.now());
        
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        int affectedRows = 0;
        
        try {
            // 执行SQL
            result = invocation.proceed();
            
            // 计算影响行数
            affectedRows = calculateAffectedRows(result, mappedStatement);
            
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            log.error("SQL执行异常: {}", sqlId, e);
            throw e;
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 获取慢SQL阈值
            long slowSqlThreshold = config != null ? config.getSlowSqlThreshold() : 1000L;
            
            // 完善监控日志信息
            monitorLog.setExecutionTime(executionTime)
                     .setAffectedRows(affectedRows)
                     .setSuccess(success)
                     .setErrorMessage(truncateText(errorMessage, MAX_TEXT_LENGTH))
                     .setSlowSql(executionTime > slowSqlThreshold)
                     .setCreateTime(LocalDateTime.now());
            
            // 异步保存监控日志（延迟获取service，避免循环依赖）
            SqlMonitorService service = getSqlMonitorService();
            if (service != null) {
                try {
                    // 始终使用异步保存
                    service.saveMonitorLogAsync(monitorLog);
                } catch (Exception e) {
                    log.warn("保存SQL监控日志失败", e);
                }
            } else {
                if (config != null && config.isDebugEnabled()) {
                    log.debug("SqlMonitorService未准备就绪，跳过监控日志保存: {}", sqlId);
                }
            }
            
            // 记录慢SQL警告
            if (executionTime > slowSqlThreshold) {
                log.warn("检测到慢SQL: {} - 执行时间: {}ms", sqlId, executionTime);
            }
        }
        
        return result;
    }

    /**
     * 判断是否应该排除监控
     * 根据配置化规则排除系统级查询，如监控查询、日志查询等
     */
    private boolean shouldExcludeFromMonitoring(String sqlId, MonitorConfig config) {
        if (StrUtil.isBlank(sqlId) || config == null) {
            return false;
        }
        
        // 排除配置的mapper包路径
        for (String excludePackage : config.getExcludeMapperPackages()) {
            if (sqlId.contains(excludePackage)) {
                return true;
            }
        }
        
        // 排除特定的查询方法
        for (String methodSuffix : config.getExcludeMethodSuffixes()) {
            if (sqlId.endsWith(methodSuffix)) {
                // 进一步检查是否来自系统管理相关的mapper
                for (String keyword : config.getExcludeMapperKeywords()) {
                    if (sqlId.contains(keyword)) {
                        return true;
                    }
                }
            }
        }
        
        // 可以根据当前请求上下文进一步过滤
        MonitorContext context = MonitorContextHolder.getContext();
        if (context != null) {
            String requestUri = context.getRequestUri();
            String module = context.getModule();
            
            // 排除配置的请求路径
            if (requestUri != null) {
                for (String excludePath : config.getExcludeRequestPaths()) {
                    if (requestUri.startsWith(excludePath)) {
                        return true;
                    }
                }
            }
            
            // 排除配置的模块
            if (module != null && config.getExcludeModules().contains(module)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 构建监控日志对象
     */
    private SqlMonitorLog buildMonitorLog(MappedStatement mappedStatement, BoundSql boundSql, 
                                         Object parameter, String sqlId) {
        SqlMonitorLog monitorLog = new SqlMonitorLog();
        
        // 获取上下文信息
        MonitorContext context = MonitorContextHolder.getContext();
        if (context != null) {
            monitorLog.setTraceId(context.getTraceId())
                     .setUserId(context.getUserId())
                     .setUserType(context.getUserType())
                     .setUsername(context.getUsername())
                     .setRequestIp(context.getRequestIp())
                     .setRequestUri(context.getRequestUri())
                     .setHttpMethod(context.getHttpMethod())
                     .setModule(context.getModule())
                     .setMethod(context.getMethod());
        } else {
            // 如果没有上下文，生成一个跟踪ID
            monitorLog.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        }
        
        // 设置SQL相关信息
        monitorLog.setMapperMethod(sqlId)
                 .setSqlStatement(truncateText(formatSql(boundSql.getSql()), MAX_TEXT_LENGTH))
                 .setSqlType(getSqlType(boundSql.getSql()))
                 .setSqlParams(getParameterString(parameter, boundSql, mappedStatement.getConfiguration()));
        
        return monitorLog;
    }

    /**
     * 获取SQL类型
     */
    private String getSqlType(String sql) {
        if (StrUtil.isBlank(sql)) {
            return "UNKNOWN";
        }
        
        String upperSql = sql.trim().toUpperCase();
        if (upperSql.startsWith("SELECT")) {
            return "SELECT";
        } else if (upperSql.startsWith("INSERT")) {
            return "INSERT";
        } else if (upperSql.startsWith("UPDATE")) {
            return "UPDATE";
        } else if (upperSql.startsWith("DELETE")) {
            return "DELETE";
        } else {
            return "OTHER";
        }
    }

    /**
     * 格式化SQL语句
     */
    private String formatSql(String sql) {
        if (StrUtil.isBlank(sql)) {
            return "";
        }
        
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * 获取SQL参数字符串
     */
    private String getParameterString(Object parameter, BoundSql boundSql, Configuration configuration) {
        try {
            if (parameter == null) {
                return "[]";
            }

            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            if (parameterMappings == null || parameterMappings.isEmpty()) {
                return "[]";
            }

            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            StringBuilder params = new StringBuilder("[");
            
            if (typeHandlerRegistry.hasTypeHandler(parameter.getClass())) {
                params.append(getParameterValue(parameter));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameter);
                for (int i = 0; i < parameterMappings.size(); i++) {
                    ParameterMapping parameterMapping = parameterMappings.get(i);
                    String propertyName = parameterMapping.getProperty();
                    
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        if (i > 0) {
                            params.append(", ");
                        }
                        params.append(getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        if (i > 0) {
                            params.append(", ");
                        }
                        params.append(getParameterValue(obj));
                    }
                }
            }
            
            params.append("]");
            String result = params.toString();
            return truncateText(result, MAX_TEXT_LENGTH);
            
        } catch (Exception e) {
            log.warn("获取SQL参数失败", e);
            return "[参数获取失败]";
        }
    }

    /**
     * 获取参数值的字符串表示
     */
    private String getParameterValue(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "'" + obj + "'";
        } else if (obj instanceof Date) {
            return "'" + obj + "'";
        } else {
            return obj.toString();
        }
    }

    /**
     * 计算影响行数
     */
    private int calculateAffectedRows(Object result, MappedStatement mappedStatement) {
        if (result == null) {
            return 0;
        }
        
        // 对于更新操作，直接返回影响行数
        if (result instanceof Integer) {
            return (Integer) result;
        }
        
        // 对于查询操作，返回结果集大小
        if (result instanceof List) {
            return ((List<?>) result).size();
        }
        
        // 其他情况返回1
        return 1;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件中读取慢SQL阈值等配置
    }
    
    /**
     * 截断过长的文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
} 