package com.originlang.onlysql.apt;

import com.google.auto.service.AutoService;
import com.palantir.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * jakarta.persistence.Entity 处理,生成 Specification
 */
@AutoService(Processor.class)
// 要处理的注解
@SupportedAnnotationTypes(value = {"jakarta.persistence.Entity", "jakarta.persistence.MappedSuperclass"})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class QEntityProcess extends AbstractProcessor {

    QEntityGenerate qEntityGenerate = new QEntityGenerate();
    RecordGenerate recordGenerate = new RecordGenerate();
    TableProcess tableProcess = new TableProcess();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {


        for (TypeElement annotation : annotations) {
            // 获取泛型参数
            List<? extends TypeParameterElement> typeParameters = annotation.getTypeParameters();
            for (TypeParameterElement typeParameter : typeParameters) {
                System.out.println("*****************typeParameter:" + typeParameter);
            }
            // 获取注解的元素
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : annotatedElements) {
                if (element.getKind() == ElementKind.CLASS) {
                    qEntityGenerate.qEntity((TypeElement) element, processingEnv);
                    recordGenerate.rEntity((TypeElement) element, processingEnv);
                    tableProcess.tableInfo((TypeElement) element, processingEnv);
//                    generateSpecificationClass((TypeElement) element, processingEnv);
//                    // ConditionProcessHandler.condition((TypeElement) element,
//                    // processingEnv);
//                    // QEntityGenerate.qEntity((TypeElement) element, processingEnv);
//                    // RepositoryHandler.repo((TypeElement) element, processingEnv);
                }
            }
        }
        return false;
    }

    private void generateSpecificationClass(TypeElement entityClass, ProcessingEnvironment env) {
        // 使用JavaPoet生成代码
        String className = entityClass.getSimpleName() + "Specification";
        System.out.println("*******************className:" + className);
        // 构造方法
        MethodSpec constrMethod = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("this.predicates = new $T()", ArrayList.class)
                .build();

        MethodSpec builder = this.builder(entityClass, env, className);


        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                // 属性
                .addField(staticFieldSpec(entityClass, env))
                // .recordConstructor(constrMethod)
                .addMethods(List.of(constrMethod, builder))
                .build();

        JavaFile javaFile = JavaFile.builder(env.getElementUtils().getPackageOf(entityClass).toString(), typeSpec)
                .build();

        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write generated class: " + e.getMessage());
        }
    }

    public FieldSpec staticFieldSpec(TypeElement entityClass, ProcessingEnvironment env) {
        FieldSpec fieldSpec = FieldSpec.builder(
                        // TypeName.get(element.asType()),
                        List.class, "predicates",
                        // element.getModifiers().toArray(new Modifier[0])
                        Modifier.PRIVATE)// .initializer("$S",element.getSimpleName().toString())
                .build();
        return fieldSpec;
    }

    public MethodSpec builder(TypeElement entityClass, ProcessingEnvironment env, String className) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                // 返回类型 SysUserRoleSpecificationBuilder
                .returns(ClassName.get("", className))
                .addStatement(" return new $T()  ", ClassName.get("", className))
                .build();
        return methodSpec;
    }

//	private MethodSpec build(TypeElement entityClass, ProcessingEnvironment env) {
//		MethodSpec methodSpec = MethodSpec.methodBuilder("build")
//			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//			.returns(ParameterizedTypeName.get(ClassName.get(Specification.class),
//					ClassName.get("", entityClass.getSimpleName().toString())))
//			.addParameter(String.class, "field")
//			.addParameter(Object.class, "value")
//			.addStatement("return (root, query, cb) -> {\n" + "    java.util.List<$T> predicates = new $T<>();\n"
//					+ "    if (value != null) {\n" + "        predicates.add(cb.equal(root.get(field), value));\n"
//					+ "    }\n" + "    return cb.and(predicates.toArray(new $T[0]));\n" + "}", Predicate.class,
//					ArrayList.class, Predicate.class)
//			.build();
//		return methodSpec;
//
//	}

    public static List list;

    public QEntityProcess() {
        this.list = new ArrayList<>();
    }

}