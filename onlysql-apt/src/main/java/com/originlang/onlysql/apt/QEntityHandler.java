package com.originlang.onlysql.apt;

import com.originlang.onlysql.*;
import com.palantir.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成Q类
 */
class QEntityHandler {

    public void qEntity(TypeElement entityClass, ProcessingEnvironment env) {
        // 使用JavaPoet生成代码
        String className = "Q" + entityClass.getSimpleName();

        // MethodSpec methodSpec = MethodSpec.methodBuilder("getSpecification")
        // .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        // .returns(
        // ParameterizedTypeName.get(
        // ClassName.get(Specification.class),
        // ClassName.get("", entityClass.getSimpleName().toString())
        // )
        // )
        // .addParameter(String.class, "field")
        // .addParameter(Object.class, "value")
        // .addStatement("return (root, query, cb) -> {\n" +
        // " java.util.List<$T> predicates = new $T<>();\n" +
        // " if (value != null) {\n" +
        // " predicates.add(cb.equal(root.get(field), value));\n" +
        // " }\n" +
        // " return cb.and(predicates.toArray(new $T[0]));\n" +
        // "}", Predicate.class, ArrayList.class, Predicate.class)
        // .build();

        MethodSpec stringConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "variable")
                .build();
        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(getFields(entityClass, env))
                .addMethod(stringConstructor)
                .build();

        String packageName = env.getElementUtils().getPackageOf(entityClass).toString();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write generated class: " + e.getMessage());
        }
    }

    private List<FieldSpec> getFields(TypeElement entityClass, ProcessingEnvironment env) {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        // 父类自身用 _super，父类字段通过 _super.xxxx 访问
        FieldSpec superFieldSpec = buildSuperField(entityClass, env);
        if (superFieldSpec != null) {
            fieldSpecs.add(superFieldSpec);
        }
        for (Element element : entityClass.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                System.out.println("*******************" + element.getKind() +
                        element.getSimpleName() + element.getModifiers());
                System.out.println(element.asType().toString());
                FieldSpec fieldSpec;
                String typeName = element.asType().toString();
                switch (typeName) {
                    case "java.lang.Long", "long" -> fieldSpec = numberFieldLong(element);
                    case "java.lang.Integer", "int", "java.lang.Short", "short", "java.lang.Byte", "byte" ->
                            fieldSpec = numberFieldInteger(element);
                    case "java.util.Date", "java.sql.Date", "java.sql.Timestamp", "java.time.LocalDateTime" ->
                            fieldSpec = datetimeField(element);
                    case "java.time.LocalDate" -> fieldSpec = dateField(element);
                    case "java.time.LocalTime" -> fieldSpec = timeField(element);
                    default -> fieldSpec = stringField(element);
                }
                fieldSpecs.add(fieldSpec);
            }
        }
        return fieldSpecs;
    }


    /**
     * String 字段处理成StringPath对象
     */
    private FieldSpec stringField(Element element) {
        String fieldName = element.getSimpleName().toString();
        return FieldSpec.builder(StringPath.class, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T($S)", StringPath.class, fieldName)
                .build();
    }

    /**
     * Long 字段处理成 public final NumberPath<Long> id = new NumberPath("id", Long.class);
     */
    private FieldSpec numberFieldLong(Element element) {
        String fieldName = element.getSimpleName().toString();
        return FieldSpec.builder(
                        ParameterizedTypeName.get(ClassName.get(NumberPath.class), TypeName.get(element.asType())),
                        fieldName,
                        Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T($S, $T.class)", NumberPath.class, fieldName, element.asType())
                .build();
    }

    /**
     * 其他数字类型（Integer、Short、Byte 等）处理成 NumberPath<Integer> 对象
     * 例如：public final NumberPath<Integer> count = new NumberPath("count", Integer.class);
     */
    private FieldSpec numberFieldInteger(Element element) {
        String fieldName = element.getSimpleName().toString();
        return FieldSpec.builder(
                        ParameterizedTypeName.get(ClassName.get(NumberPath.class), ClassName.get(Integer.class)),
                        fieldName,
                        Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T($S, $T.class)", NumberPath.class, fieldName, Integer.class)
                .build();
    }


    private FieldSpec datetimeField(Element element) {
        String fieldName = element.getSimpleName().toString();
        return FieldSpec.builder(DateTimePath.class, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T($S, $T.class)", DateTimePath.class, fieldName, element.asType())
                .build();
    }

    private FieldSpec dateField(Element element) {
        String fieldName = element.getSimpleName().toString();
        return FieldSpec.builder(DatePath.class, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T($S, $T.class)", DatePath.class, fieldName, element.asType())
                .build();
    }

    private FieldSpec timeField(Element element) {
        String fieldName = element.getSimpleName().toString();
        return FieldSpec.builder(TimePath.class, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T($S, $T.class)", TimePath.class, fieldName, element.asType())
                .build();
    }


    /**
     * 返回父类对应的 Q 类 ClassName，无父类或父类为 Object 时返回 null。
     */
    private ClassName getSuperQClassName(TypeElement entityClass, ProcessingEnvironment env) {
        TypeElement superClass = getSuperClassElement(entityClass, env);
        if (superClass == null) {
            return null;
        }
        String qSimpleName = "Q" + superClass.getSimpleName().toString();
        javax.lang.model.element.PackageElement pkgElement = env.getElementUtils().getPackageOf(superClass);
        String pkg = pkgElement == null ? "" : pkgElement.getQualifiedName().toString();
        if (pkg == null || pkg.isEmpty()) {
            return ClassName.bestGuess(qSimpleName);
        }
        return ClassName.get(pkg, qSimpleName);
    }

    private TypeElement getSuperClassElement(TypeElement entityClass, ProcessingEnvironment env) {
        List<? extends TypeMirror> supertypes = env.getTypeUtils().directSupertypes(entityClass.asType());
        if (supertypes.isEmpty()) {
            return null;
        }
        TypeMirror superType = supertypes.get(0);
        if (superType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        TypeElement superClass = (TypeElement) ((DeclaredType) superType).asElement();
        return "java.lang.Object".equals(superClass.getQualifiedName().toString()) ? null : superClass;
    }

    /**
     * 父类处理成 _super 字段，父类字段通过 _super.xxxx 访问。
     * 例如：public final QBaseEntity _super = new QBaseEntity("_super");
     */
    private FieldSpec buildSuperField(TypeElement entityClass, ProcessingEnvironment env) {
        ClassName qSuperClass = getSuperQClassName(entityClass, env);
        if (qSuperClass == null) {
            return null;
        }
        return FieldSpec.builder(qSuperClass, "_super", Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T($S)", qSuperClass, "_super")
                .build();
    }


}