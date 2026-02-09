package com.originlang.onlysql.sql;

public class NumberType<T> implements SqlType {

    private Integer min;

    private Integer max;


    public NumberType<T> to(String javaType) {
        return new NumberType<>();
    }
}
