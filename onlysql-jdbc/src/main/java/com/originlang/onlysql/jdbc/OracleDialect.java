package com.originlang.onlysql.jdbc;

/**
 * Oracle 12c+ 方言：使用 OFFSET x ROWS FETCH NEXT y ROWS ONLY。
 */
public final class OracleDialect implements Dialect {

    public static final OracleDialect INSTANCE = new OracleDialect();

    private OracleDialect() {
    }

    @Override
    public String getLimitOffsetSql(String sql, long limit, long offset) {
        if (limit <= 0 && offset <= 0) {
            return sql;
        }
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
