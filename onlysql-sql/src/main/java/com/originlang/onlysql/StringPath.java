package com.originlang.onlysql;

import com.originlang.onlysql.sql.SingleCondition;
import com.originlang.onlysql.sql.WhereCriteria;

public class StringPath implements SqlPath {

    private final String columnName;
    private final Class<?> whereBuilderClass;

    public StringPath(String columnName) {
        this(columnName, null);
    }

    /** @param whereBuilderClass 用于链式 where，如 TSysUserWhere.class；null 时 eq 返回 SingleCondition */
    public StringPath(String columnName, Class<?> whereBuilderClass) {
        this.columnName = columnName;
        this.whereBuilderClass = whereBuilderClass;
    }

    @Override
    public String getColumnName() {
        return columnName;
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
