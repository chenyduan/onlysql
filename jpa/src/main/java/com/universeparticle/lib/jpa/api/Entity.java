package com.universeparticle.lib.jpa.api;

/**
 * 表
 *
 * @author duan
 * @version 1.0.1
 * @since 2023/12/6
 */
public @interface Entity {
    /**
     * 表名
     */
    String name() default "";

    /**
     * 表注释
     */
    String comment() default "";


    /**
     * 旧表名
     */
    String oldName() default "";


}
