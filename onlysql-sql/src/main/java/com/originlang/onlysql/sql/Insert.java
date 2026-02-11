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
 *   <li>通用：insert().set("id", 1L).set("username", "admin").execute()</li>
 *   <li>若 APT 生成带 fluent 方法的子类/包装：insert().id(1L).username("admin").execute()</li>
 *   <li>批量：insert().batch(listOfRecords)</li>
 * </ul>
 *
 * @param <T> 实体类型，用于 batch(List&lt;T&gt;) 等
 */
public class Insert<T> {

    private final String table;
    private final Map<String, Object> values = new LinkedHashMap<>();

    public Insert(String table) {
        this.table = table;
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
     * 使用当前上下文中的 {@link DmlExecutor} 执行 INSERT。
     * 需先通过 {@link ExecutorContext#setExecutor(DmlExecutor)} 设置执行器（如 onlysql-jdbc 的 JdbcDmlExecutor）。
     *
     * @return 影响行数或生成的主键（由执行器实现决定）
     * @throws IllegalStateException 未设置执行器时
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

    /**
     * 批量插入：多行同表插入。
     */
    public Insert<T> batch(List<T> rows) {
        // TODO: 根据 T 与 R 实体或反射取字段，生成批量 INSERT
        return this;
    }

    public String getTable() {
        return table;
    }

    public Map<String, Object> getValues() {
        return new LinkedHashMap<>(values);
    }
}
