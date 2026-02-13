package com.originlang.onlysql.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将查询结果行（Map 列名 -> 值）映射为实体实例。
 * 列名与实体字段匹配规则：忽略大小写；下划线转驼峰（create_time -> createTime）。
 */
public final class EntityMapper {

    private EntityMapper() {
    }

    /**
     * 将多行 Map 依次映射为实体列表。
     *
     * @param rows        每行一个 Map（列名 -> 值），列名可为数据库返回的任意大小写
     * @param entityClass 实体类，需有无参构造
     * @return 实体列表，行与 rows 一一对应
     */
    public static <T> List<T> mapToEntities(List<Map<String, Object>> rows, Class<T> entityClass) {
        if (rows == null) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            result.add(mapToEntity(row, entityClass));
        }
        return result;
    }

    /**
     * 将实体转为 Map（字段名 -> 值），用于批量插入等；key 为 Java 字段名。
     */
    public static Map<String, Object> entityToMap(Object entity) {
        return entityToMap(entity, false);
    }

    /**
     * 将实体转为 Map；snakeCaseKeys 为 true 时 key 为下划线命名（如 create_time），便于直接作插入列名。
     */
    public static Map<String, Object> entityToMap(Object entity, boolean snakeCaseKeys) {
        if (entity == null) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> current = entity.getClass();
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object value = f.get(entity);
                    String key = f.getName();
                    if (snakeCaseKeys) {
                        key = camelToSnake(key);
                    }
                    map.put(key, value);
                } catch (IllegalAccessException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        return map;
    }

    private static String camelToSnake(String camel) {
        if (camel == null || camel.isEmpty()) return camel;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将单行 Map 映射为实体实例。
     */
    public static <T> T mapToEntity(Map<String, Object> row, Class<T> entityClass) {
        if (row == null || row.isEmpty()) {
            return newInstance(entityClass);
        }
        T instance = newInstance(entityClass);
        Map<String, Field> fieldByName = collectFields(entityClass);
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String columnKey = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            String normalized = normalizeColumnName(columnKey);
            Field field = fieldByName.get(normalized);
            if (field == null) {
                field = fieldByName.get(normalized.toLowerCase());
            }
            if (field == null) {
                field = fieldByName.get(columnKey);
            }
            if (field == null) {
                continue;
            }
            setField(instance, field, value);
        }
        return instance;
    }

    private static <T> T newInstance(Class<T> entityClass) {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("实体类需有无参构造: " + entityClass.getName(), e);
        }
    }

    /** 列名规范化：小写 + 下划线转驼峰，用于与字段名匹配。 */
    private static String normalizeColumnName(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            return columnName;
        }
        String lower = columnName.toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    /** 收集本类及父类所有声明字段，key 为字段名及小写（便于列名规范化后匹配）。 */
    private static Map<String, Field> collectFields(Class<?> clazz) {
        Map<String, Field> map = new LinkedHashMap<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                String name = f.getName();
                map.putIfAbsent(name, f);
                map.putIfAbsent(name.toLowerCase(), f);
            }
            current = current.getSuperclass();
        }
        return map;
    }

    private static void setField(Object target, Field field, Object value) {
        if (value == null) {
            return;
        }
        try {
            Object converted = convert(value, field.getType());
            if (converted == null) {
                return;
            }
            field.setAccessible(true);
            field.set(target, converted);
        } catch (Exception e) {
            throw new RuntimeException("设置字段失败: " + field.getDeclaringClass().getSimpleName() + "." + field.getName() + ", 值类型 " + value.getClass().getSimpleName(), e);
        }
    }

    /** 将 JDBC 返回值转为实体字段类型。 */
    private static Object convert(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        if (targetType == String.class) {
            return value.toString();
        }
        if (value instanceof Number) {
            Number n = (Number) value;
            if (targetType == long.class || targetType == Long.class) {
                return n.longValue();
            }
            if (targetType == int.class || targetType == Integer.class) {
                return n.intValue();
            }
            if (targetType == short.class || targetType == Short.class) {
                return n.shortValue();
            }
            if (targetType == byte.class || targetType == Byte.class) {
                return n.byteValue();
            }
            if (targetType == double.class || targetType == Double.class) {
                return n.doubleValue();
            }
            if (targetType == float.class || targetType == Float.class) {
                return n.floatValue();
            }
            if (targetType == BigDecimal.class) {
                return value instanceof BigDecimal ? value : BigDecimal.valueOf(n.doubleValue());
            }
        }
        if (value instanceof java.sql.Timestamp) {
            java.sql.Timestamp ts = (java.sql.Timestamp) value;
            if (targetType == LocalDateTime.class) {
                return ts.toLocalDateTime();
            }
            if (targetType == java.time.Instant.class) {
                return ts.toInstant();
            }
            return ts;
        }
        if (value instanceof java.sql.Date) {
            java.sql.Date d = (java.sql.Date) value;
            if (targetType == LocalDate.class) {
                return d.toLocalDate();
            }
            if (targetType == java.util.Date.class) {
                return new java.util.Date(d.getTime());
            }
            return d;
        }
        if (value instanceof java.sql.Time) {
            java.sql.Time t = (java.sql.Time) value;
            if (targetType == LocalTime.class) {
                return t.toLocalTime();
            }
            return t;
        }
        if (value instanceof java.util.Date && (targetType == LocalDateTime.class)) {
            return new java.sql.Timestamp(((java.util.Date) value).getTime()).toLocalDateTime();
        }
        return value;
    }
}
