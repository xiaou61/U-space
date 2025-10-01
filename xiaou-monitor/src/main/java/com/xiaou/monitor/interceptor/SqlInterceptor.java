package com.xiaou.monitor.interceptor;

import cn.hutool.core.util.ReUtil;
import com.xiaou.common.satoken.StpAdminUtil;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.monitor.context.SqlCallTreeContext;
import com.xiaou.monitor.domain.SqlNode;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * SQL调用树拦截器
 * 拦截MyBatis的SQL执行，构建SQL调用树
 * 
 * @author xiaou
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {
        MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
    }),
    @Signature(type = Executor.class, method = "update", args = {
        MappedStatement.class, Object.class
    })
})
public class SqlInterceptor implements Interceptor, ApplicationContextAware {
    
    @Autowired
    private SqlCallTreeContext sqlCallTreeContext;
    
    private ApplicationContext applicationContext;
    
    /**
     * SQL格式化的Pattern
     */
    private static final Pattern SQL_FORMAT_PATTERN = Pattern.compile("\\s+");
    
    /**
     * 需要排除的SQL Pattern
     */
    private static final List<Pattern> EXCLUDE_PATTERNS = Arrays.asList(
        Pattern.compile(".*sys_.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*information_schema.*", Pattern.CASE_INSENSITIVE)
    );
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 检查是否启用追踪
        if (sqlCallTreeContext == null || !sqlCallTreeContext.isTraceEnabled()) {
            return invocation.proceed();
        }
        
        long startTime = System.currentTimeMillis();
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];
        
        // 获取SQL信息
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String sql = boundSql.getSql();
        String sqlType = mappedStatement.getSqlCommandType().name();
        String mapperId = mappedStatement.getId();
        
        // 过滤不需要监控的SQL
        if (shouldExcludeFromMonitoring(sql, mapperId)) {
            return invocation.proceed();
        }
        
        // 获取调用栈信息
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String serviceName = extractServiceName(stackTrace);
        String methodName = extractMethodName(stackTrace);
        
        // 获取Web请求上下文信息
        RequestContextInfo contextInfo = getRequestContextInfo();
        
        // 创建SQL节点
        SqlNode sqlNode = SqlNode.builder()
                .nodeId(UUID.randomUUID().toString())
                .sql(formatSql(sql))
                .formattedSql(formatSqlForDisplay(sql))
                .sqlType(sqlType)
                .threadName(Thread.currentThread().getName())
                .serviceName(serviceName)
                .methodName(methodName)
                .startTime(LocalDateTime.now())
                .parameters(extractParameters(boundSql, parameter, mappedStatement.getConfiguration()))
                .mapperId(mapperId)
                .depth(sqlCallTreeContext.getCurrentDepth() + 1)
                // 执行相关字段初始值
                .executionTime(0L)
                .slowSql(false)
                .affectedRows(0)
                // 添加Web上下文信息
                .traceId(contextInfo.traceId)
                .userId(contextInfo.userId)
                .userType(contextInfo.userType)
                .username(contextInfo.username)
                .requestIp(contextInfo.requestIp)
                .requestUri(contextInfo.requestUri)
                .httpMethod(contextInfo.httpMethod)
                .build();
        
        // 进入SQL调用
        sqlCallTreeContext.enter(sqlNode);
        
        try {
            // 执行SQL
            Object result = invocation.proceed();
            
            // 计算影响行数
            int affectedRows = calculateAffectedRows(result, sqlType);
            
            // 退出SQL调用（成功）
            sqlCallTreeContext.exit(sqlNode, affectedRows, null);
            
            return result;
            
        } catch (Exception e) {
            // 退出SQL调用（异常）
            sqlCallTreeContext.exit(sqlNode, 0, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 判断是否需要排除监控
     */
    private boolean shouldExcludeFromMonitoring(String sql, String mapperId) {
        // 检查SQL内容
        for (Pattern pattern : EXCLUDE_PATTERNS) {
            if (pattern.matcher(sql).find()) {
                return true;
            }
        }
        
        // 检查Mapper ID
        if (mapperId != null) {
            for (Pattern pattern : EXCLUDE_PATTERNS) {
                if (pattern.matcher(mapperId).find()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 从调用栈中提取Service类名
     */
    private String extractServiceName(StackTraceElement[] stackTrace) {
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            // 跳过框架和拦截器相关的类
            if (isFrameworkClass(className)) {
                continue;
            }
            
            // 查找Service类
            if (className.contains("Service") && !className.contains("$") && !className.contains("Impl")) {
                return className.substring(className.lastIndexOf('.') + 1);
            }
            
            // 查找ServiceImpl类
            if (className.endsWith("ServiceImpl")) {
                return className.substring(className.lastIndexOf('.') + 1);
            }
        }
        
        // 如果没找到Service类，返回调用栈中第一个业务类
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (!isFrameworkClass(className) && className.contains("com.xiaou")) {
                return className.substring(className.lastIndexOf('.') + 1);
            }
        }
        
        return "Unknown";
    }
    
    /**
     * 从调用栈中提取方法名
     */
    private String extractMethodName(StackTraceElement[] stackTrace) {
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            if (!isFrameworkClass(className) && 
                (className.contains("Service") || className.contains("Controller")) &&
                className.contains("com.xiaou")) {
                return element.getMethodName();
            }
        }
        return "unknown";
    }
    
    /**
     * 判断是否为框架类
     */
    private boolean isFrameworkClass(String className) {
        return className.startsWith("org.apache.ibatis") ||
               className.startsWith("org.springframework") ||
               className.startsWith("org.mybatis") ||
               className.startsWith("com.sun") ||
               className.startsWith("sun.") ||
               className.startsWith("java.") ||
               className.startsWith("javax.") ||
               className.contains("cglib") ||
               className.contains("proxy") ||
               className.contains("Interceptor") ||
               className.contains("Aspect");
    }
    
    /**
     * 格式化SQL（去除多余空格）
     */
    private String formatSql(String sql) {
        if (sql == null) {
            return null;
        }
        return SQL_FORMAT_PATTERN.matcher(sql.trim()).replaceAll(" ");
    }
    
    /**
     * 格式化SQL用于显示
     */
    private String formatSqlForDisplay(String sql) {
        if (sql == null) {
            return null;
        }
        
        String formatted = sql.trim();
        
        // 简单的SQL格式化
        formatted = formatted.replaceAll("(?i)\\b(select|from|where|and|or|order by|group by|having|insert|update|delete|set)\\b", "\n$1");
        formatted = formatted.replaceAll("(?i)\\b(inner join|left join|right join|join)\\b", "\n  $1");
        formatted = formatted.replaceAll("\\n\\s*", "\n");
        
        return formatted;
    }
    
    /**
     * 提取SQL参数
     */
    private List<Object> extractParameters(BoundSql boundSql, Object parameterObject, Configuration configuration) {
        List<Object> parameters = new ArrayList<>();
        
        try {
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            
            if (parameterMappings != null && !parameterMappings.isEmpty()) {
                for (ParameterMapping parameterMapping : parameterMappings) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else {
                        MetaObject metaObject = configuration.newMetaObject(parameterObject);
                        value = metaObject.getValue(propertyName);
                    }
                    
                    // 处理参数值，避免过长的字符串
                    if (value instanceof String && ((String) value).length() > 200) {
                        value = ((String) value).substring(0, 200) + "...";
                    }
                    
                    parameters.add(value);
                }
            }
            
            // 限制参数数量
            if (parameters.size() > 50) {
                parameters = parameters.subList(0, 50);
                parameters.add("...(更多参数)");
            }
            
        } catch (Exception e) {
            log.debug("提取SQL参数时发生异常: {}", e.getMessage());
            parameters.add("参数提取失败: " + e.getMessage());
        }
        
        return parameters;
    }
    
    /**
     * 计算影响行数
     */
    private int calculateAffectedRows(Object result, String sqlType) {
        try {
            if ("SELECT".equals(sqlType)) {
                if (result instanceof List) {
                    return ((List<?>) result).size();
                } else if (result != null) {
                    return 1;
                }
                return 0;
            } else if (result instanceof Integer) {
                return (Integer) result;
            } else if (result instanceof Long) {
                return ((Long) result).intValue();
            }
        } catch (Exception e) {
            log.debug("计算影响行数时发生异常: {}", e.getMessage());
        }
        return 0;
    }
    
    @Override
    public Object plugin(Object target) {
        // 只对Executor进行代理
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件中读取属性
        log.info("SqlInterceptor初始化完成");
    }
    
    /**
     * 获取Web请求上下文信息
     */
    private RequestContextInfo getRequestContextInfo() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                
                RequestContextInfo contextInfo = new RequestContextInfo();
                contextInfo.requestIp = getClientIpAddress(request);
                contextInfo.requestUri = request.getRequestURI();
                contextInfo.httpMethod = request.getMethod();
                contextInfo.traceId = getOrGenerateTraceId(request);
                
                // 尝试从Authorization头中获取用户信息
                extractUserInfoFromToken(request, contextInfo);
                
                return contextInfo;
            }
        } catch (Exception e) {
            log.debug("获取请求上下文信息失败: {}", e.getMessage());
        }
        
        // 返回默认值
        RequestContextInfo contextInfo = new RequestContextInfo();
        contextInfo.traceId = UUID.randomUUID().toString().replace("-", "");
        return contextInfo;
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", 
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 多个IP时取第一个
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 获取或生成TraceId
     */
    private String getOrGenerateTraceId(HttpServletRequest request) {
        // 先尝试从请求头获取
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = request.getHeader("Trace-Id");
        }
        
        // 如果没有，则生成一个
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        
        return traceId;
    }
    
    /**
     * 从Token中提取用户信息（使用 Sa-Token）
     */
    private void extractUserInfoFromToken(HttpServletRequest request, RequestContextInfo contextInfo) {
        try {
            // 优先尝试获取用户登录信息
            if (StpUserUtil.isLogin()) {
                contextInfo.userId = StpUserUtil.getLoginIdAsLong();
                contextInfo.userType = "user";
                contextInfo.username = "用户" + contextInfo.userId;
            } 
            // 其次尝试获取管理员登录信息
            else if (StpAdminUtil.isLogin()) {
                contextInfo.userId = StpAdminUtil.getLoginIdAsLong();
                contextInfo.userType = "admin";
                contextInfo.username = "管理员" + contextInfo.userId;
            }
        } catch (Exception e) {
            log.debug("从Sa-Token中提取用户信息失败: {}", e.getMessage());
        }
    }
    
    /**
     * 请求上下文信息封装类
     */
    private static class RequestContextInfo {
        String traceId;
        Long userId;
        String userType;
        String username;
        String requestIp;
        String requestUri;
        String httpMethod;
    }
} 