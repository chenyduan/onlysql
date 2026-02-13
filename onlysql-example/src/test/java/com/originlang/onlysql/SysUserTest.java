package com.originlang.onlysql;

import com.originlang.onlysql.jdbc.EntityMapper;
import com.originlang.onlysql.jdbc.JdbcDmlExecutor;
import com.originlang.onlysql.jdbc.ObservabilityConfig;
import com.originlang.onlysql.jdbc.SimpleExecutionMetrics;
import com.originlang.onlysql.sql.ExecutorContext;
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
    void insertReturnsGeneratedKey() {
        QSysUser q = new QSysUser();
        // 不设置 id，由表自增生成；Q 的 insert() 已绑定 generatedKeyColumn(id)
        long key = q.insert().set("username", "autoUser").set("age", 99).execute();
        assertTrue(key >= 1, "应返回生成的主键");
        List<Map<String, Object>> rows = q.select().where(TSysUser.id.eq(key)).execute();
        assertFalse(rows.isEmpty());
        assertEquals("autoUser", rows.get(0).get("USERNAME"));
        assertEquals(99, rows.get(0).get("AGE"));
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
        long n = q.insert().batch(rows).execute();
        assertEquals(5, n);
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
        long n = q.insert().batch(rows).execute();
        assertEquals(3, n);
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
}
