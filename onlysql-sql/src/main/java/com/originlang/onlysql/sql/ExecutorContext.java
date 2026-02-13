package com.originlang.onlysql.sql;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行器上下文：持有当前线程的 {@link DmlExecutor}，供 DSL 的 execute() 使用。
 * <p>
 * 支持：<br>
 * 1）单执行器：{@code setExecutor(executor)} / {@code getExecutor()}；<br>
 * 2）多数据源/读写分离：{@code setExecutor(key, executor)} 按键注册，{@code setCurrentKey(key)} 切换当前键，{@code getExecutor(key)} 按键获取；<br>
 * 3）临时切换：{@code runWith(executor, runnable)} 在回调内使用指定执行器，回调结束后恢复。
 */
public final class ExecutorContext {

    private static final String DEFAULT_KEY = "default";
    private static final ThreadLocal<DmlExecutor> HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_KEY = new ThreadLocal<>();
    private static final Map<String, DmlExecutor> REGISTRY = new ConcurrentHashMap<>();

    private ExecutorContext() {
    }

    /**
     * 设置当前线程使用的执行器（等价于 setExecutor(DEFAULT_KEY, executor) 并设为当前）。
     */
    public static void setExecutor(DmlExecutor executor) {
        setExecutor(DEFAULT_KEY, executor);
        CURRENT_KEY.set(DEFAULT_KEY);
    }

    /**
     * 按键注册执行器；若该键为当前键则同时设为当前线程执行器。
     *
     * @param key      如 "master"、"slave"
     * @param executor 执行器
     */
    public static void setExecutor(String key, DmlExecutor executor) {
        if (key == null) key = DEFAULT_KEY;
        REGISTRY.put(key, executor);
        String cur = CURRENT_KEY.get();
        if (cur == null || cur.equals(key)) {
            HOLDER.set(executor);
        }
    }

    /**
     * 获取当前线程的执行器；未设置时返回 null。
     */
    public static DmlExecutor getExecutor() {
        DmlExecutor e = HOLDER.get();
        if (e != null) return e;
        String key = CURRENT_KEY.get();
        if (key != null) {
            e = REGISTRY.get(key);
            if (e != null) HOLDER.set(e);
            return e;
        }
        return REGISTRY.get(DEFAULT_KEY);
    }

    /**
     * 按键获取执行器（不改变当前线程执行器）。
     */
    public static DmlExecutor getExecutor(String key) {
        return key == null ? REGISTRY.get(DEFAULT_KEY) : REGISTRY.get(key);
    }

    /**
     * 设置当前线程的“当前键”，getExecutor() 将返回该键对应的执行器。
     */
    public static void setCurrentKey(String key) {
        CURRENT_KEY.set(key);
        DmlExecutor e = key == null ? REGISTRY.get(DEFAULT_KEY) : REGISTRY.get(key);
        HOLDER.set(e);
    }

    /**
     * 在回调内临时使用指定执行器，回调结束后恢复原执行器。
     */
    public static void runWith(DmlExecutor executor, Runnable runnable) {
        DmlExecutor prev = HOLDER.get();
        HOLDER.set(executor);
        try {
            runnable.run();
        } finally {
            HOLDER.set(prev);
        }
    }

    /**
     * 在回调内临时使用指定执行器并返回结果，回调结束后恢复原执行器。
     */
    public static <T> T runWith(DmlExecutor executor, Callable<T> callable) {
        DmlExecutor prev = HOLDER.get();
        HOLDER.set(executor);
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            HOLDER.set(prev);
        }
    }

    /**
     * 清除当前线程的执行器与当前键（不移除 REGISTRY 中的注册）。
     */
    public static void clear() {
        HOLDER.remove();
        CURRENT_KEY.remove();
    }

    /**
     * 使用当前线程的执行器在事务中执行；未设置执行器或执行器不支持事务时抛异常。
     */
    public static void runInTransaction(Runnable runnable) {
        DmlExecutor executor = getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)");
        }
        executor.runInTransaction(runnable);
    }

    /**
     * 使用当前线程的执行器在事务中执行并返回结果。
     */
    public static <T> T runInTransaction(Callable<T> callable) {
        DmlExecutor executor = getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)");
        }
        return executor.runInTransaction(callable);
    }
}
