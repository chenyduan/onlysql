package com.originlang.onlysql;

import com.originlang.onlysql.sql.SingleCondition;
import com.originlang.onlysql.sql.WhereCriteria;

public class NumberPath<T> implements SqlPath {

    private final String columnName;
    private final Class<T> type;
    private final Class<?> whereBuilderClass;

    public NumberPath(String columnName, Class<T> type) {
        this(columnName, type, null);
    }

    /** @param whereBuilderClass 用于链式 where；null 时 eq 返回 SingleCondition */
    public NumberPath(String columnName, Class<T> type, Class<?> whereBuilderClass) {
        this.columnName = columnName;
        this.type = type;
        this.whereBuilderClass = whereBuilderClass;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    public Class<T> getType() {
        return type;
    }

    /** 等值条件；若构造时传了 whereBuilderClass 则返回链式 builder，否则返回 SingleCondition。 */
    public WhereCriteria eq(Object value) {
        if (whereBuilderClass != null) {
            try {
                return (WhereCriteria) whereBuilderClass.getMethod("of", String.class, Object.class).invoke(null, columnName, value);
            } catch (Exception e) {
                throw new RuntimeException("链式 where 需要 " + whereBuilderClass.getSimpleName() + ".of(column, value)", e);
            }
        }
        return new SingleCondition(columnName, value);
    }
}
