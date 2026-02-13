package com.originlang.onlysql.sql;

import java.util.List;

/**
 * WHERE 条件抽象：可由字符串表达式或链式 Path.eq() 构建，供 Select/Update/Delete 使用。
 */
public interface WhereCriteria {

    /** SQL 条件表达式，使用 ? 占位符，如 "id = ? AND name = ?"。 */
    String getWhereExpr();

    /** 与 getWhereExpr() 中 ? 顺序一致的参数列表。 */
    List<Object> getWhereParams();
}
