package com.universeparticle.lib.jpa.api;

/**
 * 表字段
 *
 * @author duan
 * @version 1.0.1
 * @since 2023/12/6
 */
public @interface Field {

    /**
     * 字段名
     */
    String name() default "";


    /**
     * 字段注释
     */
    String comment() default "";


    /**
     * 字段类型
     */
    String type() default "";


    /**
     * 旧字段名
     */
    String oldName() default "";

}
