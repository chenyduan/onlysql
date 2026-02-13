package com.originlang.onlysql.apt;

import com.originlang.onlysql.PathWithBuilder;
import com.originlang.onlysql.sql.WhereCriteriaBuilder;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 为每个 @Entity 生成 TEntityWhere，用于链式 where(TSysUser.id.eq(2L).name.eq("admin"))。
 */
class TEntityWhereHandler {

    public void generate(TypeElement entityClass, ProcessingEnvironment env) {
        String pkg = env.getElementUtils().getPackageOf(entityClass).toString();
        String entitySimple = entityClass.getSimpleName().toString();
        String whereClassName = "T" + entitySimple + "Where";
        ClassName tType = pkg.isEmpty() ? ClassName.bestGuess("T" + entitySimple) : ClassName.get(pkg, "T" + entitySimple);
        ClassName whereType = pkg.isEmpty() ? ClassName.bestGuess(whereClassName) : ClassName.get(pkg, whereClassName);

        List<FieldEntry> fieldEntries = collectFieldEntries(entityClass);

        // conditions list
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(whereClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(WhereCriteriaBuilder.class)
                .addField(
                        com.palantir.javapoet.FieldSpec.builder(
                                ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(Object[].class)),
                                "conditions",
                                Modifier.PRIVATE, Modifier.FINAL)
                                .initializer("new $T<>()", ArrayList.class)
                                .build())
                .addField(
                        com.palantir.javapoet.FieldSpec.builder(tType, "t", Modifier.PRIVATE, Modifier.FINAL)
                                .initializer("new $T()", tType)
                                .build());

        // constructor private
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);
        typeBuilder.addMethod(constructor.build());

        // static of(String col, Object val)
        MethodSpec ofMethod = MethodSpec.methodBuilder("of")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(whereType)
                .addParameter(String.class, "col")
                .addParameter(Object.class, "val")
                .addStatement("$T w = new $T()", whereType, whereType)
                .addStatement("w.conditions.add(new $T[]{col, val})", Object.class)
                .addStatement("return w")
                .build();
        typeBuilder.addMethod(ofMethod);

        // addCondition
        MethodSpec addCondition = MethodSpec.methodBuilder("addCondition")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(String.class, "column")
                .addParameter(Object.class, "value")
                .addStatement("conditions.add(new $T[]{column, value})", Object.class)
                .build();
        typeBuilder.addMethod(addCondition);

        // getWhereExpr
        MethodSpec getWhereExpr = MethodSpec.methodBuilder("getWhereExpr")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String.class)
                .addStatement("return conditions.stream().map(c -> ($T)c[0] + \" = ?\").collect($T.joining(\" AND \"))", String.class, Collectors.class)
                .build();
        typeBuilder.addMethod(getWhereExpr);

        // getWhereParams
        MethodSpec getWhereParams = MethodSpec.methodBuilder("getWhereParams")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Object.class)))
                .addStatement("return conditions.stream().map(c -> c[1]).collect($T.toList())", Collectors.class)
                .build();
        typeBuilder.addMethod(getWhereParams);

        // path methods: id() return new PathWithBuilder(this, t.id); 含继承字段以便链式
        for (FieldEntry entry : fieldEntries) {
            String fieldName = entry.element.getSimpleName().toString();
            MethodSpec pathMethod = MethodSpec.methodBuilder(fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(PathWithBuilder.class)
                    .addStatement("return new $T(this, t.$L)", PathWithBuilder.class, fieldName)
                    .build();
            typeBuilder.addMethod(pathMethod);
        }

        TypeSpec typeSpec = typeBuilder.build();
        JavaFile javaFile = JavaFile.builder(pkg, typeSpec).build();
        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write " + whereClassName + ": " + e.getMessage());
        }
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
}
