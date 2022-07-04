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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import icu.easyj.maven.plugin.mojo.utils.IOUtils;
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

	@Parameter(property = "maven.spring-boot-release.filePatterns")
	private Set<String> filePatterns;


	/**
	 * 发布文件夹路径，仅将打包文件复制过去.
	 */
	@Parameter(property = "maven.spring-boot-release.dir")
	private String releaseDirectory;


	@Override
	public void doExecute() throws MojoExecutionException {
		if (StringUtils.isEmpty(this.releaseDirectory)) {
			throw new RuntimeException("releasePath不能为空");
		}

		// 处理发布文件夹目录
		String releaseDirectory = this.releaseDirectory
				.trim()
				.replaceAll("\\s*\\{\\s*(finalName)\\s*\\}", project.getBuild().getFinalName())
				.replaceAll("\\s*\\{\\s*(artifactId)\\s*\\}", project.getArtifactId())
				.replaceAll("[:\\*\\?\\\"'\\<\\>\\|]+", "");

		getLog().info("The release directory: " + releaseDirectory);

		// 创建化发布文件夹目录实例
		File releaseDir = new File(releaseDirectory);
		if (!releaseDir.exists()) {
			if (!releaseDir.mkdirs()) {
				getLog().warn("创建发布文件夹失败：" + releaseDir.getPath());
				return;
			}
		}

		// 创建target文件夹实例
		File targetDir = new File(this.outputDirectory + "\\target\\");
		if (!targetDir.exists()) {
			throw new RuntimeException("target文件夹不存在");
		}
		if (!targetDir.isDirectory()) {
			throw new RuntimeException(targetDir.getPath() + "不是文件夹");
		}

		// 处理匹配串
		Set<String> patterns = new HashSet<>();
		for (String filePattern : this.filePatterns) {
			if (StringUtils.isEmpty(filePattern)) {
				continue;
			}

			patterns.add(filePattern
					.trim()
					.replaceAll("\\s*\\{\\s*(finalName)\\s*\\}", project.getBuild().getFinalName())
					.replaceAll("\\s*\\{\\s*(artifactId)\\s*\\}", project.getArtifactId())
			);
		}
		getLog().info("The file patterns: " + patterns);

		// 匹配的文件复制到发布文件夹中
		File[] files = targetDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (!file.isFile()) {
					continue;
				}

				if (MatchUtils.match(patterns, file.getName())) {
					File copyFile = new File(releaseDir, file.getName());
					try {
						IOUtils.copy(file, copyFile);
						getLog().info("Copy file '/target/" + file.getName() + "' to '" + copyFile.getPath() + "'.");
					} catch (IOException e) {
						getLog().warn("Copy file failed：" + file.getPath() + " -> " + copyFile.getPath(), e);
					}
				}
			}
		}
	}
}
