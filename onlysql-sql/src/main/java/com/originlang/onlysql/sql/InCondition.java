package com.originlang.onlysql.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * IN 条件：column IN (?, ?, ...)。
 */
public final class InCondition implements WhereCriteria {

    private final String column;
    private final List<Object> values;

    public InCondition(String column, Collection<?> values) {
        this.column = column;
        this.values = values == null || values.isEmpty()
                ? Collections.emptyList()
                : new ArrayList<>(values);
    }

    @Override
    public String getWhereExpr() {
        if (values.isEmpty()) {
            return "1 = 0";
        }
        String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
        return column + " IN (" + placeholders + ")";
    }

    @Override
    public List<Object> getWhereParams() {
        return new ArrayList<>(values);
    }
}
