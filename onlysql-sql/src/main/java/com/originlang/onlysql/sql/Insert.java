package com.originlang.onlysql.sql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * INSERT DSL：持有表名与列值，支持链式 set、单条 execute、批量 batch。
 * <p>
 * 用法示例：
 * <ul>
 *   <li>单条：insert().set("id", 1L).set("username", "admin").execute()</li>
 *   <li>批量：insert().batch(listOfRows).execute()，每行一个 Map（列名 -> 值）</li>
 * </ul>
 *
 * @param <T> 实体类型，预留
 */
public class Insert<T> {

    private final String table;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private List<Map<String, Object>> batchRows;
    /** 主键列名；未 set 该列时执行器请求生成键，execute() 返回生成的主键。 */
    private String generatedKeyColumn;

    public Insert(String table) {
        this.table = table;
    }

    /**
     * 指定主键列（自增/序列）。未对该列 set 时，execute() 返回生成的主键；否则返回影响行数。
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
     * 使用当前上下文中的 {@link DmlExecutor} 执行 INSERT。
     *
     * @return 单条且设置了 {@link #generatedKeyColumn} 且未提供主键时返回生成的主键，否则返回影响行数；批量恒返回影响行数
     */
    public long execute() {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)，或使用 execute(DmlExecutor)");
        }
        return executor.executeInsert(this);
    }

    /**
     * 使用指定执行器执行 INSERT，不依赖上下文。
     */
    public long execute(DmlExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor 不能为 null");
        }
        return executor.executeInsert(this);
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
