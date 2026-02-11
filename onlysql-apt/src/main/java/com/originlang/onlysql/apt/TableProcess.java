package com.originlang.onlysql.apt;

import com.originlang.onlysql.DatePath;
import com.originlang.onlysql.DateTimePath;
import com.originlang.onlysql.NumberPath;
import com.originlang.onlysql.StringPath;
import com.originlang.onlysql.TimePath;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成 TEntity：记录表信息。表名字段放在静态内部类 Table 中；实例部分为 _super 与 Path 字段。
 */
class TableProcess {

    public void tableInfo(TypeElement entityClass, ProcessingEnvironment env) {
        String className = "T" + entityClass.getSimpleName();
        String tableName = getTableName(entityClass);
        List<FieldEntry> fieldEntries = collectFieldEntries(entityClass);
        Map<String, String> columnNames = new LinkedHashMap<>();
        for (FieldEntry e : fieldEntries) {
            columnNames.put(e.fieldName, getColumnName(e.element));
        }

        // 静态内部类 Table：表名 + 列名常量
        List<FieldSpec> tableFields = new ArrayList<>();
        tableFields.add(FieldSpec.builder(String.class, "tableName", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", tableName)
                .build());
        for (Map.Entry<String, String> e : columnNames.entrySet()) {
            tableFields.add(FieldSpec.builder(String.class, e.getKey(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", e.getValue())
                    .build());
        }
        TypeSpec tableInnerClass = TypeSpec.classBuilder("Table")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addFields(tableFields)
                .build();

        // 字段信息：_super + Path 字段
        List<FieldSpec> fields = new ArrayList<>();
        FieldSpec superField = buildTSuperField(entityClass, env);
        if (superField != null) {
            fields.add(superField);
        }
        for (FieldEntry entry : fieldEntries) {
            FieldSpec pathField = entry.fromSuper
                    ? pathFieldFromSuper(entry.element)
                    : buildPathField(entry.element, env);
            fields.add(pathField);
        }

        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addType(tableInnerClass)
                .addFields(fields)
                .build();

        JavaFile javaFile = JavaFile.builder(env.getElementUtils().getPackageOf(entityClass).toString(), typeSpec)
                .build();

        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write TEntity: " + e.getMessage());
        }
    }

    private String getTableName(TypeElement entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && table.name() != null && !table.name().isEmpty()) {
            return table.name();
        }
        return camelToSnake(entityClass.getSimpleName().toString());
    }

    private String getColumnName(Element field) {
        Column col = field.getAnnotation(Column.class);
        if (col != null && col.name() != null && !col.name().isEmpty()) {
            return col.name();
        }
        return camelToSnake(field.getSimpleName().toString());
    }

    private static String camelToSnake(String camel) {
        if (camel == null || camel.isEmpty()) return camel;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static final class FieldEntry {
        final Element element;
        final String fieldName;
        final boolean fromSuper;

        FieldEntry(Element element, String fieldName, boolean fromSuper) {
            this.element = element;
            this.fieldName = fieldName;
            this.fromSuper = fromSuper;
        }
    }

    private void collectFieldEntries(TypeElement typeElement, List<FieldEntry> out, boolean fromSuper) {
        TypeMirror superType = typeElement.getSuperclass();
        if (superType.getKind() == TypeKind.DECLARED) {
            DeclaredType declared = (DeclaredType) superType;
            if (declared.asElement() instanceof TypeElement superElement) {
                String superName = superElement.getQualifiedName().toString();
                if (!"java.lang.Object".equals(superName)) {
                    collectFieldEntries(superElement, out, true);
                }
            }
        }
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                out.add(new FieldEntry(element, element.getSimpleName().toString(), fromSuper));
            }
        }
    }

    private List<FieldEntry> collectFieldEntries(TypeElement entityClass) {
        List<FieldEntry> list = new ArrayList<>();
        collectFieldEntries(entityClass, list, false);
        return list;
    }

    private FieldSpec buildTSuperField(TypeElement entityClass, ProcessingEnvironment env) {
        TypeElement superElement = getSuperTypeElement(entityClass);
        if (superElement == null) return null;
        String tSuperName = "T" + superElement.getSimpleName();
        String pkg = env.getElementUtils().getPackageOf(superElement).toString();
        ClassName tSuperClass = pkg.isEmpty() ? ClassName.bestGuess(tSuperName) : ClassName.get(pkg, tSuperName);
        return FieldSpec.builder(tSuperClass, "_super", Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T()", tSuperClass)
                .build();
    }

    private TypeElement getSuperTypeElement(TypeElement entityClass) {
        TypeMirror superType = entityClass.getSuperclass();
        if (superType.getKind() != TypeKind.DECLARED) return null;
        TypeElement superElement = (TypeElement) ((DeclaredType) superType).asElement();
        return "java.lang.Object".equals(superElement.getQualifiedName().toString()) ? null : superElement;
    }

    private FieldSpec pathFieldFromSuper(Element element) {
        String fieldName = element.getSimpleName().toString();
        String typeName = element.asType().toString();
        TypeName pathType;
        switch (typeName) {
            case "java.lang.Long", "long" ->
                pathType = com.palantir.javapoet.ParameterizedTypeName.get(ClassName.get(NumberPath.class), TypeName.get(element.asType()));
            case "java.lang.Integer", "int", "java.lang.Short", "short", "java.lang.Byte", "byte" ->
                pathType = com.palantir.javapoet.ParameterizedTypeName.get(ClassName.get(NumberPath.class), ClassName.get(Integer.class));
            case "java.util.Date", "java.sql.Date", "java.sql.Timestamp", "java.time.LocalDateTime" ->
                pathType = ClassName.get(DateTimePath.class);
            case "java.time.LocalDate" -> pathType = ClassName.get(DatePath.class);
            case "java.time.LocalTime" -> pathType = ClassName.get(TimePath.class);
            default -> pathType = ClassName.get(StringPath.class);
        }
        return FieldSpec.builder(pathType, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                .initializer("_super.$L", fieldName)
                .build();
    }

    private FieldSpec buildPathField(Element element, ProcessingEnvironment env) {
        String fieldName = element.getSimpleName().toString();
        String typeName = element.asType().toString();
        switch (typeName) {
            case "java.lang.Long", "long" -> {
                return FieldSpec.builder(
                                com.palantir.javapoet.ParameterizedTypeName.get(ClassName.get(NumberPath.class), TypeName.get(element.asType())),
                                fieldName, Modifier.PUBLIC, Modifier.FINAL)
                        .initializer("new $T($S, $T.class)", NumberPath.class, fieldName, element.asType())
                        .build();
            }
            case "java.lang.Integer", "int", "java.lang.Short", "short", "java.lang.Byte", "byte" ->{
                return FieldSpec.builder(
                                com.palantir.javapoet.ParameterizedTypeName.get(ClassName.get(NumberPath.class), ClassName.get(Integer.class)),
                                fieldName, Modifier.PUBLIC, Modifier.FINAL)
                        .initializer("new $T($S, $T.class)", NumberPath.class, fieldName, Integer.class)
                        .build();
            }

            case "java.util.Date", "java.sql.Date", "java.sql.Timestamp", "java.time.LocalDateTime" ->{
                return FieldSpec.builder(DateTimePath.class, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                        .initializer("new $T($S, $T.class)", DateTimePath.class, fieldName, element.asType())
                        .build();
            }

            case "java.time.LocalDate" ->{
                return FieldSpec.builder(DatePath.class, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                        .initializer("new $T($S, $T.class)", DatePath.class, fieldName, element.asType())
                        .build();
            }

            case "java.time.LocalTime" ->
            { return FieldSpec.builder(TimePath.class, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                        .initializer("new $T($S, $T.class)", TimePath.class, fieldName, element.asType())
                        .build();}
            default ->{
                return FieldSpec.builder(StringPath.class, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                        .initializer("new $T($S)", StringPath.class, fieldName)
                        .build();
            }

        }
    }



}
