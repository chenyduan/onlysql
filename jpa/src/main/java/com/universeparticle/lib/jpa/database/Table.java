package com.universeparticle.lib.jpa.database;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表
 */
@Data
public class Table {


    public Table(Class<?> clazz) {

    }


    /**
     * 表名
     */
    private String name;

    /**
     * 主键
     */
    private String primaryKey;


    /**
     * 表注释
     */
    private String comment;

    /**
     * 表字段
     */
    private List<TableColumn> columns = new ArrayList<>(50);

    /**
     * 字段注释
     */
    private Map<String, String> columnComments = new ConcurrentHashMap<>(50);


    /**
     * 索引
     */
    private List<Index> indexes = new ArrayList<>(10);


    /**
     * 创建表的sql
     */
    public String createTable() {


        StringBuilder createTableSql = new StringBuilder();
        createTableSql.append("create table ").append(name).append(" (");

        String primaryKey = null;
        for (TableColumn column : columns) {
            // 主键
            if (column.isPrimaryKey()) {
                primaryKey = column.getName();
            }
            createTableSql.append(column.getName()).append(" ").append(column.getType());
            if (column.getType().equals("varchar") || column.getType().equals("char") || column.getType().equals("text")) {
                // 长度
                createTableSql.append("(").append(column.getLength() == null ? 255 : column.getLength()).append(")");
            }
            // 是否可空
            if (!column.isNullable()) {
                createTableSql.append(" not null");
            }


            createTableSql
                    // 是否自增
                    //.append(column.isAutoIncrement() ? " auto_increment" : "")
                    // 默认值
                    .append(column.getDefaultValue() != null ? " default " + column.getDefaultValue() : "")
                    // 注释
                    //.append(" COMMENT '").append(column.getComment()).append("'")

                    .append(",");
        }


        // 主键
        if (primaryKey != null) {
            createTableSql.append(" PRIMARY KEY (").append(primaryKey).append(")");
        }

        createTableSql.append(")")
        // 表注释
        //.append(" comment = '").append(comment).append("'")
        ;
        return createTableSql.toString();
    }


    /**
     * 创建索引的sql
     */
    public String createIndex() {
        StringBuilder createIndexSql = new StringBuilder();
        for (Index index : indexes) {
            createIndexSql.append("create ");
            if (index.isUnique()) {
                createIndexSql.append("unique ");
            }
            createIndexSql.append("index ").append(index.getName()).append(" on ").append(name).append("(");
            for (String column : index.getColumns()) {
                createIndexSql.append(column).append(",");
            }
            createIndexSql.deleteCharAt(createIndexSql.length() - 1);
            createIndexSql.append(")");
        }
        return createIndexSql.toString();
    }

}
