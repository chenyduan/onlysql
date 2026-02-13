package com.originlang.onlysql.sql;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * DML 执行器：与具体数据库实现解耦，由 onlysql-jdbc 等模块提供实现。
 * <p>
 * 增删改查均由对应 DSL 的 execute() 委托给当前上下文中的 DmlExecutor。
 * 事务：在 {@link #runInTransaction(Runnable)} 或 {@link #runInTransaction(Callable)} 内执行的多次 DML 使用同一连接，成功则提交，异常则回滚。
 */
public interface DmlExecutor {

    /**
     * 执行 INSERT。单条且 Insert 设置了主键列且未提供主键值时返回生成的主键，否则返回影响行数；批量恒返回影响行数。
     *
     * @param insert 待执行的 Insert DSL
     * @return 生成的主键或影响行数
     */
    long executeInsert(Insert<?> insert);

    /**
     * 执行 UPDATE，返回影响行数。
     *
     * @param update 待执行的 Update DSL
     * @return 影响行数
     */
    long executeUpdate(Update update);

    /**
     * 执行 DELETE，返回影响行数。
     *
     * @param delete 待执行的 Delete DSL
     * @return 影响行数
     */
    long executeDelete(Delete<?> delete);

    /**
     * 执行 SELECT，每行映射为 Map（列名 -> 值）。
     *
     * @param select 待执行的 Select DSL
     * @return 查询结果列表，每行一个 Map
     */
    List<Map<String, Object>> executeSelect(Select select);

    /**
     * 执行 SELECT，每行映射为指定实体类型。
     *
     * @param select      待执行的 Select DSL
     * @param entityClass 实体类，需有无参构造
     * @return 查询结果列表，每行一个实体实例
     */
    <T> List<T> executeSelect(Select select, Class<T> entityClass);

    /**
     * 分页查询，返回当前页数据与总记录数。
     * 内部先执行 COUNT(*) 查询得到 total，再按 limit/offset 执行数据查询。
     *
     * @param select     待执行的 Select DSL（无需自行 limit/offset，由本方法按 pageIndex/pageSize 设置）
     * @param pageIndex  页码，从 0 开始
     * @param pageSize   每页条数
     * @return 分页结果，包含 list、total、pageIndex、pageSize、totalPages
     */
    Page<Map<String, Object>> executeSelectPage(Select select, int pageIndex, int pageSize);

    /**
     * 分页查询并映射为实体。
     *
     * @param select      待执行的 Select DSL
     * @param pageIndex   页码，从 0 开始
     * @param pageSize    每页条数
     * @param entityClass 实体类
     * @return 分页结果，list 为实体列表
     */
    <T> Page<T> executeSelectPage(Select select, int pageIndex, int pageSize, Class<T> entityClass);

    /**
     * 在事务中执行：同一连接、成功提交、异常回滚。
     * 回调内通过当前 {@link ExecutorContext} 的 DmlExecutor 执行的 DML 均在同一事务中。
     *
     * @param runnable 事务内要执行的操作
     * @throws UnsupportedOperationException 若本实现不支持事务
     */
    default void runInTransaction(Runnable runnable) {
        runInTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 在事务中执行并返回结果；成功提交，异常回滚。
     *
     * @param callable 事务内要执行的操作
     * @return callable 的返回值
     * @throws UnsupportedOperationException 若本实现不支持事务
     */
    default <T> T runInTransaction(Callable<T> callable) {
        throw new UnsupportedOperationException("本执行器不支持事务，请使用 JdbcDmlExecutor 等支持事务的实现");
    }
}
