package com.originlang.onlysql.jdbc;

/**
 * 乐观锁冲突：UPDATE 启用了 versionColumn 但影响行数为 0（版本已变更或记录不存在）。
 */
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
