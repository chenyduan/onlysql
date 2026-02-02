package com.universeparticle.lib.jpa;

import lombok.Data;

import java.io.File;

/**
 * Java Persistence Object
 */
@Data
public class Jpo {
    /**
     * 源码文件
     */
    private File sourceFile;
    /**
     * 源码路径
     */
    private String sourceCodePath;

    private Class clazz;

    /**
     * 类注释
     */
    private String comment;


}
