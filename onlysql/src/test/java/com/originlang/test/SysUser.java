package com.originlang.test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SysUser extends BaseEntity {

    @Id
    private Long id;

    private String username;

    private Integer age;
}
