package com.originlang.onlysql;

import com.originlang.onlysql.sql.WhereCriteriaBuilder;

import java.util.Collection;

/**
 * 包装 Path + WhereCriteriaBuilder，用于链式 .id.eq(1L).name.gt("a")。
 */
public final class PathWithBuilder {

    private final WhereCriteriaBuilder builder;
    private final SqlPath path;

    public PathWithBuilder(WhereCriteriaBuilder builder, SqlPath path) {
        this.builder = builder;
        this.path = path;
    }

    /** 追加 "column = ?" 并返回 builder。 */
    public WhereCriteriaBuilder eq(Object value) {
        builder.addCondition(path.getColumnName(), value);
        return builder;
    }

    /** 追加 "column > ?" */
    public WhereCriteriaBuilder gt(Object value) {
        builder.addCondition(path.getColumnName(), ">", new Object[]{value});
        return builder;
    }

    /** 追加 "column >= ?" */
    public WhereCriteriaBuilder gte(Object value) {
        builder.addCondition(path.getColumnName(), ">=", new Object[]{value});
        return builder;
    }

    /** 追加 "column < ?" */
    public WhereCriteriaBuilder lt(Object value) {
        builder.addCondition(path.getColumnName(), "<", new Object[]{value});
        return builder;
    }

    /** 追加 "column <= ?" */
    public WhereCriteriaBuilder lte(Object value) {
        builder.addCondition(path.getColumnName(), "<=", new Object[]{value});
        return builder;
    }

    /** 追加 "column != ?" */
    public WhereCriteriaBuilder ne(Object value) {
        builder.addCondition(path.getColumnName(), "!=", new Object[]{value});
        return builder;
    }

    /** 追加 "column IN (?, ?, ...)" */
    public WhereCriteriaBuilder in(Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            builder.addCondition(path.getColumnName(), "IN", values.toArray(new Object[0]));
        }
        return builder;
    }

    /** 追加 "column IS NULL" */
    public WhereCriteriaBuilder isNull() {
        builder.addCondition(path.getColumnName(), "IS NULL", new Object[0]);
        return builder;
    }

    /** 追加 "column LIKE ?"（仅字符串 Path 语义，可链式使用） */
    public WhereCriteriaBuilder like(String pattern) {
        builder.addCondition(path.getColumnName(), "LIKE", new Object[]{pattern});
        return builder;
    }

    /** 追加 "column LIKE prefix%" */
    public WhereCriteriaBuilder startsWith(String prefix) {
        builder.addCondition(path.getColumnName(), "LIKE", new Object[]{prefix == null ? "%" : prefix + "%"});
        return builder;
    }

    /** 追加 "column LIKE %suffix" */
    public WhereCriteriaBuilder endsWith(String suffix) {
        builder.addCondition(path.getColumnName(), "LIKE", new Object[]{suffix == null ? "%" : "%" + suffix});
        return builder;
    }
}
