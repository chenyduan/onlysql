package com.universeparticle.lib.jpa.exception;

/**
 * dcl异常,操作数据库权限，
 * 控制数据库中的用户访问权限，如grant,revoke等
 */
public class DclException extends SqlException {

    public DclException(String message) {
        super(message);
    }

    public DclException(String message, Throwable cause) {
        super(message, cause);
    }

    public DclException(Throwable cause) {
        super(cause);
    }

    public DclException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
