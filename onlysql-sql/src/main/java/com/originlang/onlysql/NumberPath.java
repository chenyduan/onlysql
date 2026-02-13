package com.originlang.onlysql;

import com.originlang.onlysql.sql.CompareCondition;
import com.originlang.onlysql.sql.InCondition;
import com.originlang.onlysql.sql.IsNullCondition;
import com.originlang.onlysql.sql.SingleCondition;
import com.originlang.onlysql.sql.WhereCriteria;

import java.util.Collection;

public class NumberPath<T> implements SqlPath {

    private final String columnName;
    private final Class<T> type;
    private final Class<?> whereBuilderClass;

    public NumberPath(String columnName, Class<T> type) {
        this(columnName, type, null);
    }

    /** @param whereBuilderClass 用于链式 where；null 时各条件返回 SingleCondition/CompareCondition 等 */
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

    private WhereCriteria chainCond(String op, Object... values) {
        if (whereBuilderClass != null) {
            try {
                return (WhereCriteria) whereBuilderClass.getMethod("ofCond", String.class, String.class, Object[].class)
                        .invoke(null, columnName, op, values);
            } catch (Exception e) {
                throw new RuntimeException("链式 where 需要 " + whereBuilderClass.getSimpleName() + ".ofCond(column, op, values)", e);
            }
        }
        if (">".equals(op)) return new CompareCondition(columnName, ">", values.length > 0 ? values[0] : null);
        if (">=".equals(op)) return new CompareCondition(columnName, ">=", values.length > 0 ? values[0] : null);
        if ("<".equals(op)) return new CompareCondition(columnName, "<", values.length > 0 ? values[0] : null);
        if ("<=".equals(op)) return new CompareCondition(columnName, "<=", values.length > 0 ? values[0] : null);
        if ("!=".equals(op)) return new CompareCondition(columnName, "!=", values.length > 0 ? values[0] : null);
        if ("IN".equals(op)) {
            Collection<?> inValues = values.length > 0 && values[0] instanceof Collection
                    ? (Collection<?>) values[0] : java.util.Arrays.asList(values);
            return new InCondition(columnName, inValues);
        }
        if ("IS NULL".equals(op)) return new IsNullCondition(columnName);
        return new SingleCondition(columnName, values.length > 0 ? values[0] : null);
    }

    /** 等值条件 */
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

    /** 大于 */
    public WhereCriteria gt(Object value) {
        return chainCond(">", value);
    }

    /** 大于等于 */
    public WhereCriteria gte(Object value) {
        return chainCond(">=", value);
    }

    /** 小于 */
    public WhereCriteria lt(Object value) {
        return chainCond("<", value);
    }

    /** 小于等于 */
    public WhereCriteria lte(Object value) {
        return chainCond("<=", value);
    }

    /** 不等于 */
    public WhereCriteria ne(Object value) {
        return chainCond("!=", value);
    }

    /** IN 条件 */
    public WhereCriteria in(Collection<?> values) {
        return chainCond("IN", values == null ? new Object[0] : values.toArray());
    }

    /** IN 条件（可变参数） */
    public WhereCriteria in(Object... values) {
        return chainCond("IN", values == null ? new Object[0] : values);
    }

    /** IS NULL */
    public WhereCriteria isNull() {
        return chainCond("IS NULL");
    }
}
