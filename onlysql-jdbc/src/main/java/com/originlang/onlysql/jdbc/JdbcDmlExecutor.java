package com.originlang.onlysql.jdbc;

import com.originlang.onlysql.sql.Delete;
import com.originlang.onlysql.sql.DmlExecutor;
import com.originlang.onlysql.sql.Insert;
import com.originlang.onlysql.sql.Page;
import com.originlang.onlysql.sql.Select;
import com.originlang.onlysql.sql.Update;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 基于 JDBC DataSource 的 DmlExecutor 实现，支持增删改查（Insert/Update/Delete/Select）。
 * <p>
 * 使用示例：<br>
 * {@code ExecutorContext.setExecutor(new JdbcDmlExecutor(dataSource));}<br>
 * {@code new QSysUser().insert().set("id", 1L).set("username", "admin").execute();}
 */
public class JdbcDmlExecutor implements DmlExecutor {

    private final DataSource dataSource;
    private final ObservabilityConfig observability;
    private static final ThreadLocal<Connection> TRANSACTION_CONNECTION = new ThreadLocal<>();

    public JdbcDmlExecutor(DataSource dataSource) {
        this(dataSource, null);
    }

    /**
     * 使用可观测性配置构造（SQL 日志、慢查询阈值、执行指标）。
     *
     * @param dataSource    数据源
     * @param observability 可观测配置，null 表示不启用
     */
    public JdbcDmlExecutor(DataSource dataSource, ObservabilityConfig observability) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为 null");
        }
        this.dataSource = dataSource;
        this.observability = observability;
    }

    /** 执行块并记录耗时与日志/慢查询/metrics */
    private <T> T runWithObservability(String operation, String sql, List<?> params,
                                       ObservableCall<T> call) {
        long startNs = System.nanoTime();
        try {
            T result = call.run();
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            long rows = call.rowsAffected(result);
            if (observability != null) {
                List<?> p = params != null ? params : List.of();
                if (observability.isLogSql()) {
                    observability.getSqlLogger().log(sql, p, operation, durationMs, rows);
                }
                if (observability.getSlowQueryThresholdMs() > 0
                        && durationMs >= observability.getSlowQueryThresholdMs()) {
                    observability.getSqlLogger().onSlowQuery(sql, p, operation, durationMs);
                }
                observability.getMetrics().recordExecution(operation, durationMs, rows);
            }
            return result;
        } catch (RuntimeException e) {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            if (observability != null && observability.isLogSql()) {
                observability.getSqlLogger().log(sql, params != null ? params : List.of(),
                        operation, durationMs, -1);
            }
            throw e;
        }
    }

    private interface ObservableCall<T> {
        T run();
        long rowsAffected(T result);
    }

    /** 获取连接：若当前在事务中则返回事务连接，否则从 DataSource 获取。 */
    private Connection getConnection() throws SQLException {
        Connection c = TRANSACTION_CONNECTION.get();
        return c != null ? c : dataSource.getConnection();
    }

    /** 若非事务连接则关闭（事务连接由 runInTransaction 统一关闭）。 */
    private void releaseConnection(Connection conn) {
        if (conn == null || conn == TRANSACTION_CONNECTION.get()) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void runInTransaction(Runnable runnable) {
        runInTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public <T> T runInTransaction(Callable<T> callable) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            TRANSACTION_CONNECTION.set(conn);
            T result = callable.call();
            conn.commit();
            return result;
        } catch (Throwable e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        } finally {
            TRANSACTION_CONNECTION.remove();
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public long executeInsert(Insert<?> insert) {
        List<Map<String, Object>> batchRows = insert.getBatchRows();
        if (batchRows != null && !batchRows.isEmpty()) {
            return executeInsertBatch(insert.getTable(), batchRows);
        }
        Map<String, Object> values = insert.getValues();
        if (values.isEmpty()) {
            return 0;
        }
        String table = insert.getTable();
        List<String> columns = new ArrayList<>(values.keySet());
        String sql = buildInsertSql(table, columns);
        List<Object> params = new ArrayList<>(columns.size());
        for (String col : columns) {
            params.add(values.get(col));
        }
        return runWithObservability("INSERT", sql, params, new ObservableCall<>() {
            @Override
            public Long run() {
                return doExecuteInsert(insert, sql, columns, values);
            }
            @Override
            public long rowsAffected(Long result) {
                return result != null ? result : 0;
            }
        });
    }

    private long doExecuteInsert(Insert<?> insert, String sql, List<String> columns,
                                Map<String, Object> values) {
        String generatedKeyColumn = insert.getGeneratedKeyColumn();
        boolean requestGeneratedKeys = generatedKeyColumn != null;
        Connection conn = null;
        try {
            conn = getConnection();
            int psFlags = requestGeneratedKeys ? PreparedStatement.RETURN_GENERATED_KEYS : 0;
            try (PreparedStatement ps = conn.prepareStatement(sql, psFlags)) {
                int i = 1;
                for (String col : columns) {
                    ps.setObject(i++, values.get(col));
                }
                int rows = ps.executeUpdate();
                if (requestGeneratedKeys && rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            return rs.getLong(1);
                        }
                    }
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new RuntimeException("INSERT 执行失败: " + sql, e);
        } finally {
            releaseConnection(conn);
        }
    }

    /** 单次 executeBatch 的行数上限，避免单批过大。 */
    private static final int BATCH_CHUNK_SIZE = 500;

    /**
     * 批量 INSERT：同表、同列，使用 addBatch/executeBatch；超过 {@value #BATCH_CHUNK_SIZE} 行时分批执行。
     *
     * @return 总影响行数
     */
    private long executeInsertBatch(String table, List<Map<String, Object>> batchRows) {
        if (batchRows.isEmpty()) {
            return 0;
        }
        List<String> columns = new ArrayList<>(batchRows.get(0).keySet());
        String sql = buildInsertSql(table, columns);
        List<Object> paramsForLog = List.of("batch size: " + batchRows.size());
        return runWithObservability("INSERT", sql, paramsForLog, new ObservableCall<>() {
            @Override
            public Long run() {
                return doExecuteInsertBatch(table, columns, sql, batchRows);
            }
            @Override
            public long rowsAffected(Long result) {
                return result != null ? result : 0;
            }
        });
    }

    private long doExecuteInsertBatch(String table, List<String> columns, String sql,
                                      List<Map<String, Object>> batchRows) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                long total = 0;
                for (int from = 0; from < batchRows.size(); from += BATCH_CHUNK_SIZE) {
                    int to = Math.min(from + BATCH_CHUNK_SIZE, batchRows.size());
                    for (int i = from; i < to; i++) {
                        Map<String, Object> row = batchRows.get(i);
                        int idx = 1;
                        for (String col : columns) {
                            ps.setObject(idx++, row.get(col));
                        }
                        ps.addBatch();
                    }
                    for (int c : ps.executeBatch()) {
                        total += c;
                    }
                }
                return total;
            }
        } catch (SQLException e) {
            throw new RuntimeException("批量 INSERT 执行失败: " + sql + ", 行数=" + batchRows.size(), e);
        } finally {
            releaseConnection(conn);
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

    @Override
    public long executeUpdate(Update update) {
        Map<String, Object> values = update.getValues();
        if (values.isEmpty()) {
            return 0;
        }
        String table = update.getTable();
        List<String> columns = new ArrayList<>(values.keySet());
        String whereExpr = update.getWhereExpr();
        List<Object> whereParams = update.getWhereParams();
        String sql = buildUpdateSql(table, columns, whereExpr);
        List<Object> params = new ArrayList<>(values.size() + (whereParams != null ? whereParams.size() : 0));
        for (String col : columns) {
            params.add(values.get(col));
        }
        if (whereParams != null) {
            params.addAll(whereParams);
        }
        return runWithObservability("UPDATE", sql, params, new ObservableCall<>() {
            @Override
            public Long run() {
                return doExecuteUpdate(update, sql, columns, values, whereParams);
            }
            @Override
            public long rowsAffected(Long result) {
                return result != null ? result : 0;
            }
        });
    }

    private long doExecuteUpdate(Update update, String sql, List<String> columns,
                                Map<String, Object> values, List<Object> whereParams) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1;
                for (String col : columns) {
                    ps.setObject(i++, values.get(col));
                }
                if (whereParams != null) {
                    for (Object p : whereParams) {
                        ps.setObject(i++, p);
                    }
                }
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("UPDATE 执行失败: " + sql, e);
        } finally {
            releaseConnection(conn);
        }
    }

    @Override
    public long executeDelete(Delete<?> delete) {
        String table = delete.getTable();
        String whereExpr = delete.getWhereExpr();
        List<Object> whereParams = delete.getWhereParams();
        if (whereExpr == null || whereExpr.isEmpty()) {
            throw new IllegalArgumentException("DELETE 必须指定 WHERE 条件，禁止全表删除");
        }
        String sql = "DELETE FROM " + quoteIdentifier(table) + " WHERE " + whereExpr;
        List<Object> params = whereParams != null ? new ArrayList<>(whereParams) : List.of();
        return runWithObservability("DELETE", sql, params, new ObservableCall<>() {
            @Override
            public Long run() {
                return doExecuteDelete(delete, sql, whereParams);
            }
            @Override
            public long rowsAffected(Long result) {
                return result != null ? result : 0;
            }
        });
    }

    private long doExecuteDelete(Delete<?> delete, String sql, List<Object> whereParams) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (whereParams != null) {
                    for (int j = 0; j < whereParams.size(); j++) {
                        ps.setObject(j + 1, whereParams.get(j));
                    }
                }
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DELETE 执行失败: " + sql, e);
        } finally {
            releaseConnection(conn);
        }
    }

    @Override
    public List<Map<String, Object>> executeSelect(Select select) {
        String table = select.getTable();
        String tableAlias = select.getTableAlias();
        List<Select.Join> joins = select.getJoins();
        List<String> columns = select.getColumns();
        String whereExpr = select.getWhereExpr();
        List<Object> whereParams = select.getWhereParams();
        Long limit = select.getLimit();
        Long offset = select.getOffset();
        String sql = buildSelectSql(table, tableAlias, joins, columns, whereExpr, limit, offset);
        List<Object> params = collectSelectParams(select);
        return runWithObservability("SELECT", sql, params, new ObservableCall<>() {
            @Override
            public List<Map<String, Object>> run() {
                return doExecuteSelect(select, sql);
            }
            @Override
            public long rowsAffected(List<Map<String, Object>> result) {
                return result != null ? result.size() : 0;
            }
        });
    }

    private List<Map<String, Object>> doExecuteSelect(Select select, String sql) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindSelectParams(ps, select);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SELECT 执行失败: " + sql, e);
        } finally {
            releaseConnection(conn);
        }
    }

    private static List<Object> collectSelectParams(Select select) {
        List<Object> params = new ArrayList<>();
        for (Select.Join j : select.getJoins()) {
            params.addAll(j.getOnParams());
        }
        if (select.getWhereParams() != null) {
            params.addAll(select.getWhereParams());
        }
        return params;
    }

    @Override
    public <T> List<T> executeSelect(Select select, Class<T> entityClass) {
        List<Map<String, Object>> rows = executeSelect(select);
        return EntityMapper.mapToEntities(rows, entityClass);
    }

    @Override
    public Page<Map<String, Object>> executeSelectPage(Select select, int pageIndex, int pageSize) {
        long total = executeCount(select);
        if (total == 0 || pageSize <= 0) {
            return new Page<>(List.of(), total, pageIndex, pageSize);
        }
        int offset = pageIndex * pageSize;
        List<Map<String, Object>> list = executeSelectWithLimitOffset(select, (long) pageSize, (long) offset);
        return new Page<>(list, total, pageIndex, pageSize);
    }

    @Override
    public <T> Page<T> executeSelectPage(Select select, int pageIndex, int pageSize, Class<T> entityClass) {
        Page<Map<String, Object>> raw = executeSelectPage(select, pageIndex, pageSize);
        List<T> list = EntityMapper.mapToEntities(raw.getList(), entityClass);
        return new Page<>(list, raw.getTotal(), raw.getPageIndex(), raw.getPageSize());
    }

    /** 根据 Select 的 FROM/JOIN/WHERE 执行 COUNT(*) 查询。 */
    private long executeCount(Select select) {
        String sql = buildSelectSql(
                select.getTable(),
                select.getTableAlias(),
                select.getJoins(),
                List.of("COUNT(*)"),
                select.getWhereExpr(),
                null,
                null
        );
        List<Object> params = collectSelectParams(select);
        return runWithObservability("COUNT", sql, params, new ObservableCall<>() {
            @Override
            public Long run() {
                return runCountQuery(sql, select);
            }
            @Override
            public long rowsAffected(Long result) {
                return result != null ? result : 0;
            }
        });
    }

    private long runCountQuery(String sql, Select select) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindSelectParams(ps, select);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("COUNT 执行失败: " + sql, e);
        } finally {
            releaseConnection(conn);
        }
    }

    /** 使用指定 limit/offset 执行 Select（不修改原 Select 的 limit/offset）。 */
    private List<Map<String, Object>> executeSelectWithLimitOffset(Select select, Long limit, Long offset) {
        String sql = buildSelectSql(
                select.getTable(),
                select.getTableAlias(),
                select.getJoins(),
                select.getColumns(),
                select.getWhereExpr(),
                limit,
                offset
        );
        List<Object> params = collectSelectParams(select);
        return runWithObservability("SELECT", sql, params, new ObservableCall<>() {
            @Override
            public List<Map<String, Object>> run() {
                return doExecuteSelectWithLimitOffset(select, sql);
            }
            @Override
            public long rowsAffected(List<Map<String, Object>> result) {
                return result != null ? result.size() : 0;
            }
        });
    }

    private List<Map<String, Object>> doExecuteSelectWithLimitOffset(Select select, String sql) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindSelectParams(ps, select);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SELECT 执行失败: " + sql, e);
        } finally {
            releaseConnection(conn);
        }
    }

    private static void bindSelectParams(PreparedStatement ps, Select select) throws SQLException {
        int i = 1;
        for (Select.Join j : select.getJoins()) {
            for (Object p : j.getOnParams()) {
                ps.setObject(i++, p);
            }
        }
        List<Object> whereParams = select.getWhereParams();
        if (whereParams != null) {
            for (Object p : whereParams) {
                ps.setObject(i++, p);
            }
        }
    }

    private static String buildUpdateSql(String table, List<String> columns, String whereExpr) {
        StringBuilder set = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) set.append(", ");
            set.append(quoteIdentifier(columns.get(i))).append(" = ?");
        }
        String sql = "UPDATE " + quoteIdentifier(table) + " SET " + set;
        if (whereExpr != null && !whereExpr.isEmpty()) {
            sql += " WHERE " + whereExpr;
        }
        return sql;
    }

    private static String buildSelectSql(String table, String tableAlias, List<Select.Join> joins,
                                         List<String> columns, String whereExpr, Long limit, Long offset) {
        String cols = columns.isEmpty()
                ? "*"
                : String.join(", ", columns.stream().map(JdbcDmlExecutor::quoteColumnOrExpr).toList());
        StringBuilder from = new StringBuilder();
        from.append("SELECT ").append(cols).append(" FROM ").append(quoteIdentifier(table));
        if (tableAlias != null && !tableAlias.isEmpty()) {
            from.append(" ").append(quoteIdentifier(tableAlias));
        }
        if (joins != null) {
            for (Select.Join j : joins) {
                from.append(" ").append(j.getType()).append(" ").append(quoteIdentifier(j.getTable()));
                if (j.getAlias() != null && !j.getAlias().isEmpty()) {
                    from.append(" ").append(quoteIdentifier(j.getAlias()));
                }
                if (j.getOnExpr() != null && !j.getOnExpr().isEmpty()) {
                    from.append(" ON ").append(j.getOnExpr());
                }
            }
        }
        if (whereExpr != null && !whereExpr.isEmpty()) {
            from.append(" WHERE ").append(whereExpr);
        }
        if (limit != null && limit > 0) {
            from.append(" LIMIT ").append(limit);
        }
        if (offset != null && offset > 0) {
            from.append(" OFFSET ").append(offset);
        }
        return from.toString();
    }

    private static List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= colCount; i++) {
                String label = meta.getColumnLabel(i);
                row.put(label, rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 列名/表名加引号，避免与数据库保留字冲突；子类可覆盖以适配不同数据库引号规则。
     */
    protected static String quoteIdentifier(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }

    /** 列：简单标识符或 "alias.col" 分段加引号，含空格/括号的表达式原样输出。 */
    private static String quoteColumnOrExpr(String col) {
        if (col == null || col.isEmpty()) return col;
        String s = col.trim();
        if (s.contains("(") || s.contains(" ") || s.contains("\t")) return s;
        if (s.contains(".")) {
            String[] parts = s.split("\\.", -1);
            return String.join(".", java.util.Arrays.stream(parts).map(String::trim).map(JdbcDmlExecutor::quoteIdentifier).toList());
        }
        return quoteIdentifier(s);
    }
}
