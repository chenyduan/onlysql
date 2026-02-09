package com.originlang.onlysql.apt;

import com.palantir.javapoet.*;

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
 * 生成Q类
 */
 class QEntityGenerate {

	public  void qEntity(TypeElement entityClass, ProcessingEnvironment env) {
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

		TypeSpec typeSpec = TypeSpec.classBuilder(className)
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
			// .addAnnotation(Component.class)
			.addFields(getFields(entityClass))
			// .addMethod(methodSpec)
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

	private  List<FieldSpec> getFields(TypeElement entityClass) {
		List<FieldSpec> fieldSpecs = new ArrayList<>();
		for (Element element : entityClass.getEnclosedElements()) {
			if (element.getKind() == ElementKind.FIELD) { // file type
				 System.out.println("*******************" + element.getKind() + // field
				 element.getSimpleName() // id
						 + element.getModifiers() //[private]
				 );
				 System.out.println(element.asType().toString()); // java.lang.Long
				FieldSpec fieldSpec = FieldSpec.builder(
						// TypeName.get(element.asType()),
						String.class,
								element.getSimpleName().toString(),
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