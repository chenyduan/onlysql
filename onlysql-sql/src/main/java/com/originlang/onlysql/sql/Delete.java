package com.originlang.onlysql.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DELETE DSL：按表名与 WHERE 条件删除，支持链式 where、execute。
 * <p>
 * 用法示例：
 * <ul>
 *   <li>delete("user").where("id = ?", 100L).execute()</li>
 *   <li>delete("log").where("created_at < ?", someDate).execute()</li>
 * </ul>
 *
 * @param <T> 预留，可用于与实体类型绑定（如 APT 生成）
 */
public class Delete<T> {

    private final String table;
    private String whereExpr;
    private final List<Object> whereParams = new ArrayList<>();

    public Delete(String table) {
        this.table = table;
    }

    /**
     * WHERE 条件，支持 ? 占位符。不调用 where 时由执行器决定是否允许全表删除（通常应禁止）。
     */
    public Delete<T> where(String expression, Object... params) {
        this.whereExpr = expression;
        this.whereParams.clear();
        if (params != null) {
            Collections.addAll(this.whereParams, params);
        }
        return this;
    }

    /** WHERE 条件，使用链式 Path.eq() 或 WhereCriteria。 */
    public Delete<T> where(WhereCriteria criteria) {
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
     * 使用当前上下文中的 {@link DmlExecutor} 执行 DELETE。
     *
     * @return 影响行数
     */
    public long execute() {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)，或使用 execute(DmlExecutor)");
        }
        return executor.executeDelete(this);
    }

    /**
     * 使用指定执行器执行 DELETE。
     */
    public long execute(DmlExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor 不能为 null");
        }
        return executor.executeDelete(this);
    }

    public String getTable() {
        return table;
    }

    public String getWhereExpr() {
        return whereExpr;
    }

    public List<Object> getWhereParams() {
        return new ArrayList<>(whereParams);
    }
}
