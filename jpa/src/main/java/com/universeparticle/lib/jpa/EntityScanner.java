package com.universeparticle.lib.jpa;


import com.universeparticle.lib.scan.ClassScanner;
import com.universeparticle.lib.scan.SourceCodeScanner;
import net.bytebuddy.ByteBuddy;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 扫描实体类,被@Entity注解的类
 *
 * @link jakarta.persistence.Entity
 */
public class EntityScanner {


    /**
     * 扫描包下的类，返回JPO对象
     */
    public List<Jpo> scanJPO(String... pkgs) {
        List<Jpo> jpos = new java.util.ArrayList<>();
        var clazz = scan(pkgs);
        for (Class aClass : clazz) {
            var jpo = new Jpo();
            jpo.setClazz(aClass);
            var sourceCodeScanner = new SourceCodeScanner();
            var sourceFile = sourceCodeScanner.sourceCode(aClass);
            jpo.setSourceCodePath(sourceFile.getPath());
            jpo.setSourceFile(sourceFile);
        }
        return jpos;
    }

    /**
     * 获取指定类的源码
     *
     * @param clazz 类
     * @return JPO
     */
    public Jpo getJpo(Class clazz) {
        var jpo = new Jpo();
        jpo.setClazz(clazz);
        var sourceCodeScanner = new SourceCodeScanner();
        var sourceFile = sourceCodeScanner.sourceCode(clazz);
        jpo.setSourceFile(sourceFile);
        jpo.setSourceCodePath(sourceFile.getPath());
        return jpo;
    }


    /**
     * 扫描指定包下的类,且被指定注解标记的类
     *
     * @param pkgs 包名
     * @return clazz
     */
    public List<Class<?>> scan(String... pkgs) {
        var scanner = new ClassScanner();
        return scanner.scanClass(pkgs);
    }


    /**
     * 生成实体类
     */
    void gen() {
        var obj = new ByteBuddy()
                .subclass(Object.class)
                .name("")
                .make()
                .load(ClassLoader.getSystemClassLoader())
                .getLoaded()
//               .getDeclaredConstructors().newInstance()
                ;
    }

    /**
     * output file
     */
    void output() throws IOException {
        new ByteBuddy()
                .subclass(Object.class)
                .name("com.")
                .make()
                .saveIn(new File("D:\\"));
    }


}
