package com.originlang.onlysql;

import org.junit.jupiter.api.Test;

public class SysUserTest {

    @Test
    public void t1() {
        QSysUser qSysUser = new QSysUser();
        long id = qSysUser.insert().set("id", 1).execute();
        System.out.println(id);
    }
}
