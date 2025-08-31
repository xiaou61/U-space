package com.xiaou.monitor.context;

/**
 * 监控上下文持有者
 *
 * @author xiaou
 */
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置上下文
     */
    public static void setContext(MonitorContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取上下文
     */
    public static MonitorContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 是否存在上下文
     */
    public static boolean hasContext() {
        return CONTEXT_HOLDER.get() != null;
    }
} 