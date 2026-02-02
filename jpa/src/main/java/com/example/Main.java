package com.example;


import com.universeparticle.lib.jpa.database.DatabaseUser;
import com.universeparticle.lib.jpa.datasource.Datasource;
import com.universeparticle.lib.jpa.datasource.DatasourceConfigProperty;
import com.universeparticle.lib.jpa.datasource.DdlAction;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 数据源
        Datasource datasource = new Datasource();
        DatasourceConfigProperty datasourceConfigProperty = new DatasourceConfigProperty();
        datasourceConfigProperty.setAlias("default");
        datasourceConfigProperty.setDatabase("test");
        datasourceConfigProperty.setDriver("org.postgresql.Driver");
        datasourceConfigProperty.setUrl("jdbc:postgresql://localhost:5431/");
        datasourceConfigProperty.setAdmin(new DatabaseUser("postgres", "Aa123456"));
        datasourceConfigProperty.setPkgs(List.of("com.example"));
        datasourceConfigProperty.setDdlAction(DdlAction.CREATE);

        datasource.addDatasource(datasourceConfigProperty);
    }
}
