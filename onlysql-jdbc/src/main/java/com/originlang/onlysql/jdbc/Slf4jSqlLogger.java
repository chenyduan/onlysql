package com.originlang.onlysql.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 基于 Slf4j 的 SQL 日志实现：打印每条 SQL、参数及耗时。
 * 日志名为 {@code com.originlang.onlysql.jdbc.sql}，可通过 logback/log4j 配置级别。
 */
public class Slf4jSqlLogger implements SqlLogger {

    private static final Logger LOG = LoggerFactory.getLogger("com.originlang.onlysql.jdbc.sql");

    private final boolean logParams;

    public Slf4jSqlLogger() {
        this(true);
    }

    /**
     * @param logParams 是否在日志中输出绑定参数
     */
    public Slf4jSqlLogger(boolean logParams) {
        this.logParams = logParams;
    }

    @Override
    public void log(String sql, List<?> params, String operation, long durationMs, long rowsAffected) {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        StringBuilder msg = new StringBuilder();
        msg.append('[').append(operation).append("] ").append(durationMs).append("ms");
        if (rowsAffected >= 0) {
            msg.append(", rows=").append(rowsAffected);
        }
        msg.append(" | ").append(sql);
        if (logParams && params != null && !params.isEmpty()) {
            msg.append(" | params=").append(params);
        }
        LOG.debug(msg.toString());
    }

    @Override
    public void onSlowQuery(String sql, List<?> params, String operation, long durationMs) {
        StringBuilder msg = new StringBuilder();
        msg.append("[SLOW] ").append(operation).append(" ").append(durationMs).append("ms | ").append(sql);
        if (logParams && params != null && !params.isEmpty()) {
            msg.append(" | params=").append(params);
        }
        LOG.warn(msg.toString());
    }
}
