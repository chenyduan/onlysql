package com.originlang.onlysql;

import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
public class BaseEntity implements Serializable {

    private LocalDateTime createTime;


    private LocalDateTime updateTime;


    private Long createBy;


    private Long updateBy;


    private Integer revision;
}
