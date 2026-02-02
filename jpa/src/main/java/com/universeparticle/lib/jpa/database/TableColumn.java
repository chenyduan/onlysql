package com.universeparticle.lib.jpa.database;

import lombok.Data;

/**
 * 表字段
 */
@Data
public class TableColumn {

    /**
     * 字段名
     */
    private String name;

    /**
     * 字段注释
     */
    private String comment;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 字段长度
     */
    private Integer length;

    /**
     * 字段精度
     */
    private int scale;


    /**
     * 是否可空
     */

    private boolean nullable;

    /**
     * 是否主键
     */
    private boolean primaryKey;

    /**
     * 是否自增
     */
    private boolean autoIncrement;

    /**
     * 默认值
     */
    private String defaultValue;


}
