package com.universeparticle.lib.jpa.datasource;

import com.universeparticle.lib.jpa.database.DatabaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库配置
 */
public class DatasourceConfigProperty {
    /**
     * 数据库逻辑名称，用于区分不同的数据库
     */
    private String alias = "default";
    /**
     * 数据库名称
     */
    private String database;


    /**
     * 数据库驱动
     */
    private String driver;


    /**
     * 数据库连接地址
     */
    private String url;


    /**
     * 初始化数据库，create,drop,alter等操作
     */
    private String ddlAction;

    /**
     * 管理员账号，用于创建数据库，创建表等操作
     */
    private DatabaseUser admin;
    /**
     * 普通账号，用于查询，插入，更新，删除数据等操作
     */
    private DatabaseUser normal;

    /**
     * 要扫描的包
     */
    private List<String> pkgs = new ArrayList<>();


    public String getAlias() {
        return alias;
    }

    public String getDatabase() {
        return database;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }


    public DatabaseUser getAdmin() {
        return admin;
    }

    public DatabaseUser getNormal() {
        return normal;
    }


    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public void setAdmin(DatabaseUser admin) {
        this.admin = admin;
    }

    public void setNormal(DatabaseUser normal) {
        this.normal = normal;
    }

    public String getDdlAction() {
        return ddlAction;
    }

    public void setDdlAction(String ddlAction) {
        this.ddlAction = ddlAction;
    }


    public List<String> getPkgs() {
        return pkgs;
    }

    public void setPkgs(List<String> pkgs) {
        this.pkgs = pkgs;
    }
}
