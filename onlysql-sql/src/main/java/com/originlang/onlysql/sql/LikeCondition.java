package com.originlang.onlysql.sql;

import java.util.List;

/**
 * LIKE 条件：column LIKE ?。
 */
public final class LikeCondition implements WhereCriteria {

    private final String column;
    private final String pattern;

    public LikeCondition(String column, String pattern) {
        this.column = column;
        this.pattern = pattern == null ? "" : pattern;
    }

    @Override
    public String getWhereExpr() {
        return column + " LIKE ?";
    }

    @Override
    public List<Object> getWhereParams() {
        return List.of(pattern);
    }
}
