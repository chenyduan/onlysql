package com.originlang.onlysql.sql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * INSERT DSL：持有表名与列值，支持链式 set、单条/批量执行。
 * <p>
 * 用法示例：
 * <ul>
 *   <li>单条并返回实体：insert().set("username", "admin").execute(SysUser.class)</li>
 *   <li>仅执行（不取实体）：insert().set("id", 1L).set("username", "x").execute() 或 insert().batch(rows).execute()</li>
 * </ul>
 *
 * @param <T> 实体类型，预留
 */
public class Insert<T> {

    private final String table;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private List<Map<String, Object>> batchRows;
    /** 主键列名；未 set 该列时执行器请求生成键，execute(Class) 返回的实体会带该主键。 */
    private String generatedKeyColumn;

    public Insert(String table) {
        this.table = table;
    }

    /**
     * 指定主键列（自增/序列）。未对该列 set 时，execute(Class) 返回的实体将带生成的主键。
     */
    public Insert<T> generatedKeyColumn(String column) {
        this.generatedKeyColumn = column;
        return this;
    }

    /**
     * 设置单列值，返回 this 以支持链式调用。
     */
    public Insert<T> set(String column, Object value) {
        if (value != null) {
            values.put(column, value);
        }
        return this;
    }

    /**
     * 批量插入：多行同表、同列结构。执行时使用 JDBC addBatch/executeBatch。
     *
     * @param rows 多行数据，每行一个 Map（列名 -> 值）；列以第一行的 key 为准
     * @return this
     */
    public Insert<T> batch(List<Map<String, Object>> rows) {
        if (rows != null && !rows.isEmpty()) {
            this.batchRows = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                this.batchRows.add(row == null ? new LinkedHashMap<>() : new LinkedHashMap<>(row));
            }
        } else {
            this.batchRows = null;
        }
        return this;
    }

    /**
     * 执行 INSERT（不返回值）。单条或批量均可；需要插入后实体时使用 {@link #execute(Class)}。
     */
    public void execute() {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)，或使用 execute(DmlExecutor)");
        }
        executor.executeInsert(this);
    }

    /**
     * 单条 INSERT 并返回当前实体（含生成的主键）；批量插入时执行后返回 null。
     *
     * @param entityClass 实体类，需有无参构造
     * @return 插入后的实体，批量时返回 null
     */
    public <E> E execute(Class<E> entityClass) {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)");
        }
        return executor.executeInsertAndReturn(this, entityClass);
    }

    /**
     * 使用指定执行器执行 INSERT，不依赖上下文。
     */
    public void execute(DmlExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor 不能为 null");
        }
        executor.executeInsert(this);
    }

    public String getTable() {
        return table;
    }

    public Map<String, Object> getValues() {
        return new LinkedHashMap<>(values);
    }

    /**
     * 批量行数据；非空时执行器将走批量 INSERT（addBatch/executeBatch）。
     */
    public List<Map<String, Object>> getBatchRows() {
        return batchRows == null ? null : new ArrayList<>(batchRows);
    }

    /** 主键列名，用于请求生成键并作为返回值。 */
    public String getGeneratedKeyColumn() {
        return generatedKeyColumn;
    }
}
