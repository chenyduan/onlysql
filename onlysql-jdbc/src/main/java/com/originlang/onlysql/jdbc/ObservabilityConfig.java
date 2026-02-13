package com.originlang.onlysql.jdbc;

/**
 * 可观测性配置：SQL 日志、慢查询阈值、执行指标。
 * <p>
 * 使用示例：<br>
 * <pre>
 * ObservabilityConfig config = ObservabilityConfig.builder()
 *     .logSql(true)
 *     .slowQueryThresholdMs(500)
 *     .metrics(new SimpleExecutionMetrics())
 *     .build();
 * JdbcDmlExecutor executor = new JdbcDmlExecutor(dataSource, config);
 * </pre>
 */
public final class ObservabilityConfig {

    private final boolean logSql;
    private final SqlLogger sqlLogger;
    private final long slowQueryThresholdMs;
    private final ExecutionMetrics metrics;

    private ObservabilityConfig(boolean logSql, SqlLogger sqlLogger,
                                long slowQueryThresholdMs, ExecutionMetrics metrics) {
        this.logSql = logSql;
        this.sqlLogger = sqlLogger != null ? sqlLogger : new Slf4jSqlLogger();
        this.slowQueryThresholdMs = slowQueryThresholdMs;
        this.metrics = metrics != null ? metrics : ExecutionMetrics.NO_OP;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isLogSql() {
        return logSql;
    }

    public SqlLogger getSqlLogger() {
        return sqlLogger;
    }

    /** 慢查询阈值（毫秒），0 表示不告警 */
    public long getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }

    public ExecutionMetrics getMetrics() {
        return metrics;
    }

    public static final class Builder {
        private boolean logSql;
        private SqlLogger sqlLogger;
        private long slowQueryThresholdMs;
        private ExecutionMetrics metrics;

        private Builder() {}

        /** 是否打印每条 SQL 及参数（使用 Slf4j，需 DEBUG 级别） */
        public Builder logSql(boolean logSql) {
            this.logSql = logSql;
            return this;
        }

        /** 自定义 SQL 日志实现；不设置且 logSql=true 时使用 {@link Slf4jSqlLogger} */
        public Builder sqlLogger(SqlLogger sqlLogger) {
            this.sqlLogger = sqlLogger;
            return this;
        }

        /** 慢查询阈值（毫秒），超过则调用 {@link SqlLogger#onSlowQuery}；0 表示不告警 */
        public Builder slowQueryThresholdMs(long slowQueryThresholdMs) {
            this.slowQueryThresholdMs = slowQueryThresholdMs;
            return this;
        }

        /** 执行指标收集器；不设置则不收集 */
        public Builder metrics(ExecutionMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public ObservabilityConfig build() {
            return new ObservabilityConfig(logSql, sqlLogger, slowQueryThresholdMs, metrics);
        }
    }
}
