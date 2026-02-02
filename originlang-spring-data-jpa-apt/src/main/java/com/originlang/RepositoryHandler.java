package com.originlang;

import com.palantir.javapoet.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成父级repo
 */
public class RepositoryHandler {

	public static void repo(TypeElement entityClass, ProcessingEnvironment env) {
		// 使用JavaPoet生成代码
		String className = "QRepo" + entityClass.getSimpleName();

		MethodSpec methodSpec = MethodSpec.methodBuilder("findFirstBy")
			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
			.returns(ParameterizedTypeName.get(ClassName.get(Specification.class),
					ClassName.get("", entityClass.getSimpleName().toString())))
			.addParameter(String.class, "field")
			.addParameter(Object.class, "value")
			.addStatement("return (root, query, cb) -> {\n" + "    java.util.List<$T> predicates = new $T<>();\n"
					+ "    if (value != null) {\n" + "        predicates.add(cb.equal(root.get(field), value));\n"
					+ "    }\n" + "    return cb.and(predicates.toArray(new $T[0]));\n" + "}", Predicate.class,
					ArrayList.class, Predicate.class)
			.build();

		TypeSpec typeSpec = TypeSpec.interfaceBuilder(className)
			.addModifiers(Modifier.PUBLIC)
			// .addAnnotation(Component.class)
			// .addFields(getFields(entityClass))
			.addMethod(methodSpec)
			.build();

		JavaFile javaFile = JavaFile.builder(env.getElementUtils().getPackageOf(entityClass).toString(), typeSpec)
			.build();

		try {
			javaFile.writeTo(env.getFiler());
		}
		catch (IOException e) {
			env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write generated class: " + e.getMessage());
		}
	}

	private static List<FieldSpec> getFields(TypeElement entityClass) {
		List<FieldSpec> fieldSpecs = new ArrayList<>();
		for (Element element : entityClass.getEnclosedElements()) {
			if (element.getKind() == ElementKind.FIELD) {
				// System.out.println("*************getFields:" + element);
				// System.out.println("*******************" + element.getKind() +
				// element.getSimpleName() + element.getModifiers());
				// System.out.println(element.asType().toString());
				FieldSpec fieldSpec = FieldSpec.builder(
						// TypeName.get(element.asType()),
						String.class, element.getSimpleName().toString(),
						// element.getModifiers().toArray(new Modifier[0])
						Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer("$S", element.getSimpleName().toString())
					.build();
				fieldSpecs.add(fieldSpec);
			}

		}
		return fieldSpecs;
	}

}
