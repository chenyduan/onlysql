package com.originlang.onlysql.database;

import java.util.ArrayList;
import java.util.List;

/**
 * database config
 */
public class DatabaseConfigProperty {
    /**
     * alias
     */
    private String alias = "default";
    /**
     * database name
     */
    private String database;


    /**
     * drive class name
     */
    private String driver;


    /**
     * url
     */
    private String url;


    /**
     * 初始化数据库，create,drop,alter等操作
     */
    private String ddlAction;



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
