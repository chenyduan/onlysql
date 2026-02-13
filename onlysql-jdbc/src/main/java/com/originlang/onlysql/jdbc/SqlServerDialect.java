package com.originlang.onlysql.jdbc;

/**
 * SQL Server 2012+ 方言：使用 OFFSET x ROWS FETCH NEXT y ROWS ONLY。
 */
public final class SqlServerDialect implements Dialect {

    public static final SqlServerDialect INSTANCE = new SqlServerDialect();

    private SqlServerDialect() {
    }

    @Override
    public String getLimitOffsetSql(String sql, long limit, long offset) {
        if (limit <= 0 && offset <= 0) {
            return sql;
        }
        // SQL Server 要求 ORDER BY 与 OFFSET/FETCH 一起使用；若原 SQL 无 ORDER BY 则可能报错，由调用方保证
        StringBuilder sb = new StringBuilder(sql);
        if (offset > 0) {
            sb.append(" OFFSET ").append(offset).append(" ROWS");
        }
        if (limit > 0) {
            sb.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY");
        }
        return sb.toString();
    }
}
