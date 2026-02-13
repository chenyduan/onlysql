package com.originlang.onlysql.sql;

import java.util.Collection;

/**
 * 可追加条件的 WHERE 构建器，用于链式 path.eq(x).path2.gt(y).path3.isNull()。
 */
public interface WhereCriteriaBuilder extends WhereCriteria {

    /** 追加一条 "column = ?" 条件。 */
    default void addCondition(String column, Object value) {
        addCondition(column, "=", new Object[]{value});
    }

    /**
     * 追加一条条件，支持多种操作符。
     *
     * @param column 列名
     * @param op     操作符："=", "!=", ">", ">=", "<", "<=", "IN", "IS NULL", "LIKE"
     * @param values 参数（IN 时为多值）；IS NULL 时可为空或长度为 0
     */
    void addCondition(String column, String op, Object[] values);
}
