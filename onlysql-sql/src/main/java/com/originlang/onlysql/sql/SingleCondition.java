package com.originlang.onlysql.sql;

import java.util.Collections;
import java.util.List;

/**
 * 单列等值条件，用于 path.eq(value) 且无链式时。
 */
public final class SingleCondition implements WhereCriteria {

    private final String column;
    private final Object value;

    public SingleCondition(String column, Object value) {
        this.column = column;
        this.value = value;
    }

    @Override
    public String getWhereExpr() {
        return column + " = ?";
    }

    @Override
    public List<Object> getWhereParams() {
        return value == null ? Collections.emptyList() : List.of(value);
    }
}
