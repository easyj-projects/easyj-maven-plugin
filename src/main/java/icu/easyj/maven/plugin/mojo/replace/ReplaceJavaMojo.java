/*
 * Copyright 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package icu.easyj.maven.plugin.mojo.replace;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import icu.easyj.maven.plugin.mojo.AbstractEasyjMojo;
import icu.easyj.maven.plugin.mojo.utils.IOUtils;
import icu.easyj.maven.plugin.mojo.utils.ObjectUtils;
import icu.easyj.maven.plugin.mojo.utils.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * *.java 文件 占位符替换 的 Goal
 *
 * @author wangliang181230
 * @since 1.0.4
 */
@Mojo(name = "replace-java", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class ReplaceJavaMojo extends AbstractEasyjMojo {

	@Parameter
	private Set<String> mainPaths;
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/easyj-maven-plugin_replace-java/java")
	private File generatedSourcesDirectory;

	@Parameter
	private Set<String> testPaths;
	@Parameter(defaultValue = "${project.build.directory}/generated-test-sources/easyj-maven-plugin_replace-java/java")
	private File generatedTestSourcesDirectory;

	@Parameter
	private Map<String, String> placeholders;


	@Override
	public void execute() throws MojoExecutionException {
		if (ObjectUtils.isEmpty(this.mainPaths) && ObjectUtils.isEmpty(this.testPaths)) {
			this.info("Skip, because 'mainPaths' and 'testPaths' is empty.");
			return;
		}

		// 处理placeholders
		if (placeholders == null) {
			placeholders = new HashMap<>();
		}
		placeholders.put("project.groupId", project.getGroupId());
		placeholders.put("project.artifactId", project.getArtifactId());
		placeholders.put("project.version", project.getVersion());
		placeholders.put("revision", project.getVersion());

		try {
			if (this.replaceFiles(this.mainPaths, "src/main/java/", this.generatedSourcesDirectory)) {
				project.addCompileSourceRoot(this.generatedSourcesDirectory.getPath());
			}
			if (this.replaceFiles(this.testPaths, "src/test/java/", this.generatedTestSourcesDirectory)) {
				project.addTestCompileSourceRoot(this.generatedTestSourcesDirectory.getPath());
			}
		} catch (IOException e) {
			throw new RuntimeException("Replace placeholder failed", e);
		}
	}

	private boolean replaceFiles(Set<String> paths, String basePath, File generatedSourcesDirectory) throws IOException {
		if (ObjectUtils.isEmpty(paths)) {
			return false;
		}

		boolean needScanGeneratedSourcesDirectory = false;
		for (String path : paths) {
			File file = new File(this.outputDirectory, basePath + path);
			if (!file.exists()) {
				continue;
			}

			if (file.isDirectory()) {
				// 暂时不支持目录
				continue;
			}

			// 获取文件内容，并替换占位符
			String text = this.readAndReplacePlaceholder(file);

			// 生成文件
			if (StringUtils.isNotEmpty(text)) {
				String generatedFilePath = generatedSourcesDirectory.getPath() + "/" + path.substring(0, path.indexOf(".java") + ".java".length());
				File generatedFile = new File(generatedFilePath);

				// 生成目录
				generatedFile.getParentFile().mkdirs();

				// 创建文件
				IOUtils.createFile(generatedFile, text);
				this.info("Generate java file: %s -> %s", file.getName(), generatedFile.getPath());

				needScanGeneratedSourcesDirectory = true;
			}
		}

		return needScanGeneratedSourcesDirectory;
	}


	private String readAndReplacePlaceholder(File file) throws IOException {
		StringBuilder sb = new StringBuilder();

		String line;
		try (Scanner sc = new Scanner(file)) {
			while (sc.hasNextLine()) {
				line = sc.nextLine();

				// 替换占位符
				for (Map.Entry<String, String> entry : this.placeholders.entrySet()) {
					String placeholder = "${" + entry.getKey() + "}";
					if (line.contains(placeholder)) {
						line = line.replace(placeholder, entry.getValue());
					}
				}

				// 拼接文件
				sb.append(line).append(IOUtils.LINE_SEPARATOR);
			}
		}

		return sb.toString().trim();
	}
}
