# onlysql-sql

DSL 模块：Select / Insert / Update / Delete、WhereCriteria、Path 等，不依赖具体数据库。

## H2 连接方式

本模块不包含 JDBC 实现。若需连接 **H2**，请使用 **onlysql-jdbc** 提供的工厂类：

- 依赖：`onlysql-jdbc` + `com.h2database:h2`
- 创建 DataSource：`com.originlang.onlysql.jdbc.H2DataSources.inMemory("dbName")` 或 `H2DataSources.file("./path")`
- 设置执行器：`ExecutorContext.setExecutor(new JdbcDmlExecutor(dataSource))`

示例见 **onlysql-example** 中基于 H2 的测试与 `H2TestSupport`。
