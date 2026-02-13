# OnlySQL 作为完整 ORM 框架 — 功能规划

## 一、当前已有能力

| 能力 | 状态 | 说明 |
|------|------|------|
| 增删改查 DSL | ✅ | Select / Insert / Update / Delete，链式调用 |
| WHERE 条件 | ✅ | 字符串 + 占位符；链式 Path.eq()（TSysUser.id.eq(2L).name.eq("admin")） |
| JOIN | ✅ | INNER / LEFT / RIGHT / FULL OUTER / CROSS |
| 分页 | ✅ | Select.limit() / offset() |
| 代码生成 | ✅ | QEntity（DSL 入口）、TEntity（表/列信息）、REntity（Record） |
| 执行层 | ✅ | DmlExecutor、JdbcDmlExecutor、ExecutorContext |
| H2 连接 | ✅ | H2DataSources 工厂，example 中测试 |

---

## 二、建议补充的功能（按优先级）

### 1. 查询结果映射为实体（高）✅ 已实现

**实现**：

- **API**：`Select.executeAs(Class<T> entityClass)`、`DmlExecutor.executeSelect(Select, Class<T>)`，返回 `List<T>`。
- **onlysql-jdbc**：`EntityMapper` 将每行 `Map<String,Object>` 反射映射为实体；列名与字段匹配规则：忽略大小写、下划线转驼峰（`create_time` → `createTime`）；支持常见类型转换（Number、Timestamp→LocalDateTime、sql.Date→LocalDate 等）。
- **用法**：`q.select().where(TSysUser.id.eq(1L)).executeAs(SysUser.class)`。

### 2. 事务支持（高）✅ 已实现

**实现**：

- **onlysql-sql**：`DmlExecutor` 增加 `runInTransaction(Runnable)`、`runInTransaction(Callable<T>)`（默认抛 UnsupportedOperationException）；`ExecutorContext.runInTransaction(Runnable)` / `runInTransaction(Callable<T>)` 委托给当前执行器。
- **onlysql-jdbc**：`JdbcDmlExecutor` 使用 ThreadLocal 绑定事务连接；事务内 `getConnection()` 返回同一连接，回调结束后 commit，异常时 rollback，finally 中恢复 autoCommit 并关闭连接。
- **用法**：`ExecutorContext.runInTransaction(() -> { insert(); update(); });` 或 `executor.runInTransaction(() -> { ... });`。

### 3. 批量插入实现（高）✅ 已实现

**实现**：

- **Insert**：`batch(List<Map<String, Object>> rows)` 设置待插入的多行；`getBatchRows()` 供执行器使用。列结构以第一行 key 为准。
- **JdbcDmlExecutor**：`executeInsert` 时若 `getBatchRows()` 非空则走 `executeInsertBatch`：同一 PreparedStatement、`addBatch()` 每行、每满 500 行执行一次 `executeBatch()`，最后返回总影响行数。
- **EntityMapper**：新增 `entityToMap(Object entity)`、`entityToMap(entity, boolean snakeCaseKeys)`，便于将实体列表转为 `List<Map>` 再 `batch(rows).execute()`。

### 4. 主键策略与 Insert 返回值（中）

**现状**：Insert 可返回“影响行数或主键”，语义依赖实现；未抽象主键生成策略。

**建议**：

- 明确约定：**单条 Insert** 返回当前实体。
- 可选：在实体上支持 `@GeneratedValue`，或配置级“主键策略”（AUTO/IDENTITY/SEQUENCE/UUID），由执行层按库实现。

### 5. 分页封装（中）✅ 已实现

**实现**：

- **Page<T>**（onlysql-sql）：`list`、`total`、`pageIndex`（从 0 开始）、`pageSize`、`totalPages`，以及 `hasNext()`、`hasPrevious()`、`isEmpty()`。
- **DmlExecutor**：`executeSelectPage(Select, pageIndex, pageSize)`、`executeSelectPage(Select, pageIndex, pageSize, Class<T>)`，返回 `Page<Map>` 或 `Page<T>`。
- **JdbcDmlExecutor**：先执行 `COUNT(*)`（同 FROM/JOIN/WHERE）得到 total，再按 limit=pageSize、offset=pageIndex*pageSize 执行数据查询。
- **Select**：`toPage(pageIndex, pageSize)`、`toPage(pageIndex, pageSize, Class<T>)`，使用当前上下文执行器。
- **用法**：`q.select().where(...).toPage(0, 10)` 或 `.toPage(0, 10, SysUser.class)`。

### 6. Path 条件扩展（中）

**现状**：仅有 `eq(value)`。

**建议**：

- 在 NumberPath / StringPath 等上增加：`gt`, `gte`, `lt`, `lte`, `ne`, `in(Collection)`, `isNull()`；StringPath 增加 `like`, `startsWith`, `endsWith`。
- 条件组合：`WhereCriteria` 支持 `and(c1, c2)` / `or(c1, c2)`，便于组合链式与手写条件。

### 7. 数据库方言（中）

**现状**：分页用 LIMIT/OFFSET，部分数据库（如 Oracle、SQL Server）语法不同。

**建议**：

- 在 **onlysql-jdbc** 引入 **Dialect** 接口（如 `getLimitOffsetSql(String sql, long limit, long offset)`），按库实现 H2Dialect、MySQLDialect、PgDialect 等。
- JdbcDmlExecutor 构造时或根据 URL 选择 Dialect，生成 SQL 时通过 Dialect 改写。

### 8. Repository 层（中）

**现状**：业务层直接 `new QSysUser().select()...`，无统一数据访问抽象。

**建议**：

- 提供 **Repository 基类/接口**：如 `EntityRepository<Entity, ID>`，内置 `findById(ID)`、`insert(Entity)`、`update(Entity)`、`deleteById(ID)`，内部使用 Q/T 与 DmlExecutor。
- 可选：APT 为每个 Entity 生成 `XxxRepository`，或约定“某包下接口由框架生成实现”。

### 9. 乐观锁（低）**

**建议**：

- 实体支持 `@Version` 字段，Update 时自动带 `WHERE version = ?` 并在 SET 中 `version = version + 1`；若影响行数为 0 可抛乐观锁异常。

### 10. DDL / 建表（低）**

**建议**：

- 根据 `@Entity` + `@Table` / `@Column` 生成 `CREATE TABLE`（可选带 IF NOT EXISTS），由执行层执行；适合测试或简单迁移，生产仍建议用专业迁移工具。

### 11. 多数据源 / 读写分离（低）**

**建议**：

- ExecutorContext 支持“键”（如 "master" / "slave"），按键绑定不同 DataSource；或提供 `ExecutorContext.runWith(DmlExecutor, Callback)` 临时切换。

### 12. 可观测性（低）✅ 已实现

**实现**：

- **ObservabilityConfig**（onlysql-jdbc）：可配置 `logSql`、`slowQueryThresholdMs`、`sqlLogger`、`metrics`。
- **SQL 日志**：`SqlLogger` 接口，默认实现 `Slf4jSqlLogger`，打印每条 SQL、参数及耗时（需 Slf4j，DEBUG 级别日志名 `com.originlang.onlysql.jdbc.sql`）。
- **慢查询**：超过 `slowQueryThresholdMs` 时调用 `SqlLogger.onSlowQuery`，默认以 WARN 输出。
- **Metrics**：`ExecutionMetrics` 接口，内存实现 `SimpleExecutionMetrics` 按操作类型统计执行次数与总耗时。
- **用法**：`new JdbcDmlExecutor(dataSource, ObservabilityConfig.builder().logSql(true).slowQueryThresholdMs(500).metrics(metrics).build())`。

---

## 三、实现顺序建议

1. **结果映射为实体** — 最直接影响“像 ORM 一样用”。
2. **事务** — 保证多步操作一致性。
3. **批量插入** — 补齐 Insert 的 batch 能力。
4. **主键与 Insert 返回值** — 明确语义，便于 Repository 封装。
5. **分页 Page** — 常用且易做。
6. **Path 条件 + 方言** — 提升表达力和多库兼容。
7. **Repository + 乐观锁 / DDL / 多数据源 / 日志** — 按需迭代。

---

## 四、与“完整 ORM”的对比（简要）

| 维度 | 典型 ORM（如 JPA/Hibernate） | OnlySQL 现状 | 建议 |
|------|------------------------------|--------------|------|
| 查询 | JPQL / Criteria / 原生 SQL | DSL + 手写列名/条件 | 保持 DSL，补实体映射与更多条件 |
| 映射 | 实体 ↔ 表自动映射 | 仅 Map 或手写 | 增加 executeAs(Entity) / 基于 R 的映射 |
| 事务 | @Transactional / 编程式 | 无 | 增加 runInTransaction / 注解支持 |
| 主键 | @GeneratedValue 等 | 未统一 | 明确策略与 Insert 返回值 |
| 关联 | 一对多/多对一/懒加载 | 无 | 可后期做简单关联或保持“显式 JOIN” |
| 批量 | Batch API | batch() 未实现 | 实现 JDBC batch |
| 分页 | Pageable / Page | limit/offset | 封装为 Page<T> |
| 多库 | 方言 | 未抽象 | Dialect 接口 + 按库实现 |

若优先做 **实体映射 + 事务 + 批量插入**，即可在“轻量 ORM”场景下覆盖大部分需求；其余功能可按上表逐步补全。
