package com.originlang.onlysql.sql;

/**
 * 执行器上下文：持有当前线程或全局的 {@link DmlExecutor}，供 DSL 的 execute() 使用。
 * <p>
 * 应用启动时设置一次，例如：<br>
 * {@code ExecutorContext.setExecutor(new JdbcDmlExecutor(dataSource));}
 */
public final class ExecutorContext {

    private static final ThreadLocal<DmlExecutor> HOLDER = new ThreadLocal<>();

    private ExecutorContext() {
    }

    /**
     * 设置当前线程使用的执行器。
     */
    public static void setExecutor(DmlExecutor executor) {
        HOLDER.set(executor);
    }

    /**
     * 获取当前线程的执行器；未设置时返回 null。
     */
    public static DmlExecutor getExecutor() {
        return HOLDER.get();
    }

    /**
     * 清除当前线程的执行器。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
