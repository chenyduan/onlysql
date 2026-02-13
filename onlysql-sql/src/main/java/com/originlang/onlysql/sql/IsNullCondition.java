package com.originlang.onlysql.sql;

import java.util.Collections;
import java.util.List;

/**
 * IS NULL 条件。
 */
public final class IsNullCondition implements WhereCriteria {

    private final String column;

    public IsNullCondition(String column) {
        this.column = column;
    }

    @Override
    public String getWhereExpr() {
        return column + " IS NULL";
    }

    @Override
    public List<Object> getWhereParams() {
        return Collections.emptyList();
    }
}
