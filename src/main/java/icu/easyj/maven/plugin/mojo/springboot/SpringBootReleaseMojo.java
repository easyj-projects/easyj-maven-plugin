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
package icu.easyj.maven.plugin.mojo.springboot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import icu.easyj.maven.plugin.mojo.utils.MatchUtils;
import icu.easyj.maven.plugin.mojo.utils.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * spring-boot应用的辅助发布插件
 *
 * @author wangliang181230
 * @since 0.7.4
 */
@Mojo(name = "spring-boot-release", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class SpringBootReleaseMojo extends AbstractSpringBootMojo {

	/**
	 * 源文件目录集合
	 */
	@Parameter
	private Set<File> sourceDirectories;

	/**
	 * 需发布文件匹配串。
	 */
	@Parameter(property = "maven.spring-boot-release.filePatterns", defaultValue = "{finalName}.jar,lib-*.zip,startup.*,*.yml,*.yaml,*.properties")
	private Set<String> filePatterns;

	/**
	 * 发布文件夹路径，仅将打包文件复制过去.
	 */
	@Parameter(property = "maven.spring-boot-release.dir")
	private String releaseDirectory;


	@Override
	public void doExecute() throws MojoExecutionException {
		if (StringUtils.isEmpty(this.releaseDirectory)) {
			throw new RuntimeException("'releaseDirectory' must be not empty.");
		}

		if (this.sourceDirectories == null) {
			this.sourceDirectories = new HashSet<>();
		}

		// release/ 文件夹实例
		File releaseDir = this.createReleaseDir();

		// 添加 /target/ 和 /target/classes/ 文件夹实例
		File targetDir = this.getTargetDir();
		if (targetDir.exists() && targetDir.isDirectory()) {
			this.sourceDirectories.add(targetDir);
			this.info("add targetDir");

			File classesDir = new File(targetDir, "classes");
			if (classesDir.exists() && classesDir.isDirectory()) {
				this.sourceDirectories.add(classesDir);
				this.info("add classesDir");
			}
		}

		// 处理匹配串
		Set<String> patterns = this.getFilePatterns();
		this.info("The file patterns: " + patterns);


		for (File sourceDir : this.sourceDirectories) {
			// 匹配的文件复制到发布文件夹中
			File[] files = sourceDir.listFiles();
			if (files != null) {
				List<File> fileList = new ArrayList<>(files.length);

				for (File file : files) {
					if (!file.isFile()) {
						continue;
					}

					if (MatchUtils.match(patterns, file.getName())) {
						fileList.add(file);
					}
				}

				this.copyFilesToDir(fileList, releaseDir, true);
			}
		}
	}


	private File createReleaseDir() {
		// 处理发布文件夹目录
		String releaseDirectory = this.replacePlaceholder(this.releaseDirectory.trim())
				.replaceAll("[\\*\\?\\\"'\\<\\>\\|]+", ""); // 移除部分特殊字符
		this.info("The release directory: " + releaseDirectory);

		File releaseDir = new File(releaseDirectory);

		if (!releaseDir.exists()) {
			if (!releaseDir.mkdirs()) {
				throw new RuntimeException("Create release directory failed：" + releaseDir.getPath());
			}
		}

		return releaseDir;
	}

	private Set<String> getFilePatterns() {
		Set<String> patterns = new HashSet<>();
		for (String filePattern : this.filePatterns) {
			if (StringUtils.isEmpty(filePattern)) {
				continue;
			}

			patterns.add(this.replacePlaceholder(filePattern.trim()));
		}

		return patterns;
	}
}
