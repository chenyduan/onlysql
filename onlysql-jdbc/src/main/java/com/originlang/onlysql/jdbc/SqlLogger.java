package com.originlang.onlysql.jdbc;

import java.util.List;

/**
 * SQL 执行日志接口，用于可观测性：打印每条 SQL 及参数。
 * 默认实现见 {@link Slf4jSqlLogger}。
 */
public interface SqlLogger {

    /**
     * 记录一次 SQL 执行（含参数与耗时）。
     *
     * @param sql          最终 SQL
     * @param params       绑定参数（可为空）
     * @param operation    操作类型，如 "INSERT"、"SELECT"、"UPDATE"、"DELETE"
     * @param durationMs   执行耗时（毫秒）
     * @param rowsAffected 影响行数或结果行数，查询时可为 -1 表示不统计
     */
    void log(String sql, List<?> params, String operation, long durationMs, long rowsAffected);

    /**
     * 慢查询告警：当执行时间超过配置的阈值时调用。
     *
     * @param sql         SQL
     * @param params      参数
     * @param operation   操作类型
     * @param durationMs  执行耗时（毫秒）
     */
    default void onSlowQuery(String sql, List<?> params, String operation, long durationMs) {
        // 默认不处理，由实现类决定是否打印
    }
}
