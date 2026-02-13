package com.originlang.onlysql.jdbc;

/**
 * 标准 LIMIT/OFFSET 方言，适用于 H2、PostgreSQL、SQLite 等。
 */
public final class LimitOffsetDialect implements Dialect {

    public static final LimitOffsetDialect INSTANCE = new LimitOffsetDialect();

    private LimitOffsetDialect() {
    }

    @Override
    public String getLimitOffsetSql(String sql, long limit, long offset) {
        if (limit <= 0 && offset <= 0) {
            return sql;
        }
        StringBuilder sb = new StringBuilder(sql);
        if (limit > 0) {
            sb.append(" LIMIT ").append(limit);
        }
        if (offset > 0) {
            sb.append(" OFFSET ").append(offset);
        }
        return sb.toString();
    }
}
