package com.universeparticle.lib.jpa.database;

import java.util.ArrayList;
import java.util.List;

/**
 * 索引
 */
public class Index {

    /**
     * 索引名
     */
    private String name;

    /**
     * 索引字段
     */
    private final List<String> columns = new ArrayList<>(8);

    /**
     * 索引类型
     */
    private String type;

    /**
     * 索引注释
     */
    private String comment;

    /**
     * 是否唯一
     */
    private boolean unique;

    /**
     * 是否聚集索引
     */
    private boolean clustered;

    /**
     * 是否主键
     */
    private boolean primaryKey;

    /**
     * 是否外键
     */
    private boolean foreignKey;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(boolean foreignKey) {
        this.foreignKey = foreignKey;
    }
}
