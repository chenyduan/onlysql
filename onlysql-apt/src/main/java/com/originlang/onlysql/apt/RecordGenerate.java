package com.originlang.onlysql.apt;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
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

/**
 * 生成 REntity：对应数据库表字段的 record，将当前 Entity 和 SuperEntity 的字段统一归纳。
 * 示例：public record RSysUser(Long id, LocalDateTime createTime) {}
 */
class RecordGenerate {

    public void rEntity(TypeElement entityClass, ProcessingEnvironment env) {
        String className = "R" + entityClass.getSimpleName();
        List<RecordComponent> components = getRecordComponents(entityClass);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
        for (RecordComponent comp : components) {
            constructorBuilder.addParameter(comp.type, comp.name);
        }
        MethodSpec recordConstructor = constructorBuilder.build();

        TypeSpec typeSpec = TypeSpec.recordBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .recordConstructor(recordConstructor)
                .build();

        JavaFile javaFile = JavaFile.builder(env.getElementUtils().getPackageOf(entityClass).toString(), typeSpec)
                .build();

        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write generated class: " + e.getMessage());
        }
    }

    /**
     * 收集当前实体及其父类（含 MappedSuperclass）的所有字段，先父类后子类，作为 record 组件。
     */
    private List<RecordComponent> getRecordComponents(TypeElement entityClass) {
        List<RecordComponent> components = new ArrayList<>();
        collectFieldsFromHierarchy(entityClass, components);
        return components;
    }

    private void collectFieldsFromHierarchy(TypeElement typeElement, List<RecordComponent> out) {
        TypeMirror superType = typeElement.getSuperclass();
        if (superType.getKind() == TypeKind.DECLARED) {
            DeclaredType declared = (DeclaredType) superType;
            if (declared.asElement() instanceof TypeElement superElement) {
                String superName = superElement.getQualifiedName().toString();
                if (!"java.lang.Object".equals(superName)) {
                    collectFieldsFromHierarchy(superElement, out);
                }
            }
        }
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                out.add(new RecordComponent(
                        TypeName.get(element.asType()),
                        element.getSimpleName().toString()));
            }
        }
    }

    private static final class RecordComponent {
        final TypeName type;
        final String name;

        RecordComponent(TypeName type, String name) {
            this.type = type;
            this.name = name;
        }
    }
}
