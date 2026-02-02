package com.originlang;

import freemarker.template.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JpaTemplate {

	public static class Field {

		public String name;

		public Integer pk;

		public String type;

	}

	public static List<Field> fields = new ArrayList<>();

	static {
		Field field = new Field();
		field.name = "id";
		field.pk = 1;
		field.type = "bigint";

		fields.add(field);
	}

	public static void main() throws IOException, TemplateException {

		// 1. 创建 FreeMarker 配置
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
		// String userDir = System.getProperty("user.dir") + "/";
		String dir = JpaTemplate.class.getClassLoader().getResource("").getPath();
		String codeDir = JpaTemplate.class.getResource("").getPath();
		System.out.println(dir);
		System.out.println(codeDir);

		cfg.setDirectoryForTemplateLoading(new File(dir));
		cfg.setDefaultEncoding("UTF-8");

		// 2. 创建模板
		Template template = cfg.getTemplate("entity.ftl");

		// 3. 收集用户输入数据

		String packageName = "com.originlang";

		String className = "User";
		String entityId = "Long";
		// 4. 准备数据模型
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put("packageName", packageName);
		dataModel.put("entityName", className);
		dataModel.put("entityId", entityId);

		// 5. 输出生成的代码到文件
		FileWriter fileWriter = new FileWriter(new File(codeDir + File.separator + className + ".java"));
		template.process(dataModel, fileWriter);
		fileWriter.close();

		System.out.println("Code generated successfully!");
	}

}
