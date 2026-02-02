package com.universeparticle.lib.jpa.database;

import com.universeparticle.lib.jpa.CodeGenerater;
import com.universeparticle.lib.jpa.datasource.DatasourceConfigProperty;
import com.universeparticle.lib.jpa.dbms.PostgreSQL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库
 */

public interface Database {

    static Database getInstance(DatasourceConfigProperty configProperty) {
        // todo 扫描包

        if ("org.postgresql.Driver".equals(configProperty.getDriver())) {
            var tables = getTables(configProperty.getPkgs());
            var post = new PostgreSQL(configProperty);
            post.addTables(tables);
            return post;
        } else if ("com.mysql.cj.jdbc.Driver".equals(configProperty.getDriver())) {
            throw new RuntimeException("暂不支持mysql");
        }
        throw new RuntimeException("暂不支持该数据库");


    }


    // todo class 转Table
    public static List<Table> getTables(List<String> pkgs) {
        List<Table> tables = new ArrayList<>();
        CodeGenerater codeGenerater = new CodeGenerater();
        var classes = doScan(pkgs);

        for (Class<?> clazz : classes) {
            var table = codeGenerater.toTable(clazz);
            if (table != null) {
                tables.add(table);
            }
        }
        return tables;
    }


    /**
     * 扫描包,获取所有类Clazz
     *
     * @param pkgs 包名
     * @return 类
     */

    private static List<Class<?>> doScan(List<String> pkgs) {
        var names = scan(pkgs);
        List<Class<?>> classes = new ArrayList<>();
        for (String name : names) {
            try {
                classes.add(Class.forName(name));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }


    /**
     * 扫描包,获取所有类名
     *
     * @param pkgs 包名
     * @return 类名
     */
    public static List<String> scan(List<String> pkgs) {
        List<String> classNames = new ArrayList<>();
        for (String pkg : pkgs) {
            classNames.addAll(scan(pkg));
        }
        return classNames;
    }


    /**
     * 扫描包,获取所有类名
     *
     * @param pkg 包名
     * @return 类名
     */
    private static List<String> scan(String pkg) {
        String packagePath = pkg.replace(".", "/");
        List<String> classNames = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String packageDirectory = classLoader.getResource(packagePath).getFile();
            File directory = new File(packageDirectory);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = pkg + "." + file.getName().replaceAll(".class$", "");
                            classNames.add(className);
                        } else if (file.isDirectory()) {
                            // 递归
                            classNames.addAll(scan(pkg + "." + file.getName()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classNames;
    }


    /**
     * drop table
     */
    default String dropTable(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }


    void createDatabase();

    void dropDatabase();

    void createTable();


    void createForeignKey();


    String createIndex(String table, Index index);

    String createTable(Table table);
}
