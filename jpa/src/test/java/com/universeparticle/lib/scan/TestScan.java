package com.universeparticle.lib.scan;


import org.junit.jupiter.api.Test;

class TestScan {


    @Test
    public void testAnnotation() {
        System.out.println("测试扫描注解");
        ClassScanner scanner = new ClassScanner();
        var list = scanner.scanAnnotationClass(TestAnnotation.class, "com.universeparticle.scan");
        System.out.println(list);
    }


    @Test
    public void scanClazz() {
        System.out.println("测试扫描类");
        ClassScanner scanner = new ClassScanner();
        var list = scanner.scanClass("com.universeparticle.scan");
        System.out.println(list);
    }


    @Test
    public void scan() {
        ClassScanner scanner = new ClassScanner();
        var list = scanner.scanName("com.universeparticle.scan");
        System.out.println(list);
    }


    @Test
    public void testScan() {
        Scanner scanner = new Scanner();
        var list = scanner.scan(TestScan.class, TestAnnotation.class);
        System.out.println(list);
    }

}
