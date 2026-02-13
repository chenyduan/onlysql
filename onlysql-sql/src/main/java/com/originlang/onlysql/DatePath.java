package com.originlang.onlysql;

import com.originlang.onlysql.sql.SingleCondition;
import com.originlang.onlysql.sql.WhereCriteria;

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
}
