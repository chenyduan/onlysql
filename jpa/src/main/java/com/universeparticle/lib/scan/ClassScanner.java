package com.universeparticle.lib.scan;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * 类扫描器
 *
 * @author cheny
 * @since 1.0.0
 */
public class ClassScanner {


    public List<Class<?>> scanAnnotationClass(Class<? extends Annotation> annotationClass, String... packages) {
        return this.scanClass(packages)
                .stream().filter(clazz -> {
                    Annotation annotation = clazz.getAnnotation(annotationClass);
                    return annotation != null;
                }).toList();

    }


    /**
     * 扫描指定包下的类,返回Class字节码
     *
     * @param packages 包名
     * @return 类名列表, 包含包名
     */
    public List<Class<?>> scanClass(String... packages) {
        List<Class<?>> list = new ArrayList<>();
        var names = scanName(packages);
        for (String name : names) {
            try {
                list.add(Class.forName(name));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    /**
     * 扫描指定包下的类
     *
     * @param packages 包名
     * @return 类名列表, 包含包名
     */

    public List<String> scanName(String... packages) {
        List<String> list = new ArrayList<>();
        for (String pkg : packages) {
            list.addAll(scanName(pkg));
        }
        return list;
    }


    /**
     * 扫描指定包下的类
     *
     * @param pkg 包名
     * @return 类名列表, 包含包名
     * @since 1.0.0
     */

    public List<String> scanName(String pkg) {
        String packagePath = pkg.replace(".", "/");
        List<String> classNames = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String packageDirectory = classLoader.getResource(packagePath).getFile();
            File directory = new File(packageDirectory);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = pkg + "." + file.getName().replaceAll(".class$", "");
                            classNames.add(className);
                        } else if (file.isDirectory()) {
                            // 递归
                            classNames.addAll(scanName(pkg + "." + file.getName()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classNames;
    }


}
