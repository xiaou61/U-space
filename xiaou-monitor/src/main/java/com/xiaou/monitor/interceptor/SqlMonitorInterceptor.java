package com.xiaou.monitor.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.monitor.context.MonitorContext;
import com.xiaou.monitor.context.MonitorContextHolder;
import com.xiaou.monitor.domain.SqlMonitorLog;
import com.xiaou.monitor.service.SqlMonitorService;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Component;

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
@Component
@RequiredArgsConstructor
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlMonitorInterceptor implements Interceptor {

    private final SqlMonitorService sqlMonitorService;

    /**
     * 慢SQL阈值(毫秒)
     */
    private static final long SLOW_SQL_THRESHOLD = 1000L;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 获取SQL信息
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        String sqlId = mappedStatement.getId();
        
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
            
            // 完善监控日志信息
            monitorLog.setExecutionTime(executionTime)
                     .setAffectedRows(affectedRows)
                     .setSuccess(success)
                     .setErrorMessage(errorMessage)
                     .setSlowSql(executionTime > SLOW_SQL_THRESHOLD)
                     .setCreateTime(LocalDateTime.now());
            
            // 异步保存监控日志
            sqlMonitorService.saveMonitorLogAsync(monitorLog);
            
            // 记录慢SQL警告
            if (executionTime > SLOW_SQL_THRESHOLD) {
                log.warn("检测到慢SQL: {} - 执行时间: {}ms", sqlId, executionTime);
            }
        }
        
        return result;
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
                 .setSqlStatement(formatSql(boundSql.getSql()))
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
            return params.toString();
            
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
} 