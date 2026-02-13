package com.originlang.onlysql.sql;

/**
 * 可追加条件的 WHERE 构建器，用于链式 path.eq(x).path2.eq(y)。
 */
public interface WhereCriteriaBuilder extends WhereCriteria {

    /** 追加一条 "column = ?" 条件。 */
    void addCondition(String column, Object value);
}
