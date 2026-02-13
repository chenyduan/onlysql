package com.originlang.onlysql;

import com.originlang.onlysql.sql.CompareCondition;
import com.originlang.onlysql.sql.InCondition;
import com.originlang.onlysql.sql.IsNullCondition;
import com.originlang.onlysql.sql.LikeCondition;
import com.originlang.onlysql.sql.SingleCondition;
import com.originlang.onlysql.sql.WhereCriteria;

import java.util.Collection;

public class StringPath implements SqlPath {

    private final String columnName;
    private final Class<?> whereBuilderClass;

    public StringPath(String columnName) {
        this(columnName, null);
    }

    /** @param whereBuilderClass 用于链式 where，如 TSysUserWhere.class；null 时各条件返回 SingleCondition 等 */
    public StringPath(String columnName, Class<?> whereBuilderClass) {
        this.columnName = columnName;
        this.whereBuilderClass = whereBuilderClass;
    }

    @Override
    public String getColumnName() {
        return columnName;
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
        if ("!=".equals(op)) return new CompareCondition(columnName, "!=", values.length > 0 ? values[0] : null);
        if ("IN".equals(op)) return new InCondition(columnName, values.length > 0 && values[0] instanceof Collection ? (Collection<?>) values[0] : java.util.Arrays.asList(values));
        if ("IS NULL".equals(op)) return new IsNullCondition(columnName);
        if ("LIKE".equals(op)) return new LikeCondition(columnName, values.length > 0 ? String.valueOf(values[0]) : "");
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

    /** LIKE pattern（可含 % _） */
    public WhereCriteria like(String pattern) {
        return chainCond("LIKE", pattern);
    }

    /** LIKE prefix% */
    public WhereCriteria startsWith(String prefix) {
        String p = prefix == null ? "%" : prefix + "%";
        return chainCond("LIKE", p);
    }

    /** LIKE %suffix */
    public WhereCriteria endsWith(String suffix) {
        String s = suffix == null ? "%" : "%" + suffix;
        return chainCond("LIKE", s);
    }
}
