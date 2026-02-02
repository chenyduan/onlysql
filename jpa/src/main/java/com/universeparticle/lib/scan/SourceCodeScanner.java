package com.universeparticle.lib.scan;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 源码扫描器
 */
public class SourceCodeScanner {

    /**
     * 获取指定类的源码
     */
    public File sourceCode(Class clazz) {
        String dir = URLDecoder.decode(clazz.getResource("").getPath(), StandardCharsets.UTF_8);
        String codeDir = dir.split("target")[0] + "src/main/java/";
        // 包名转换为目录
        String sourceFileDir = codeDir + clazz.getPackageName().replace(".", "/") + "/" + clazz.getSimpleName() + ".java";
        // 递归扫描目录
        File sourceFile = new File(sourceFileDir);
        if (sourceFile.exists() && sourceFile.isFile()) {
            return sourceFile;
        }
        throw new RuntimeException("source code not found [ " + clazz.getName() + " ]");
    }


    /**
     * 扫描指定包下的源码路径
     *
     */
/*    public List<String> sourceCode(String pkg) {
        List<String> codeList = new java.util.ArrayList<>();
            // todo 这里如果maven在子目录下会有问题。
        String dir = URLDecoder.decode(SourceCode.class.getResource("").getPath(), StandardCharsets.UTF_8);
        String codeDir = dir.split("target")[0] + "src/main/java/";
        // 包名转换为目录
        String pkgDir = codeDir + pkg.replace(".", "/");
        // 递归扫描目录
        File pkgFile = new File(pkgDir);
        if (pkgFile.exists() && pkgFile.isDirectory()) {
            File[] files = pkgFile.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归扫描子目录
                    codeList.addAll(sourceCode(pkg + "." + file.getName()));
                } else {
                    codeList.add(pkg + "." + file.getName().replace(".java", ""));
                }
            }
        }

        return codeList;
    }*/


}
