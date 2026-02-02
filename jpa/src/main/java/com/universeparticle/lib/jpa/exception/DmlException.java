package com.universeparticle.lib.jpa.exception;

/**
 * dml异常,操作数据库中的数据，增删改查等
 */
public class DmlException extends SqlException {
    public DmlException(String message) {
        super(message);
    }

    public DmlException(String message, Throwable cause) {
        super(message, cause);
    }

    public DmlException(Throwable cause) {
        super(cause);
    }

    public DmlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
