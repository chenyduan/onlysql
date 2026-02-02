package com.universeparticle.lib.jpa.exception;

/**
 * ddl异常,操作数据库中的表结构，视图，索引等
 */
public class DdlException extends SqlException {

    public DdlException(String message) {
        super(message);
    }

    public DdlException(String message, Throwable cause) {
        super(message, cause);
    }

    public DdlException(Throwable cause) {
        super(cause);
    }

    public DdlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
