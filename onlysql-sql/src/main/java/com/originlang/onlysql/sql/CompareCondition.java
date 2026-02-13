package com.originlang.onlysql.sql;

import java.util.Collections;
import java.util.List;

/**
 * 单列比较条件：=, !=, &gt;, &gt;=, &lt;, &lt;=。
 */
public final class CompareCondition implements WhereCriteria {

    private final String column;
    private final String op;
    private final Object value;

    public CompareCondition(String column, String op, Object value) {
        this.column = column;
        this.op = op;
        this.value = value;
    }

    @Override
    public String getWhereExpr() {
        return column + " " + op + " ?";
    }

    @Override
    public List<Object> getWhereParams() {
        return value == null ? Collections.emptyList() : List.of(value);
    }
}
