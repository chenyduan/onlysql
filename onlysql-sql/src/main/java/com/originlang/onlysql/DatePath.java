package com.originlang.onlysql;

import com.originlang.onlysql.sql.CompareCondition;
import com.originlang.onlysql.sql.InCondition;
import com.originlang.onlysql.sql.IsNullCondition;
import com.originlang.onlysql.sql.SingleCondition;
import com.originlang.onlysql.sql.WhereCriteria;

import java.util.Collection;

public class DatePath<T> implements SqlPath {

    private final String columnName;
    private final Class<T> type;

    public DatePath(String columnName, Class<T> type) {
        this.columnName = columnName;
        this.type = type;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    public Class<T> getType() {
        return type;
    }

    public WhereCriteria eq(Object value) {
        return new SingleCondition(columnName, value);
    }

    public WhereCriteria gt(Object value) {
        return new CompareCondition(columnName, ">", value);
    }

    public WhereCriteria gte(Object value) {
        return new CompareCondition(columnName, ">=", value);
    }

    public WhereCriteria lt(Object value) {
        return new CompareCondition(columnName, "<", value);
    }

    public WhereCriteria lte(Object value) {
        return new CompareCondition(columnName, "<=", value);
    }

    public WhereCriteria ne(Object value) {
        return new CompareCondition(columnName, "!=", value);
    }

    public WhereCriteria in(Collection<?> values) {
        return new InCondition(columnName, values);
    }

    public WhereCriteria in(Object... values) {
        return new InCondition(columnName, values == null ? java.util.Collections.emptyList() : java.util.Arrays.asList(values));
    }

    public WhereCriteria isNull() {
        return new IsNullCondition(columnName);
    }
}
