package com.originlang.onlysql;

import com.originlang.onlysql.database.DatabaseConfigProperty;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatasourceSet {
    private static final Map<String, DataSource> datasource = new ConcurrentHashMap<>(1);


    public DataSource get() {
        return datasource.get("default");
    }


    public DatasourceSet(DatabaseConfigProperty property) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("password");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource ds = new HikariDataSource(config);
        datasource.put(property.getAlias(), ds);
    }

}
