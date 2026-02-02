package com.universeparticle.lib.jpa.test;

import com.universeparticle.lib.jpa.CodeGenerater;
import com.universeparticle.lib.jpa.Jpo;
import com.universeparticle.lib.scan.SourceCodeScanner;
import org.junit.jupiter.api.Test;

class CodeGeneraterTest {

    /**
     * 源码
     */
    @Test
    public void javaDoc() {
        CodeGenerater codeGenerater = new CodeGenerater();
        codeGenerater.javaDoc(Jpo.class);
    }


    @Test
    public void sourceCode() {
        SourceCodeScanner codeGenerater = new SourceCodeScanner();
        var sourceCode = codeGenerater.sourceCode(Jpo.class);
        System.out.println(sourceCode);
    }


    @Test
    public void test() {
        CodeGenerater codeGenerater = new CodeGenerater();
        System.out.println(codeGenerater.camelToUnderline("User"));
        System.out.println(codeGenerater.camelToUnderline("SysUserAccount"));
    }
}
