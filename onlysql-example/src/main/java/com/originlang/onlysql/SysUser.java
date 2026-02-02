package com.originlang.onlysql;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SysUser {

    @Id
    private Long id;

    private String username;

    private Integer age;
}
