package com.originlang.onlysql;

public class NumberPath<T> implements SqlPath {

    private String name;
    private Class<T> type;

    public NumberPath(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    
}
