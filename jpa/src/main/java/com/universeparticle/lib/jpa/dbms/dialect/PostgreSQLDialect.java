package com.universeparticle.lib.jpa.dbms.dialect;


import com.universeparticle.lib.jpa.exception.DdlException;

import java.util.List;

/**
 * postgresql方言
 */
public class PostgreSQLDialect {


    public String toDatabaseType(Object javaType) {
        return DataType.javaType2dbType(javaType);
    }

    /**
     * 数据类型
     */
    public static class DataType {
        /**
         * java类型转换为数据库类型
         *
         * @param javaType java类型
         * @return 数据库类型
         */
        public static String javaType2dbType(Object javaType) {
            if (javaType == null) {
                throw new DdlException("not support java type,javaType is null");
            } else if (javaType instanceof String) {
                return VARCHAR;
            } else if (javaType instanceof Integer) {
                return INTEGER;
            } else if (javaType instanceof Long) {
                return BIGINT;
            } else if (javaType instanceof Float) {
                return REAL;
            } else if (javaType instanceof Double) {
                return DOUBLE_PRECISION;
            } else if (javaType instanceof Boolean) {
                return BOOLEAN;
            } else if (javaType instanceof java.sql.Date) {
                return DATE;
            } else if (javaType instanceof java.sql.Time) {
                return TIME;
            } else if (javaType instanceof java.sql.Timestamp) {
                return TIMESTAMP;
            } else if (javaType instanceof Short) {
                return SMALLINT;
            } else if (javaType instanceof List<?>) {
                return ARRAY;
            }
            throw new DdlException("not support java type," + javaType.getClass().getName());
        }


        /**
         * 字符串
         */
        public static final String VARCHAR = "varchar";

        /**
         * 字符串
         */
        public static final String CHAR = "char";

        /**
         * 字符串
         */
        public static final String TEXT = "text";

        /**
         * 小整数
         */
        public static final String SMALLINT = "smallint";

        /**
         * 整数
         */
        public static final String INTEGER = "integer";

        /**
         * 大整数
         */
        public static final String BIGINT = "bigint";

        /**
         * 单精度浮点数
         */
        public static final String REAL = "real";

        /**
         * 双精度浮点数
         */
        public static final String DOUBLE_PRECISION = "double precision";

        /**
         * 可变精度数值类型
         */
        public static final String NUMERIC = "numeric";

        /**
         * 日期类型
         */
        public static final String DATE = "date";

        /**
         * 时间类型
         */
        public static final String TIME = "time";

        /**
         * 日期和时间类型
         */
        public static final String TIMESTAMP = "timestamp";

        /**
         * 时间间隔类型
         */
        public static final String INTERVAL = "interval";

        /**
         * 布尔类型
         */
        public static final String BOOLEAN = "boolean";

        /**
         * 二进制数据类型
         */
        public static final String BYTEA = "bytea";

        /**
         * 可变长度的数组类型
         */
        public static final String ARRAY = "array";

        /**
         * 存储JSON格式的数据
         */
        public static final String JSON = "json";

        /**
         * bson
         */
        public static final String BSON = "bson";

        /**
         * 通用唯一标识符类型
         */
        public static final String UUID = "uuid";


        /*
   ###### PostgreSQL字段

- 整数类型：

SMALLINT：小整数，占用2字节。
INTEGER：整数，占用4字节。
BIGINT：大整数，占用8字节。
- 浮点数类型：

REAL：单精度浮点数，占用4字节。
DOUBLE PRECISION：双精度浮点数，占用8字节。
- 数值类型：

NUMERIC(precision, scale)：可变精度数值类型，指定了总位数和小数位数。
- 字符类型：

CHAR(n)：固定长度的字符，最多占用n字节。
VARCHAR(n)：可变长度的字符，最多占用n字节。
TEXT：可变长度的字符，适合存储大量文本数据。
- 日期和时间类型：

DATE：日期类型。
TIME：时间类型。
TIMESTAMP：日期和时间类型。
INTERVAL：时间间隔类型。
- 布尔类型：

BOOLEAN：布尔类型，取值为TRUE或FALSE。
- 二进制类型：

BYTEA：二进制数据类型，用于存储任意字节序列。
- 数组类型：

ARRAY：可变长度的数组类型，可以存储多个相同类型的值。
- JSON类型：

JSON：存储JSON格式的数据。
- UUID类型：

UUID：通用唯一标识符类型，用于存储唯一标识符。

        * */

    }


}
