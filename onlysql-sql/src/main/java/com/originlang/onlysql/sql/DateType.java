package com.originlang.onlysql.sql;

public class DateType {
    /**
     * java type to db type
     */
    public String to(String javaType) {
        switch (javaType) {
            case null -> throw new RuntimeException("can not change null to a sql type");
            default -> {
                return "varchar";
            }
            case "java.lang.Integer" -> {
                return "int";
            }
            case "java.lang.Long" -> {
                return "bigint";
            }

        }

    }

    /**
     * db type to java type
     */
    public void of() {

    }
}
