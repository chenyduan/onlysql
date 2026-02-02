package com.originlang.test;

import com.originlang.onlysql.DatasourceSet;
import com.originlang.onlysql.database.DatabaseConfigProperty;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatasourceTest {


    @Test
    public void testDatasource() throws SQLException {
        DatabaseConfigProperty configProperty = new DatabaseConfigProperty();

        DatasourceSet datasourceSet = new DatasourceSet(configProperty);
        DataSource dataSource = datasourceSet.get();
        Connection connection = dataSource.getConnection();
        var statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select 1");
        System.out.println(resultSet.wasNull());
//        statement.executeUpdate(" create database " + dabasename);
    }
}
