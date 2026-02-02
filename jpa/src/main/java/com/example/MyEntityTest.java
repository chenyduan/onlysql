package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;


/**
 * 测试实体
 * 多行注释
 * 多行注释
 *
 * @since 1.0.0
 */
@Entity
public
class MyEntityTest {
    /**
     * 主键
     */
    @Id
    private Long id;
    /**
     * 名称
     */
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    private String password;

    private Integer age;

    private LocalDateTime birthday;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
