package com.universeparticle.lib.jpa;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.universeparticle.lib.jpa.database.Table;
import com.universeparticle.lib.jpa.database.TableColumn;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * 代码生成
 */
public class CodeGenerater {


    /**
     * 生成表定义
     */
    public Table toTable(Class clazz) {
        // 获取类上的注解
        var entity = clazz.getAnnotation(jakarta.persistence.Entity.class);
        if (entity == null) {
            return null;
        }
        var tableName = getTableName(clazz);

        Table table = new Table(clazz);
        table.setName(tableName);
        // 获取类上的文档注释
        var t = javaDoc(clazz);
        table.setComment(t.getComment());
        table.setColumnComments(t.getColumnComments());


        List<TableColumn> columns = table.getColumns();
        // 获取字段
        var fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            TableColumn tableColumn = new TableColumn();
            // 获取字段名
            var fieldName = field.getName();
            // 字段名转换为列名,驼峰转下划线
            var columnName = camelToUnderline(fieldName);
            tableColumn.setName(columnName);
            // 获取字段类型
            var fieldType = field.getType();
            // 转换为数据库类型
            var columnType = convertToColumnType(fieldType);
            tableColumn.setType(columnType);
            // 获取字段注解
            var id = field.getAnnotation(jakarta.persistence.Id.class);
            if (id != null) {
                tableColumn.setPrimaryKey(true);
                tableColumn.setAutoIncrement(true);
            }

            var column = field.getAnnotation(jakarta.persistence.Column.class);
            if (column != null) {

                // 获取字段长度
                var columnLength = column.length();
                tableColumn.setLength(columnLength);
                // 是否可空
                var columnNullable = column.nullable();
                tableColumn.setNullable(columnNullable);
            }
            columns.add(tableColumn);
        }
        return table;
    }


    /**
     * 获取类上的文档注释
     */
    public Table javaDoc(Class clazz) {
        try {
            Table table = new Table(clazz);
            // 获取源码
            EntityScanner entityScanner = new EntityScanner();
            var jpo = entityScanner.getJpo(clazz);
            String sourceCode = readFile(jpo.getSourceFile().toPath());
            // 解析源代码
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> pr = javaParser.parse(sourceCode);
            // 创建一个访问者
            CommentVisitor visitor = new CommentVisitor();
            // 访问者访问源代码
            var res = pr.getResult();
            CompilationUnit cu = res.get();
            visitor.visit(cu, null);

            table.setComment(visitor.getClassComment());
            table.setColumnComments(visitor.getFieldCommentMap());
            return table;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static class CommentVisitor extends VoidVisitorAdapter<Void> {

        private String classComment;

        private Map<String, String> fieldCommentMap = new HashMap<>();

        public String getClassComment() {
            return classComment;
        }

        public Map<String, String> getFieldCommentMap() {
            return fieldCommentMap;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration cl, Void arg) {
            super.visit(cl, arg);

            // 获取类上的注释
            Optional<Comment> comment = cl.getComment();
            // 单行注释
            if (comment.isPresent() && comment.get() instanceof LineComment lineComment) {
                String commentText = lineComment.getContent();
                classComment = commentText;
            } else if (comment.isPresent() && comment.get() instanceof JavadocComment javadoc) {
                // 文档注释
                String commentText = javadoc.getContent();
                classComment = commentText;

            } else if (comment.isPresent() && comment.get() instanceof BlockComment blockComment) {
                // 多行注释
                String commentText = blockComment.getContent();
                classComment = commentText;
            }


            // 获取字段的注释
            for (FieldDeclaration field : cl.getFields()) {
                Optional<Comment> fieldComment = field.getComment();
                String fieldName = field.getVariables().get(0).getNameAsString();
                if (!fieldComment.isPresent()) {
                    continue;
                }
                if (fieldComment.get() instanceof JavadocComment javadoc) {
                    String commentText = javadoc.getContent();

                    System.out.println("Field: " + fieldName);
                    System.out.println("Comment: " + commentText);
                    fieldCommentMap.put(fieldName, commentText);
                } else if (fieldComment.get() instanceof BlockComment blockComment) {
                    String commentText = blockComment.getContent();
                    System.out.println("Field: " + fieldName);
                    System.out.println("Comment: " + commentText);
                    fieldCommentMap.put(fieldName, commentText);
                } else if (fieldComment.get() instanceof LineComment lineComment) {
                    String commentText = lineComment.getContent();
                    System.out.println("Field: " + fieldName);
                    System.out.println("Comment: " + commentText);
                    fieldCommentMap.put(fieldName, commentText);
                } else {
                    System.out.println("未知注释类型");
                }


            }
        }
    }


    private static String readFile(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取表名
     */
    public String getTableName(Class clazz) {
        // 获取类名
        var className = clazz.getSimpleName();
        // 类名转换为表名,驼峰转下划线
        var tableName = camelToUnderline(className);
        return tableName;
    }


    /**
     * 转换为数据库类型
     */
    public String convertToColumnType(Class clazz) {
        // 获取Java类型
        var typeName = clazz.getTypeName();
        // 转换为数据库类型
        var columnType = "";
        switch (typeName) {
            case "java.lang.String":
                columnType = "varchar";
                break;
            case "java.lang.Integer":
                columnType = "int";
                break;
            case "java.lang.Long":
                columnType = "bigint";
                break;
            case "java.lang.Float":
                columnType = "float";
                break;
            case "java.lang.Double":
                columnType = "double";
                break;
            case "java.lang.Boolean":
                columnType = "boolean";
                break;
            case "java.util.Date":
                columnType = "datetime";
                break;
            case "java.time.LocalDateTime":
                columnType = "datetime";
                break;
            case "java.time.LocalDate":
                columnType = "date";
                break;
            case "java.time.LocalTime":
                columnType = "time";
                break;
            case "java.math.BigDecimal":
                columnType = "decimal";
                break;
            case "java.math.BigInteger":
                columnType = "bigint";
                break;
            case "java.sql.Timestamp":
                columnType = "timestamp";
                break;
            case "java.sql.Date":
                columnType = "date";
                break;
            case "java.sql.Time":
                columnType = "time";
                break;
            case "java.sql.Blob":
                columnType = "blob";
                break;
            case "java.sql.Clob":
                columnType = "clob";
                break;
            case "java.sql.Array":
                columnType = "array";
                break;
            case "java.sql.Ref":
                columnType = "ref";
                break;
            case "java.sql.Struct":
                columnType = "struct";
                break;
            case "java.sql.RowId":
                columnType = "rowid";
                break;
            case "java.sql.NClob":
                columnType = "nclob";
                break;

        }
        return columnType;
    }


    /**
     * 驼峰转下划线
     */
    public String camelToUnderline(String str) {
        var sb = new StringBuilder();
        for (int i = 0; i < str.length();
             i++) {
            var c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i != 0) {
                    sb.append("_");
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


}
