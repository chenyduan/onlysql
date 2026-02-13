package com.originlang.onlysql.jdbc;

/**
 * MySQL / MariaDB 方言。现代版本支持 LIMIT limit OFFSET offset，与标准一致。
 */
public final class MySQLDialect implements Dialect {

    public static final MySQLDialect INSTANCE = new MySQLDialect();

    private MySQLDialect() {
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
