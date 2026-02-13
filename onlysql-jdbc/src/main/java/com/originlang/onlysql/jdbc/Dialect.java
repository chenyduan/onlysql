package com.originlang.onlysql.jdbc;

/**
 * 数据库方言：主要提供分页 SQL 的改写，以兼容不同数据库的 LIMIT/OFFSET 语法。
 */
public interface Dialect {

    /**
     * 在完整 SELECT 语句后追加分页子句，或改写为方言语法。
     *
     * @param sql    不含分页的完整 SQL（如 SELECT ... FROM ... WHERE ...）
     * @param limit  取出行数，0 表示不限制
     * @param offset 跳过行数，0 表示不跳过
     * @return 带分页的 SQL；当 limit 与 offset 均为 0 时可直接返回原 sql
     */
    String getLimitOffsetSql(String sql, long limit, long offset);

    /**
     * 根据 JDBC URL 推断方言；无法识别时返回默认（LIMIT/OFFSET）。
     *
     * @param jdbcUrl 如 jdbc:h2:mem:test、jdbc:mysql://localhost/db、jdbc:postgresql:///db
     * @return 对应 Dialect，不会为 null
     */
    static Dialect fromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return LimitOffsetDialect.INSTANCE;
        }
        String url = jdbcUrl.toLowerCase();
        if (url.contains(":h2:")) {
            return LimitOffsetDialect.INSTANCE;
        }
        if (url.contains(":mysql:") || url.contains(":mariadb:")) {
            return MySQLDialect.INSTANCE;
        }
        if (url.contains(":postgresql:")) {
            return LimitOffsetDialect.INSTANCE;
        }
        if (url.contains(":sqlite:")) {
            return LimitOffsetDialect.INSTANCE;
        }
        if (url.contains(":oracle:")) {
            return OracleDialect.INSTANCE;
        }
        if (url.contains(":sqlserver:") || url.contains(":microsoft:") || url.contains(":jtds:")) {
            return SqlServerDialect.INSTANCE;
        }
        return LimitOffsetDialect.INSTANCE;
    }
}
