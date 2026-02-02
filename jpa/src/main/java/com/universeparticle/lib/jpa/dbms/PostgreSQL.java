package com.universeparticle.lib.jpa.dbms;

import com.universeparticle.lib.jpa.database.*;
import com.universeparticle.lib.jpa.datasource.DatasourceConfigProperty;
import com.universeparticle.lib.jpa.exception.DdlException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL数据库管理系统
 */
@SuppressWarnings("unused")
public class PostgreSQL implements Database {


    /**
     * 数据库逻辑名称，用于区分不同的数据库,单个数据源可以不定义。
     */
    private final String alias;
    /**
     * 数据库名称
     */
    private final String database;


    /**
     * 数据库驱动
     */
    private final String driver;


    /**
     * 数据库连接地址
     */
    private final String url;


    /**
     * 初始化数据库，create,drop,alter等操作
     */
    private final String ddlAction;

    /**
     * 管理员账号，用于创建数据库，创建表等操作
     */
    private final DatabaseUser admin;
    /**
     * 普通账号，用于查询，插入，更新，删除数据等操作
     */
    private final DatabaseUser normal;


    public PostgreSQL(DatasourceConfigProperty configProperty) {
        this.alias = configProperty.getAlias();
        this.database = configProperty.getDatabase();
        this.driver = configProperty.getDriver();
        this.url = configProperty.getUrl();
        this.ddlAction = configProperty.getDdlAction();
        this.admin = configProperty.getAdmin();
        this.normal = configProperty.getNormal();
        // 扫描包


    }

    private final List<Table> tables = new ArrayList<>(100);


    public void addTables(List<Table> ta) {
        tables.addAll(ta);
    }


    /**
     * 执行sql
     */
    public void execute(DatabaseUser user, String sql) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            // 加载驱动
            Class<?> driveClass = Class.forName(driver);
            //  获取数据库连接
            connection = DriverManager.getConnection(url, user.getUsername(), user.getPassword());
            // 获取执行sql的对象
            ps = connection.prepareStatement(sql);
            // 执行sql
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    // 关闭执行sql的对象
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    // 关闭数据库连接
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 创建数据库
     */
    public void createDatabase() {
        // createdb -h localhost -p 5432 -U postgres runoobdb
        DatabaseUser user = null;
        if (admin != null) {
            user = admin;
        } else {
            user = normal;
        }
        String sql = "CREATE DATABASE  " + database + "  --encoding UTF8  ;";
        execute(user, sql);
    }

    /**
     * 删除数据库
     */
    @Override
    public void dropDatabase() {
        // DROP DATABASE IF EXISTS `test`;
        String sql = "DROP DATABASE IF EXISTS " + database + ";";
        execute(admin, sql);
    }

    /**
     * truncate table
     */
    public void truncateTable(String table) {
        // truncate table kn_node_properties;
        String sql = "truncate table " + table + ";";
        execute(normal, sql);
    }

    /**
     * 创建表
     */
    public void createTable() {
        for (Table table : tables) {
            String sql = table.createTable();
            execute(admin, sql);
        }
    }

    /**
     * 创建索引，
     *
     * @return 返回创建索引的sql
     */
    public List<String> createIndex(Table table) {
        List<String> indexSqls = new ArrayList<>();


        List<Index> indexes = table.getIndexes();
        for (Index index : indexes) {
            String sql = createIndex(table.getName(), index);
            indexSqls.add(sql);
        }
        return indexSqls;

    }


    /**
     * 创建索引的sql,单个索引
     */

    @Override
    public String createIndex(String table, Index index) {
        // CREATE UNIQUE INDEX i_unique_nodeid_propkeyname ON kn_node_properties(nodeid,propkeyname);
        if (index.getColumns().isEmpty()) {
            throw new DdlException("create index error,columns is empty,table:" + table + ",index:" + index.getName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ").append(index.isUnique() ? "UNIQUE " : "").append("INDEX ").append(index.getName()).append(" ON ").append(table).append("(");
        for (String column : index.getColumns()) {
            sb.append(column).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }


    /**
     * drop table
     */
    @Override
    public String dropTable(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    /**
     * 创建外键
     */

    public void createForeignKey() {
        throw new DdlException("暂不支持创建外键");
    }


    /**
     * 创建PostgreSQL数据库
     *
     * @param table 表
     * @return sql
     */
    @Override
    public String createTable(Table table) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(table.getName()).append(" (");
        List<TableColumn> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            TableColumn column = columns.get(i);
            sql.append(column.getName()).append(" ").append(column.getType());
            if (column.getLength() != null) {
                sql.append("(").append(column.getLength()).append(")");
            }
            if (column.isPrimaryKey()) {
                sql.append(" PRIMARY KEY");
            }
            if (column.isAutoIncrement()) {
                sql.append(" AUTO_INCREMENT");
            }
            if (column.getDefaultValue() != null) {
                sql.append(" DEFAULT ").append(column.getDefaultValue());
            }
            if (i != columns.size() - 1) {
                sql.append(",");
            } else {
                // 主键
                if (table.getPrimaryKey() != null) {
                    sql.append(",PRIMARY KEY (").append(table.getPrimaryKey()).append(")");
                }
            }
        }
        sql.append(")");
        //

        return sql.toString();
    }

}
