package com.originlang.onlysql;

import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
public class BaseEntity implements Serializable {
    private LocalDateTime createTime;
}
