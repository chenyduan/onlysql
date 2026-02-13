package com.originlang.onlysql;

import com.originlang.onlysql.jdbc.EntityMapper;
import com.originlang.onlysql.jdbc.EntityDdlBuilder;
import com.originlang.onlysql.jdbc.H2DataSources;
import com.originlang.onlysql.jdbc.Dialect;
import com.originlang.onlysql.jdbc.JdbcDmlExecutor;
import com.originlang.onlysql.jdbc.LimitOffsetDialect;
import com.originlang.onlysql.jdbc.MySQLDialect;
import com.originlang.onlysql.jdbc.ObservabilityConfig;
import com.originlang.onlysql.jdbc.OptimisticLockException;
import com.originlang.onlysql.jdbc.OracleDialect;
import com.originlang.onlysql.jdbc.SimpleExecutionMetrics;
import com.originlang.onlysql.sql.ExecutorContext;
import com.originlang.onlysql.sql.WhereCriteria;
import com.originlang.onlysql.sql.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用 H2 内存库连接 onlysql-example，测试增删改查。
 */
class SysUserTest {

    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = H2TestSupport.initH2WithSysUser();
    }

    @Test
    void insertAndSelect() {
        QSysUser q = new QSysUser();
        q.insert().set("id", 1L).set("username", "admin").set("age", 20).execute();

        List<Map<String, Object>> rows = q.select().columns("id", "username", "age").where(TSysUser.id.eq(1L)).execute();
        assertFalse(rows.isEmpty());
        assertEquals(1L, rows.get(0).get("ID"));
        assertEquals("admin", rows.get(0).get("USERNAME"));
        assertEquals(20, rows.get(0).get("AGE"));
    }

    @Test
    void insertReturnsEntityWithGeneratedKey() {
        QSysUser q = new QSysUser();
        SysUser entity = q.insert().set("username", "autoUser").set("age", 99).execute(SysUser.class);
        assertNotNull(entity);
        assertTrue(entity.getId() != null && entity.getId() >= 1, "应返回带生成主键的实体");
        assertEquals("autoUser", entity.getUsername());
        assertEquals(99, entity.getAge());
    }

    @Test
    void insertExecuteReturnsEntity() {
        QSysUser q = new QSysUser();
        SysUser entity = q.insert().set("username", "returnUser").set("age", 88).execute(SysUser.class);
        assertNotNull(entity);
        assertTrue(entity.getId() != null && entity.getId() >= 1, "应带生成的主键");
        assertEquals("returnUser", entity.getUsername());
        assertEquals(88, entity.getAge());
        SysUser withId = q.insert().set("id", 999L).set("username", "explicit").set("age", 1).execute(SysUser.class);
        assertNotNull(withId);
        assertEquals(999L, withId.getId());
        assertEquals("explicit", withId.getUsername());
    }

    @Test
    void update() {
        QSysUser q = new QSysUser();
        q.insert().set("id", 2L).set("username", "old").set("age", 18).execute();

        long n = q.update().set("username", "updated").set("age", 19).where(TSysUser.id.eq(2L)).execute();
        assertEquals(1, n);

        List<Map<String, Object>> rows = q.select().where(TSysUser.id.eq(2L)).execute();
        assertEquals("updated", rows.get(0).get("USERNAME"));
        assertEquals(19, rows.get(0).get("AGE"));
    }

    @Test
    void delete() {
        QSysUser q = new QSysUser();
        q.insert().set("id", 3L).set("username", "toDelete").execute();

        long n = q.delete().where(TSysUser.id.eq(3L)).execute();
        assertEquals(1, n);

        List<Map<String, Object>> rows = q.select().where(TSysUser.id.eq(3L)).execute();
        assertEquals(0, rows.size());
    }

    @Test
    void selectWithStringWhere() {
        QSysUser q = new QSysUser();
        q.insert().set("id", 10L).set("username", "u10").set("age", 10).execute();
        q.insert().set("id", 11L).set("username", "u11").set("age", 11).execute();

        List<Map<String, Object>> rows = q.select().columns("id", "username").where("id = ?", 10L).execute();
        assertFalse(rows.isEmpty());
        assertEquals(10L, rows.get(0).get("ID"));
        assertEquals("u10", rows.get(0).get("USERNAME"));
    }

    @Test
    void optimisticLock_versionColumnAndException() {
        QSysUser q = new QSysUser();
        q.insert().set("id", 80L).set("username", "ver").set("age", 1).set("revision", 0).execute();
        long n = q.update().set("username", "v1").where(TSysUser.id.eq(80L)).versionColumn("revision", 0).execute();
        assertEquals(1, n);
        List<Map<String, Object>> row = q.select().where(TSysUser.id.eq(80L)).execute();
        assertEquals(1, row.get(0).get("REVISION"));
        assertThrows(OptimisticLockException.class, () ->
                q.update().set("username", "v2").where(TSysUser.id.eq(80L)).versionColumn("revision", 0).execute());
    }

    @Test
    void pathConditions_gtLtInLike() {
        QSysUser q = new QSysUser();
        for (long i = 1L; i <= 5L; i++) {
            q.insert().set("id", i).set("username", "user" + i).set("age", (int) (i * 10)).execute();
        }
        List<Map<String, Object>> gt = q.select().where(TSysUser.id.gt(2L)).execute();
        assertEquals(3, gt.size());
        List<Map<String, Object>> in = q.select().where(TSysUser.id.in(2L, 4L)).execute();
        assertEquals(2, in.size());
        List<Map<String, Object>> like = q.select().where(TSysUser.username.startsWith("user3")).execute();
        assertEquals(1, like.size());
        assertEquals(3L, like.get(0).get("ID"));
    }

    @Test
    void whereCriteriaAndOr() {
        QSysUser q = new QSysUser();
        q.insert().set("id", 70L).set("username", "a").set("age", 10).execute();
        q.insert().set("id", 71L).set("username", "b").set("age", 10).execute();
        q.insert().set("id", 72L).set("username", "a").set("age", 20).execute();
        WhereCriteria c = WhereCriteria.and(TSysUser.username.eq("a"), TSysUser.age.eq(10));
        List<Map<String, Object>> rows = q.select().where(c).execute();
        assertEquals(1, rows.size());
        assertEquals(70L, rows.get(0).get("ID"));
    }

    @Test
    void selectMappingToEntity() {
        QSysUser q = new QSysUser();
        q.insert().set("id", 20L).set("username", "entityUser").set("age", 25).execute();

        List<SysUser> list = q.select().where(TSysUser.id.eq(20L)).executeAs(SysUser.class);
        assertFalse(list.isEmpty());
        SysUser user = list.get(0);
        assertEquals(20L, user.getId());
        assertEquals("entityUser", user.getUsername());
        assertEquals(25, user.getAge());
    }

    @Test
    void transactionCommit() {
        QSysUser q = new QSysUser();
        ExecutorContext.runInTransaction(() -> {
            q.insert().set("id", 30L).set("username", "tx1").set("age", 1).execute();
            q.insert().set("id", 31L).set("username", "tx2").set("age", 2).execute();
        });
        List<Map<String, Object>> rows = q.select().where("id IN (?, ?)", 30L, 31L).execute();
        assertEquals(2, rows.size());
    }

    @Test
    void transactionRollback() {
        QSysUser q = new QSysUser();
        assertThrows(RuntimeException.class, () ->
                ExecutorContext.runInTransaction(() -> {
                    q.insert().set("id", 40L).set("username", "rollback").set("age", 1).execute();
                    throw new RuntimeException("模拟异常，触发回滚");
                }));
        List<Map<String, Object>> rows = q.select().where(TSysUser.id.eq(40L)).execute();
        assertEquals(0, rows.size());
    }

    @Test
    void batchInsert() {
        QSysUser q = new QSysUser();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (long i = 50L; i <= 54L; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i);
            row.put("username", "batch" + i);
            row.put("age", (int) (i - 50));
            rows.add(row);
        }
        q.insert().batch(rows).execute();
        List<Map<String, Object>> list = q.select().where("id >= 50 AND id <= 54").execute();
        assertEquals(5, list.size());
    }

    @Test
    void batchInsertFromEntities() {
        QSysUser q = new QSysUser();
        List<SysUser> users = new ArrayList<>();
        for (long i = 60L; i <= 62L; i++) {
            SysUser u = new SysUser();
            u.setId(i);
            u.setUsername("entity" + i);
            u.setAge((int) (i - 59));
            users.add(u);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysUser u : users) {
            rows.add(EntityMapper.entityToMap(u));
        }
        q.insert().batch(rows).execute();
        List<SysUser> list = q.select().where("id >= 60 AND id <= 62").executeAs(SysUser.class);
        assertEquals(3, list.size());
    }

    @Test
    void toPage() {
        QSysUser q = new QSysUser();
        for (long i = 100L; i <= 124L; i++) {
            q.insert().set("id", i).set("username", "user" + i).set("age", (int) (i - 99)).execute();
        }
        Page<Map<String, Object>> p0 = q.select().columns("id", "username", "age").toPage(0, 10);
        assertEquals(10, p0.getList().size());
        assertEquals(25, p0.getTotal());
        assertEquals(0, p0.getPageIndex());
        assertEquals(10, p0.getPageSize());
        assertEquals(3, p0.getTotalPages());
        assertTrue(p0.hasNext());
        assertFalse(p0.hasPrevious());

        Page<Map<String, Object>> p2 = q.select().columns("id", "username").toPage(2, 10);
        assertEquals(5, p2.getList().size());
        assertEquals(25, p2.getTotal());
        assertEquals(2, p2.getPageIndex());
        assertFalse(p2.hasNext());
        assertTrue(p2.hasPrevious());
    }

    @Test
    void toPageAsEntity() {
        QSysUser q = new QSysUser();
        for (long i = 200L; i <= 206L; i++) {
            q.insert().set("id", i).set("username", "e" + i).set("age", 1).execute();
        }
        Page<SysUser> page = q.select().where("id >= 200").toPage(0, 3, SysUser.class);
        assertEquals(3, page.getList().size());
        assertEquals(7, page.getTotal());
        assertEquals(3, page.getTotalPages());
        assertEquals(200L, page.getList().get(0).getId());
    }

    @Test
    void observability_metricsAndSlowQuery() {
        SimpleExecutionMetrics metrics = new SimpleExecutionMetrics();
        ObservabilityConfig config = ObservabilityConfig.builder()
                .logSql(false)
                .slowQueryThresholdMs(1)
                .metrics(metrics)
                .build();
        ExecutorContext.setExecutor(new JdbcDmlExecutor(dataSource, config));

        QSysUser q = new QSysUser();
        q.insert().set("id", 300L).set("username", "obs").set("age", 1).execute();
        q.select().where(TSysUser.id.eq(300L)).execute();
        q.update().set("age", 2).where(TSysUser.id.eq(300L)).execute();
        q.delete().where(TSysUser.id.eq(300L)).execute();

        assertEquals(1, metrics.getCount("INSERT"));
        assertEquals(1, metrics.getCount("SELECT"));
        assertEquals(1, metrics.getCount("UPDATE"));
        assertEquals(1, metrics.getCount("DELETE"));
        assertTrue(metrics.getTotalCount() >= 4);
        assertTrue(metrics.getTotalDurationMs() >= 0);
    }

    @Test
    void dialect_fromJdbcUrlAndLimitOffsetSql() {
        assertTrue(Dialect.fromJdbcUrl("jdbc:h2:mem:test") instanceof LimitOffsetDialect);
        assertTrue(Dialect.fromJdbcUrl("jdbc:mysql://localhost/db") instanceof MySQLDialect);
        assertTrue(Dialect.fromJdbcUrl("jdbc:oracle:thin:@host:1521:xe") instanceof OracleDialect);
        String base = "SELECT * FROM t";
        assertEquals(base + " LIMIT 10 OFFSET 20", LimitOffsetDialect.INSTANCE.getLimitOffsetSql(base, 10, 20));
        assertEquals(base + " OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY", OracleDialect.INSTANCE.getLimitOffsetSql(base, 10, 20));
    }

    @Test
    void ddl_createTableFromEntityAndExecute() throws SQLException {
        DataSource ddlDs = H2DataSources.inMemory("ddl_test");
        ExecutorContext.setExecutor(new JdbcDmlExecutor(ddlDs));
        String sql = EntityDdlBuilder.buildCreateTableSql(SysUser.class, true);
        ExecutorContext.getExecutor().executeDdl(sql);
        QSysUser q = new QSysUser();
        q.insert().set("id", 1L).set("username", "ddlUser").set("age", 1).execute();
        List<Map<String, Object>> rows = q.select().where(TSysUser.id.eq(1L)).execute();
        assertEquals(1, rows.size());
        assertEquals("ddlUser", rows.get(0).get("USERNAME"));
    }

    @Test
    void executorContext_runWithAndMultiKey() throws SQLException {
        DataSource masterDs = H2DataSources.inMemory("master");
        DataSource slaveDs = H2DataSources.inMemory("slave");
        String ddl = EntityDdlBuilder.buildCreateTableSql(SysUser.class, true);
        try (var c1 = masterDs.getConnection(); var s1 = c1.createStatement()) {
            s1.execute(ddl);
        }
        try (var c2 = slaveDs.getConnection(); var s2 = c2.createStatement()) {
            s2.execute(ddl);
        }
        JdbcDmlExecutor master = new JdbcDmlExecutor(masterDs);
        JdbcDmlExecutor slave = new JdbcDmlExecutor(slaveDs);
        ExecutorContext.setExecutor("master", master);
        ExecutorContext.setExecutor("slave", slave);
        ExecutorContext.setCurrentKey("master");
        new QSysUser().insert().set("id", 1L).set("username", "m").set("age", 1).execute();
        assertEquals(1, new QSysUser().select().where(TSysUser.id.eq(1L)).execute().size());
        List<Map<String, Object>> onSlave = ExecutorContext.runWith(slave, () ->
                new QSysUser().select().where(TSysUser.id.eq(1L)).execute());
        assertTrue(onSlave.isEmpty());
    }
}
