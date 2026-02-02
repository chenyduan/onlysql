//package com.originlang.onlysql.apt;
//
//import com.palantir.javapoet.*;
//import jakarta.persistence.criteria.Predicate;
//import org.springframework.data.jpa.domain.Specification;
//
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.TypeElement;
//import javax.tools.Diagnostic;
//import java.io.IOException;
//import java.util.ArrayList;
//
///**
// * jakarta.persistence.Entity 处理,生成 条件类
// */
//public class ConditionProcessHandler {
//
//	public static void condition(TypeElement entityClass, ProcessingEnvironment env) {
//		// 使用JavaPoet生成代码
//		String className = entityClass.getSimpleName() + "Condition";
//
//		MethodSpec methodSpec = MethodSpec.methodBuilder("getSpecification")
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
//
//		TypeSpec typeSpec = TypeSpec.classBuilder(className)
//			.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//			// .addAnnotation(Component.class)
//			.addMethod(methodSpec)
//			.build();
//
//		JavaFile javaFile = JavaFile.builder(env.getElementUtils().getPackageOf(entityClass).toString(), typeSpec)
//			.build();
//
//		try {
//			javaFile.writeTo(env.getFiler());
//		}
//		catch (IOException e) {
//			env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write generated class: " + e.getMessage());
//		}
//	}
//
//}