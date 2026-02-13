package com.originlang.onlysql.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SELECT DSL：支持 from、columns、多种 JOIN、where、limit、offset，通过 {@link DmlExecutor} 执行查询。
 * <p>
 * 用法示例：
 * <ul>
 *   <li>查全表：Select.from("user").execute()</li>
 *   <li>指定列：Select.from("user").columns("id", "username").where("status = ?", 1).execute()</li>
 *   <li>INNER JOIN：Select.from("order").innerJoin("user", "u", "order.user_id = u.id").execute()</li>
 *   <li>LEFT JOIN：Select.from("user").leftJoin("order", "o", "u.id = o.user_id").columns("u.name", "o.id").execute()</li>
 *   <li>分页：Select.from("user").limit(10).offset(20).execute()</li>
 * </ul>
 */
public class Select {

    /** JOIN 子句描述：类型、表、别名、ON 条件及参数（CROSS JOIN 无 ON）。 */
    public static final class Join {
        private final String type;
        private final String table;
        private final String alias;
        private final String onExpr;
        private final List<Object> onParams;

        public Join(String type, String table, String alias, String onExpr, List<Object> onParams) {
            this.type = type;
            this.table = table;
            this.alias = alias;
            this.onExpr = onExpr;
            this.onParams = onParams == null ? List.of() : new ArrayList<>(onParams);
        }

        public String getType() { return type; }
        public String getTable() { return table; }
        public String getAlias() { return alias; }
        public String getOnExpr() { return onExpr; }
        public List<Object> getOnParams() { return new ArrayList<>(onParams); }
    }

    private static final String INNER = "INNER JOIN";
    private static final String LEFT = "LEFT OUTER JOIN";
    private static final String RIGHT = "RIGHT OUTER JOIN";
    private static final String FULL = "FULL OUTER JOIN";
    private static final String CROSS = "CROSS JOIN";

    private final String table;
    private String tableAlias;
    private final List<String> columns = new ArrayList<>();
    private final List<Join> joins = new ArrayList<>();
    private String whereExpr;
    private final List<Object> whereParams = new ArrayList<>();
    private Long limit;
    private Long offset;

    public Select(String table) {
        this.table = table;
    }

    /** 从指定表查询，等价于 new Select(table)。 */
    public static Select from(String table) {
        return new Select(table);
    }

    /** 为主表指定别名，便于 JOIN 时引用。 */
    public Select as(String alias) {
        this.tableAlias = alias;
        return this;
    }

    /**
     * INNER JOIN 表 ON 条件，支持 ? 占位符。
     * @param table 要 JOIN 的表名
     * @param onExpr ON 条件表达式，如 "order.user_id = u.id"
     */
    public Select innerJoin(String table, String onExpr, Object... params) {
        joins.add(new Join(INNER, table, null, onExpr, params == null ? null : Arrays.asList(params)));
        return this;
    }

    /** INNER JOIN 表 [AS] 别名 ON 条件。 */
    public Select innerJoin(String table, String alias, String onExpr, Object... params) {
        joins.add(new Join(INNER, table, alias, onExpr, params == null ? null : Arrays.asList(params)));
        return this;
    }

    /** LEFT OUTER JOIN 表 ON 条件。 */
    public Select leftJoin(String table, String onExpr, Object... params) {
        joins.add(new Join(LEFT, table, null, onExpr, params == null ? null : Arrays.asList(params)));
        return this;
    }

    /** LEFT OUTER JOIN 表 [AS] 别名 ON 条件。 */
    public Select leftJoin(String table, String alias, String onExpr, Object... params) {
        joins.add(new Join(LEFT, table, alias, onExpr, params == null ? null : Arrays.asList(params)));
        return this;
    }

    /** RIGHT OUTER JOIN 表 ON 条件。 */
    public Select rightJoin(String table, String onExpr, Object... params) {
        joins.add(new Join(RIGHT, table, null, onExpr, params == null ? null : Arrays.asList(params)));
        return this;
    }

    /** RIGHT OUTER JOIN 表 [AS] 别名 ON 条件。 */
    public Select rightJoin(String table, String alias, String onExpr, Object... params) {
        joins.add(new Join(RIGHT, table, alias, onExpr, params == null ? null : Arrays.asList(params)));
        return this;
    }

    /** FULL OUTER JOIN 表 ON 条件。 */
    public Select fullOuterJoin(String table, String onExpr, Object... params) {
        joins.add(new Join(FULL, table, null, onExpr, params == null ? null : Arrays.asList(params)));
        return this;
    }

    /** FULL OUTER JOIN 表 [AS] 别名 ON 条件。 */
    public Select fullOuterJoin(String table, String alias, String onExpr, Object... params) {
        joins.add(new Join(FULL, table, alias, onExpr, params == null ? null : Arrays.asList(params)));
        return this;
    }

    /** CROSS JOIN 表（无 ON 条件）。 */
    public Select crossJoin(String table) {
        joins.add(new Join(CROSS, table, null, null, null));
        return this;
    }

    /** CROSS JOIN 表 [AS] 别名。 */
    public Select crossJoin(String table, String alias) {
        joins.add(new Join(CROSS, table, alias, null, null));
        return this;
    }

    /**
     * 指定查询列；不调用则等价于 SELECT *。
     */
    public Select columns(String... columnNames) {
        columns.clear();
        if (columnNames != null) {
            for (String c : columnNames) {
                if (c != null && !c.isEmpty()) {
                    columns.add(c);
                }
            }
        }
        return this;
    }

    /**
     * WHERE 条件，支持 ? 占位符，参数顺序与 params 一致。
     */
    public Select where(String expression, Object... params) {
        this.whereExpr = expression;
        this.whereParams.clear();
        if (params != null) {
            Collections.addAll(this.whereParams, params);
        }
        return this;
    }

    /**
     * WHERE 条件，使用链式 Path.eq() 或 SingleCondition，如 where(TSysUser.id.eq(2L).name.eq("admin"))。
     */
    public Select where(WhereCriteria criteria) {
        if (criteria == null) {
            this.whereExpr = null;
            this.whereParams.clear();
            return this;
        }
        this.whereExpr = criteria.getWhereExpr();
        this.whereParams.clear();
        List<Object> params = criteria.getWhereParams();
        if (params != null) {
            this.whereParams.addAll(params);
        }
        return this;
    }

    public Select limit(long limit) {
        this.limit = limit;
        return this;
    }

    public Select offset(long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * 使用当前上下文中的 {@link DmlExecutor} 执行 SELECT。
     *
     * @return 每行对应一个 Map（列名 -> 值），未设置执行器时抛异常
     */
    public List<Map<String, Object>> execute() {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)，或使用 execute(DmlExecutor)");
        }
        return executor.executeSelect(this);
    }

    /**
     * 使用指定执行器执行 SELECT。
     */
    public List<Map<String, Object>> execute(DmlExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor 不能为 null");
        }
        return executor.executeSelect(this);
    }

    /**
     * 执行 SELECT 并将每行映射为实体，使用当前上下文中的 {@link DmlExecutor}。
     *
     * @param entityClass 实体类，需有无参构造；列名与实体字段名按忽略大小写、下划线转驼峰匹配
     * @return 实体列表
     */
    public <T> List<T> executeAs(Class<T> entityClass) {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)，或使用 executeAs(Class, DmlExecutor)");
        }
        return executor.executeSelect(this, entityClass);
    }

    /**
     * 使用指定执行器执行 SELECT 并映射为实体。
     */
    public <T> List<T> executeAs(Class<T> entityClass, DmlExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor 不能为 null");
        }
        return executor.executeSelect(this, entityClass);
    }

    /**
     * 分页查询，使用当前上下文中的 {@link DmlExecutor}。
     * 无需自行 limit/offset，由本方法按 pageIndex、pageSize 计算；返回结果包含当前页数据和总记录数。
     *
     * @param pageIndex 页码，从 0 开始
     * @param pageSize  每页条数
     * @return 分页结果，list 为 Map 行数据
     */
    public Page<Map<String, Object>> toPage(int pageIndex, int pageSize) {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)，或使用 toPage(pageIndex, pageSize, DmlExecutor)");
        }
        return executor.executeSelectPage(this, pageIndex, pageSize);
    }

    /**
     * 分页查询（使用指定执行器）。
     */
    public Page<Map<String, Object>> toPage(int pageIndex, int pageSize, DmlExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor 不能为 null");
        }
        return executor.executeSelectPage(this, pageIndex, pageSize);
    }

    /**
     * 分页查询并映射为实体，使用当前上下文中的 {@link DmlExecutor}。
     *
     * @param pageIndex   页码，从 0 开始
     * @param pageSize    每页条数
     * @param entityClass 实体类
     * @return 分页结果，list 为实体列表
     */
    public <T> Page<T> toPage(int pageIndex, int pageSize, Class<T> entityClass) {
        DmlExecutor executor = ExecutorContext.getExecutor();
        if (executor == null) {
            throw new IllegalStateException("未设置 DmlExecutor，请先调用 ExecutorContext.setExecutor(executor)，或使用 toPage(pageIndex, pageSize, entityClass, DmlExecutor)");
        }
        return executor.executeSelectPage(this, pageIndex, pageSize, entityClass);
    }

    /**
     * 分页查询并映射为实体（使用指定执行器）。
     */
    public <T> Page<T> toPage(int pageIndex, int pageSize, Class<T> entityClass, DmlExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor 不能为 null");
        }
        return executor.executeSelectPage(this, pageIndex, pageSize, entityClass);
    }

    public String getTable() {
        return table;
    }

    /** 主表别名，未设置时为 null。 */
    public String getTableAlias() {
        return tableAlias;
    }

    /** 已添加的 JOIN 列表（只读）。 */
    public List<Join> getJoins() {
        return Collections.unmodifiableList(joins);
    }

    /** 空表示 SELECT * */
    public List<String> getColumns() {
        return columns.isEmpty() ? Collections.emptyList() : new ArrayList<>(columns);
    }

    public String getWhereExpr() {
        return whereExpr;
    }

    public List<Object> getWhereParams() {
        return new ArrayList<>(whereParams);
    }

    public Long getLimit() {
        return limit;
    }

    public Long getOffset() {
        return offset;
    }
}
