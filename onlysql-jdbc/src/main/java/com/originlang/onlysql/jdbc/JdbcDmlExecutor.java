package com.originlang.onlysql.jdbc;

import com.originlang.onlysql.sql.Insert;
import com.originlang.onlysql.sql.DmlExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于 JDBC DataSource 的 DmlExecutor 实现，用于执行 Insert DSL。
 * <p>
 * 使用示例：<br>
 * {@code ExecutorContext.setExecutor(new JdbcDmlExecutor(dataSource));}<br>
 * {@code new QSysUser().insert().set("id", 1L).set("username", "admin").execute();}
 */
public class JdbcDmlExecutor implements DmlExecutor {

    private final DataSource dataSource;

    public JdbcDmlExecutor(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为 null");
        }
        this.dataSource = dataSource;
    }

    @Override
    public long executeInsert(Insert<?> insert) {
        Map<String, Object> values = insert.getValues();
        if (values.isEmpty()) {
            return 0;
        }
        String table = insert.getTable();
        List<String> columns = new ArrayList<>(values.keySet());
        String sql = buildInsertSql(table, columns);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            for (String col : columns) {
                ps.setObject(i++, values.get(col));
            }
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("INSERT 执行失败: " + sql, e);
        }
    }

    private static String buildInsertSql(String table, List<String> columns) {
        StringBuilder cols = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                cols.append(", ");
                placeholders.append(", ");
            }
            cols.append(quoteIdentifier(columns.get(i)));
            placeholders.append("?");
        }
        return "INSERT INTO " + quoteIdentifier(table) + " (" + cols + ") VALUES (" + placeholders + ")";
    }

    /**
     * 列名/表名加引号，避免与数据库保留字冲突；子类可覆盖以适配不同数据库引号规则。
     */
    protected static String quoteIdentifier(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }
}
