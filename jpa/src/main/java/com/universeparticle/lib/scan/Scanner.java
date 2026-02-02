package com.universeparticle.lib.scan;

import java.io.File;
import java.io.FileFilter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 代码扫描器
 *
 * @author cheny
 * @since 1.0.0
 */
public class Scanner {

    List<String> classNameList = new ArrayList<>(256);


    private List token(Class clazz, Class annotationClass) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(annotationClass);
        // 获取类所在包路径 ,/D:/code/log/up-server-java/lib/scan/target/test-classes/com/universeparticle/scan/test/
        String clazzDir = URLDecoder.decode(clazz.getResource("").getPath(), StandardCharsets.UTF_8);

        // 获取目录
        File mainFileDir = new File(clazzDir);
        // 获取目录下的文件。这里我们只需要 directory 和 .class 文件,
        File[] dirOrClazzFiles = mainFileDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });


/*
        for (File file : dirOrClazzFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                doScan(dirPath + File.pathSeparator + file.getName(), pkg + "." + file.getName());
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                System.out.println("className000000" + className);
                // 添加到集合中去
                // classes.add(Class.forName(packageName + '.' +
                // className));
                // 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
//                    System.out.println(clazz.getClassLoader().loadClass(packageName + '.' + className).getName());
//                    classes.add(clazz.getClassLoader().loadClass(packageName + '.' + className));

            }
        }
*/

        return null;
    }


    /**
     * 扫描指定类所在子包的 指定注解的类
     *
     * @param clazz           指定类
     * @param annotationClass 要扫描的注解
     * @return
     */

    public List<String> scan(Class clazz, Class annotationClass) {
        String clazzPath = URLDecoder.decode(clazz.getResource("").getPath(), StandardCharsets.UTF_8);
        doScan(clazzPath, clazz.getPackage().getName());
        return classNameList;
    }


    private void doScan(String dirPath, String pkg) {

        File mainFileDir = new File(dirPath);
        File[] dirfiles = mainFileDir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(File file) {
                System.out.println("是否class文件+" + file.isDirectory() + file.getName().endsWith(".class"));
                return (file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });

        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                doScan(dirPath + File.pathSeparator + file.getName(), pkg + "." + file.getName());
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                System.out.println("className000000" + className);
                // 添加到集合中去
                // classes.add(Class.forName(packageName + '.' +
                // className));
                // 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
//                    System.out.println(clazz.getClassLoader().loadClass(packageName + '.' + className).getName());
//                    classes.add(clazz.getClassLoader().loadClass(packageName + '.' + className));
                classNameList.add(pkg + "." + className);
            }
        }
    }

}
