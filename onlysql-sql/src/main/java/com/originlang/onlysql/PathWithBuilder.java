package com.originlang.onlysql;

import com.originlang.onlysql.sql.WhereCriteriaBuilder;

/**
 * 包装 Path + WhereCriteriaBuilder，用于链式 .name.eq("admin")。
 */
public final class PathWithBuilder {

    private final WhereCriteriaBuilder builder;
    private final SqlPath path;

    public PathWithBuilder(WhereCriteriaBuilder builder, SqlPath path) {
        this.builder = builder;
        this.path = path;
    }

    /** 追加 "path列 = value" 并返回 builder，可继续链式。 */
    public WhereCriteriaBuilder eq(Object value) {
        builder.addCondition(path.getColumnName(), value);
        return builder;
    }
}
