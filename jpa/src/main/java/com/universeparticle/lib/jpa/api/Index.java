package com.universeparticle.lib.jpa.api;

/**
 * 表索引
 *
 * @author duan
 * @version 1.0.1
 * @since 2023/12/6
 */
public @interface Index {

    /**
     * 索引名
     */
    String name() default "";

    /**
     * 索引字段
     */
    String[] fields() default {};

    /**
     * 索引类型
     */
    String type() default "";

    /**
     * 索引注释
     */
    String comment() default "";

    /**
     * 旧索引名
     */
    String oldName() default "";

}
