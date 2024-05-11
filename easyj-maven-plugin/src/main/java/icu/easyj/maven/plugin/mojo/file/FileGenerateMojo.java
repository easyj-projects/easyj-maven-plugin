/*
 * Copyright 2021-2024 the original author or authors.
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
package icu.easyj.maven.plugin.mojo.file;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import icu.easyj.maven.plugin.mojo.AbstractEasyjMojo;
import icu.easyj.maven.plugin.mojo.utils.IOUtils;
import icu.easyj.maven.plugin.mojo.utils.MatchUtils;
import icu.easyj.maven.plugin.mojo.utils.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 根据配置在项目目录下，生成文件
 *
 * @author wangliang181230
 * @since 1.1.6
 */
@Mojo(name = "file-generate", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class FileGenerateMojo extends AbstractEasyjMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(property = "maven.file-generate.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * 是否需要判断pom.xml里存在 modules 标签
	 */
	@Parameter(property = "maven.file-generate.check-has-modules-tag", defaultValue = "false")
	private boolean checkHasModulesTag;

	/**
	 * 匹配
	 */
	@Parameter(property = "maven.file-generate.module-name-pattern")
	private String moduleNamePattern;

	@Parameter(property = "maven.file-generate.exclude-modules")
	private String excludeModules;

	@Parameter
	List<FileInfo> generateFiles;

	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("File-Generate has been skipped, because `skip=true`.");
			return;
		}

		if (checkHasModulesTag && (project.getModules() == null || project.getModules().isEmpty())) {
			getLog().info("File-Generate has been skipped, because `<modules>...</modules>` not exist.");
			return;
		}

		if (StringUtils.isNotEmpty(moduleNamePattern)) {
			boolean isMatched = false;

			Set<String> moduleNamePatterns = new HashSet<>();

			if (moduleNamePattern.contains(",")) {
				String[] moduleNamePatternArr = moduleNamePattern.split(",");
				for (String pattern : moduleNamePatternArr) {
					if (pattern != null) {
						pattern = pattern.trim();
					}
					if (StringUtils.isEmpty(pattern)) {
						continue;
					}

					moduleNamePatterns.add(pattern);
				}
			} else {
				moduleNamePatterns.add(moduleNamePattern.trim());
			}

			for (String pattern : moduleNamePatterns) {
				if (MatchUtils.match(pattern, project.getArtifactId())) {
					isMatched = true;
					break;
				}
			}

			if (!isMatched) {
				getLog().info("File-Generate has been skipped, because module name '" + project.getArtifactId() + "' not matched '" + moduleNamePattern + "'.");
				return;
			}

			// 判断排除项
			if (StringUtils.isNotEmpty(excludeModules) && (',' + excludeModules.replaceAll("\\s+", "") + ',').contains(',' + project.getArtifactId() + ',')) {
				getLog().info("File-Generate has been skipped, because exclude, the exclude-modules is '" + excludeModules + "'.");
				return;
			}
		}


		if (generateFiles != null) {
			getLog().info("fileList: " + generateFiles.toString());

			for (FileInfo fileInfo : generateFiles) {
				if (StringUtils.isEmpty(fileInfo.getPath())) {
					continue;
				}

				File file = new File(this.outputDirectory, fileInfo.getPath());
				try {
					IOUtils.createFile(file, fileInfo.getContent());
					getLog().info("Generate file: " + fileInfo.getPath());
				} catch (IOException e) {
					getLog().warn("Generate file failed: " + fileInfo.getPath(), e);
				}
			}
		}
	}

}
