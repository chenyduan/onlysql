package com.originlang.onlysql.sql;

/**
 * DML 执行器：与具体数据库实现解耦，由 onlysql-jdbc 等模块提供实现。
 * <p>
 * 执行 INSERT 时由 {@link Insert#execute()} 委托给当前上下文中的 DmlExecutor。
 */
public interface DmlExecutor {

    /**
     * 执行 INSERT，返回影响行数或生成的主键（由实现决定）。
     *
     * @param insert 待执行的 Insert DSL
     * @return 影响行数，或生成的主键
     */
    long executeInsert(Insert<?> insert);
}
