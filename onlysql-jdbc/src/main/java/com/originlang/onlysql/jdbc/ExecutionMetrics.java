package com.originlang.onlysql.jdbc;

/**
 * SQL 执行指标接口：记录执行次数与耗时，便于监控。
 */
public interface ExecutionMetrics {

    /**
     * 记录一次执行。
     *
     * @param operation    操作类型，如 "INSERT"、"SELECT"、"UPDATE"、"DELETE"
     * @param durationMs   执行耗时（毫秒）
     * @param rowsAffected 影响行数或结果行数
     */
    void recordExecution(String operation, long durationMs, long rowsAffected);

    /**
     * 无操作实现，用于关闭 metrics 时使用。
     */
    ExecutionMetrics NO_OP = (operation, durationMs, rowsAffected) -> {};
}
