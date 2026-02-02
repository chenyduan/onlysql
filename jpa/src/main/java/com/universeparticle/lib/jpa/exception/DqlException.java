package com.universeparticle.lib.jpa.exception;

/**
 * dql异常,操作数据库中的数据，查询
 */
public class DqlException extends SqlException {

    public DqlException(String message) {
        super(message);
    }

    public DqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public DqlException(Throwable cause) {
        super(cause);
    }

    public DqlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
