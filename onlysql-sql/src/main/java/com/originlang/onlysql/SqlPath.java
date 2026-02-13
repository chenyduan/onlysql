package com.originlang.onlysql;

/**
 * 可参与 WHERE 链式的列路径，需提供列名（用于 SQL）。
 */
public interface SqlPath {

    /** 列名（表列或别名.列），用于生成 "column = ?"。 */
    String getColumnName();

    /** 兼容旧用法，默认与 getColumnName() 一致。 */
    default String getName() {
        return getColumnName();
    }
}
