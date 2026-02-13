package com.originlang.onlysql.apt;

import com.originlang.onlysql.sql.Delete;
import com.originlang.onlysql.sql.Insert;
import com.originlang.onlysql.sql.Select;
import com.originlang.onlysql.sql.Update;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

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
import java.util.List;

/**
 * 生成 QEntity：组装 DSL，extends QSuperEntity，仅提供 select/insert/update/delete 方法（无字段信息）。
 * 示例见 README-zh.md QEntity。
 */
class QEntityHandler {

    public void qEntity(TypeElement entityClass, ProcessingEnvironment env) {
        String className = "Q" + entityClass.getSimpleName();
        String pkg = env.getElementUtils().getPackageOf(entityClass).toString();
        ClassName qSuperClass = getSuperQClassName(entityClass, env);

        // MappedSuperclass 对应的 Q 类不能加 final，否则子类 Q 无法继承
        boolean isMappedSuperclass = entityClass.getAnnotation(MappedSuperclass.class) != null;
        Modifier[] classModifiers = isMappedSuperclass
                ? new Modifier[]{Modifier.PUBLIC}
                : new Modifier[]{Modifier.PUBLIC, Modifier.FINAL};
        ClassName tType = pkg.isEmpty() ? ClassName.bestGuess("T" + entityClass.getSimpleName()) : ClassName.get(pkg, "T" + entityClass.getSimpleName());
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className)
                .addModifiers(classModifiers)
                .addMethod(selectMethod(entityClass, tType))
                .addMethod(insertMethod(entityClass, pkg))
                .addMethod(updateMethod(entityClass, tType))
                .addMethod(deleteMethod(entityClass, tType));

        if (qSuperClass != null) {
            typeBuilder.superclass(qSuperClass);
        }

        TypeSpec typeSpec = typeBuilder.build();

        String packageName = env.getElementUtils().getPackageOf(entityClass).toString();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write generated class: " + e.getMessage());
        }
    }

    private ClassName getSuperQClassName(TypeElement entityClass, ProcessingEnvironment env) {
        TypeElement superClass = getSuperClassElement(entityClass, env);
        if (superClass == null) {
            return null;
        }
        // 仅当父类是 @Entity 时才继承其 Q 类（@MappedSuperclass 不生成 Q 类）
        if (superClass.getAnnotation(Entity.class) == null) {
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

    private MethodSpec selectMethod(TypeElement entityClass, ClassName tType) {
        return MethodSpec.methodBuilder("select")
                .addModifiers(Modifier.PUBLIC)
                .returns(Select.class)
                .addStatement("return $T.from($T.Table.tableName)", Select.class, tType)
                .build();
    }

    private MethodSpec insertMethod(TypeElement entityClass, String pkg) {
        String tClassName = "T" + entityClass.getSimpleName();
        ClassName tType = pkg.isEmpty() ? ClassName.bestGuess(tClassName) : ClassName.get(pkg, tClassName);
        TypeName entityType = TypeName.get(entityClass.asType());
        ParameterizedTypeName insertType = ParameterizedTypeName.get(ClassName.get(Insert.class), entityType);
        String idFieldName = findIdFieldName(entityClass);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("insert")
                .addModifiers(Modifier.PUBLIC)
                .returns(insertType);
        if (idFieldName != null) {
            builder.addStatement("return new $T($T.Table.tableName).generatedKeyColumn($T.Table.$L)", insertType, tType, tType, idFieldName);
        } else {
            builder.addStatement("return new $T($T.Table.tableName)", insertType, tType);
        }
        return builder.build();
    }

    /** 查找实体（含父类）中带 @Id 的字段名，仅返回第一个。 */
    private String findIdFieldName(TypeElement entityClass) {
        TypeElement current = entityClass;
        while (current != null && !"java.lang.Object".equals(current.getQualifiedName().toString())) {
            for (Element e : current.getEnclosedElements()) {
                if (e.getKind() == ElementKind.FIELD && e.getAnnotation(Id.class) != null) {
                    return e.getSimpleName().toString();
                }
            }
            if (current.getSuperclass().getKind() != TypeKind.DECLARED) break;
            current = (TypeElement) ((DeclaredType) current.getSuperclass()).asElement();
        }
        return null;
    }

    private MethodSpec updateMethod(TypeElement entityClass, ClassName tType) {
        return MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .returns(Update.class)
                .addStatement("return new $T($T.Table.tableName)", Update.class, tType)
                .build();
    }

    private MethodSpec deleteMethod(TypeElement entityClass, ClassName tType) {
        TypeName entityType = TypeName.get(entityClass.asType());
        ParameterizedTypeName deleteType = ParameterizedTypeName.get(ClassName.get(Delete.class), entityType);
        return MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .returns(deleteType)
                .addStatement("return new $T($T.Table.tableName)", deleteType, tType)
                .build();
    }
}
