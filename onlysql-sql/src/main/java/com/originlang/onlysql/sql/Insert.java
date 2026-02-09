package com.originlang.onlysql.sql;

import java.util.List;

public class Insert<T> {

    private String table;

    public Insert(String table) {
        this.table = table;
    }
    public long execute() {
        return 0;
    }



    public Insert<T> set(String column, Object value) {
        return this;
    }
/**
 * batch insert
 */
    public Insert<T> batch(List<T> values) {
        return this;
    }

}
