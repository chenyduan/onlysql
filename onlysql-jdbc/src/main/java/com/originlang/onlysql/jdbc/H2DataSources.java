package com.originlang.onlysql.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * H2 数据库 DataSource 工厂，便于在示例或测试中快速创建内存/文件 H2 连接。
 * <p>
 * 使用前需在 pom 中引入 H2 依赖，例如：
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;com.h2database&lt;/groupId&gt;
 *   &lt;artifactId&gt;h2&lt;/artifactId&gt;
 *   &lt;version&gt;2.4.240&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
public final class H2DataSources {

    private static final String DRIVER = "org.h2.Driver";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private H2DataSources() {
    }

    /**
     * 创建内存 H2 DataSource，库名为 dbName，用户 sa，空密码。
     *
     * @param dbName 库名，如 "testdb"，对应 URL jdbc:h2:mem:testdb
     */
    public static DataSource inMemory(String dbName) {
        return inMemory(dbName, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    /**
     * 创建内存 H2 DataSource。
     *
     * @param dbName  库名
     * @param user    用户名
     * @param password 密码
     */
    public static DataSource inMemory(String dbName, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        config.setDriverClassName(DRIVER);
        config.setUsername(user != null ? user : DEFAULT_USER);
        config.setPassword(password != null ? password : DEFAULT_PASSWORD);
        config.setMaximumPoolSize(4);
        return new HikariDataSource(config);
    }

    /**
     * 创建基于文件的 H2 DataSource。
     *
     * @param filePath 文件路径（不含 .db），如 "./data/mydb" 对应 jdbc:h2:file:./data/mydb
     * @param user    用户名
     * @param password 密码
     */
    public static DataSource file(String filePath, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:" + filePath + ";AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE");
        config.setDriverClassName(DRIVER);
        config.setUsername(user != null ? user : DEFAULT_USER);
        config.setPassword(password != null ? password : DEFAULT_PASSWORD);
        config.setMaximumPoolSize(4);
        return new HikariDataSource(config);
    }

    /**
     * 创建基于文件的 H2 DataSource，用户 sa，空密码。
     */
    public static DataSource file(String filePath) {
        return file(filePath, DEFAULT_USER, DEFAULT_PASSWORD);
    }
}
