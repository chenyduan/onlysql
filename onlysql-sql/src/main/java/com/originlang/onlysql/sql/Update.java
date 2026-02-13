package com.originlang.onlysql.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UPDATE DSL：持有表名、SET 列值与 WHERE 条件，支持链式 set、where、execute。
 * <p>
 * 用法示例：
 * <ul>
 *   <li>update("user").set("name", "张三").set("status", 1).where("id = ?", 100L).execute()</li>
 * </ul>
 */
public class Update {

    private final String table;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private String whereExpr;
    private final List<Object> whereParams = new ArrayList<>();
    /** 乐观锁：列名，如 "revision"；非空时 SET 中该列为 version = version + 1，WHERE 中追加 AND version = ? */
    private String versionColumn;
    private Object versionValue;

    public Update(String table) {
        this.table = table;
    }

    /**
     * 设置待更新的列值，返回 this 以支持链式调用。
     */
    public Update set(String column, Object value) {
        values.put(column, value);
        return this;
    }

    /**
     * 乐观锁：指定版本列及当前值。执行时 SET 中该列变为 {@code column = column + 1}，WHERE 中追加 {@code AND column = ?}；
     * 若影响行数为 0 则执行器抛出乐观锁异常。
     *
     * @param column 版本列名，如 "revision"
     * @param currentValue 当前版本值（用于 WHERE）
     */
    public Update versionColumn(String column, Object currentValue) {
        this.versionColumn = column;
        this.versionValue = currentValue;
        return this;
    }

    /**
     * WHERE 条件，支持 ? 占位符。
     */
    public Update where(String expression, Object... params) {
        this.whereExpr = expression;
        this.whereParams.clear();
        if (params != null) {
            Collections.addAll(this.whereParams, params);
        }
        return this;
    }

    /** WHERE 条件，使用链式 Path.eq() 或 WhereCriteria。 */
    public Update where(WhereCriteria criteria) {
        if (criteria == null) {
            this.whereExpr = null;
            this.whereParams.clear();
            return this;
        }
        this.whereExpr = criteria.getWhereExpr();
        this.whereParams.clear();
        List<Object> params = criteria.getWhereParams();
        if (params != null) {
            this.whereParams.addAll(params);
        }
        return this;
    }

    /**
     * 使用当前上下文中的 {@link DmlExecutor} 执行 UPDATE。
     *
     * @return 影响行数
     */
    public long execute() {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)，或使用 execute(DmlExecutor)");
        }
        return executor.executeUpdate(this);
    }

    /**
     * 使用指定执行器执行 UPDATE。
     */
    public long execute(DmlExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor 不能为 null");
        }
        return executor.executeUpdate(this);
    }

    public String getTable() {
        return table;
    }

    public Map<String, Object> getValues() {
        return new LinkedHashMap<>(values);
    }

    public String getWhereExpr() {
        return whereExpr;
    }

    public List<Object> getWhereParams() {
        return new ArrayList<>(whereParams);
    }

    /** 乐观锁版本列名，null 表示未启用。 */
    public String getVersionColumn() {
        return versionColumn;
    }

    /** 乐观锁当前版本值（用于 WHERE）。 */
    public Object getVersionValue() {
        return versionValue;
    }
}
