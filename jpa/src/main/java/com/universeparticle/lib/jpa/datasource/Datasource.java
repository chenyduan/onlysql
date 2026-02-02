package com.universeparticle.lib.jpa.datasource;

import com.universeparticle.lib.jpa.database.Database;
import com.universeparticle.lib.jpa.exception.SqlException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源，
 */
public class Datasource {

    /**
     * 数据源,key- 数据源名称，value- 数据库的抽象
     */
    private static final Map<String, Database> databases = new ConcurrentHashMap<>(1);


    /**
     * 添加数据源
     *
     * @param config 数据源的配置
     * @return 返回添加的数据源
     */
    public Database addDatasource(DatasourceConfigProperty config) {
        if (databases.containsKey(config.getAlias())) {
            throw new SqlException("datasource name is exists, name: " + config.getAlias() + ",please change another name.");
        }
        Database database = Database.getInstance(config);
        if (DdlAction.CREATE.equals(config.getDdlAction())) {
            database.dropDatabase();
            database.createDatabase();
            database.createTable();
        }

        return databases.put(config.getAlias(), database);
    }


    /**
     * 获取数据源
     *
     * @param name 数据源名称
     * @return 返回数据源
     */
    public Database getDatasource(String name) {
        return databases.get(name);
    }


    /**
     * 创建数据源中的数据库及表
     */
    public void createDatabaseAndTable() {
        for (Database database : databases.values()) {
            database.createDatabase();
            database.createTable();
//            database.createIndex();
            database.createForeignKey();
        }
    }

}
