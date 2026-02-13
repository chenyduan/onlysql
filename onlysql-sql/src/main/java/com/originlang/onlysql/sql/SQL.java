package com.originlang.onlysql.sql;

/**
 * 增删改查统一入口：提供静态工厂方法构建 Select / Insert / Update / Delete。
 * <p>
 * 示例：
 * <ul>
 *   <li>查：SQL.from("user").where("id = ?", 1L).execute()</li>
 *   <li>增：SQL.insert("user").set("id", 1L).set("name", "x").execute()</li>
 *   <li>改：SQL.update("user").set("name", "y").where("id = ?", 1L).execute()</li>
 *   <li>删：SQL.delete("user").where("id = ?", 1L).execute()</li>
 * </ul>
 */
public final class SQL {

    private SQL() {
    }

    /** 构建 SELECT（查） */
    public static Select from(String table) {
        return new Select(table);
    }

    /** 构建 INSERT（增） */
    public static Insert<Object> insert(String table) {
        return new Insert<>(table);
    }

    /** 构建 UPDATE（改） */
    public static Update update(String table) {
        return new Update(table);
    }

    /** 构建 DELETE（删） */
    public static Delete<Object> delete(String table) {
        return new Delete<>(table);
    }
}
