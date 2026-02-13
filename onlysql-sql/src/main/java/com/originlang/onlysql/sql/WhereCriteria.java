package com.originlang.onlysql.sql;

import java.util.List;

/**
 * WHERE 条件抽象：可由字符串表达式或链式 Path.eq() 构建，供 Select/Update/Delete 使用。
 * 组合条件使用 {@link #and(WhereCriteria...)} / {@link #or(WhereCriteria...)}。
 */
public interface WhereCriteria {

    /** SQL 条件表达式，使用 ? 占位符，如 "id = ? AND name = ?"。 */
    String getWhereExpr();

    /** 与 getWhereExpr() 中 ? 顺序一致的参数列表。 */
    List<Object> getWhereParams();

    /** (c1) AND (c2) AND ...，便于组合链式与手写条件。 */
    static WhereCriteria and(WhereCriteria... criteria) {
        return CompositeCondition.and(criteria);
    }

    /** (c1) OR (c2) OR ... */
    static WhereCriteria or(WhereCriteria... criteria) {
        return CompositeCondition.or(criteria);
    }
}
