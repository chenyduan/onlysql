package com.originlang.onlysql;

public class DatePath<T> {

    private String name;
    private Class<T> type;

    public DatePath(String name, Class<T> type) {
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
