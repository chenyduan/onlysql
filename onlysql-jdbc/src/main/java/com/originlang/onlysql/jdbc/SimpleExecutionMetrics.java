package com.originlang.onlysql.jdbc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 内存版执行指标：按操作类型统计执行次数与总耗时，便于监控与测试。
 */
public class SimpleExecutionMetrics implements ExecutionMetrics {

    private final Map<String, LongAdder> countByOperation = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> totalMsByOperation = new ConcurrentHashMap<>();

    @Override
    public void recordExecution(String operation, long durationMs, long rowsAffected) {
        countByOperation.computeIfAbsent(operation, k -> new LongAdder()).add(1);
        totalMsByOperation.computeIfAbsent(operation, k -> new LongAdder()).add(durationMs);
    }

    /** 某类操作的执行次数 */
    public long getCount(String operation) {
        LongAdder a = countByOperation.get(operation);
        return a == null ? 0 : a.sum();
    }

    /** 某类操作的总耗时（毫秒） */
    public long getTotalDurationMs(String operation) {
        LongAdder a = totalMsByOperation.get(operation);
        return a == null ? 0 : a.sum();
    }

    /** 总执行次数（所有类型） */
    public long getTotalCount() {
        return countByOperation.values().stream().mapToLong(LongAdder::sum).sum();
    }

    /** 总耗时（毫秒） */
    public long getTotalDurationMs() {
        return totalMsByOperation.values().stream().mapToLong(LongAdder::sum).sum();
    }

    /** 清空统计（便于测试或周期上报后重置） */
    public void reset() {
        countByOperation.clear();
        totalMsByOperation.clear();
    }
}
