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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import icu.easyj.maven.plugin.mojo.utils.IOUtils;
import icu.easyj.maven.plugin.mojo.utils.MatchUtils;
import icu.easyj.maven.plugin.mojo.utils.ObjectUtils;
import icu.easyj.maven.plugin.mojo.utils.StringUtils;
import icu.easyj.maven.plugin.mojo.utils.ZipUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * spring-boot插件的协助插件
 *
 * @author wangliang181230
 * @since 0.6.8
 */
@Mojo(name = "spring-boot-extend", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class SpringBootExtendMojo extends AbstractSpringBootMojo {

	//region 功能1：skip install or deploy

	@Parameter(defaultValue = "false")
	private boolean skipInstall;

	@Parameter(defaultValue = "false")
	private boolean skipDeploy;

	//endregion


	//region 功能2：includeGroupIds

	/**
	 * 由于springboot官方不提供该功能，只提供 excludeGroupIds，所以这里提供该功能
	 */
	@Parameter(property = "maven.spring-boot-extend.includeGroupIds")
	private String includeGroupIds;

	@Parameter(property = "maven.spring-boot-extend.commonDependencyPatterns")
	private String commonDependencyPatterns;

	/**
	 * @since 0.7.3
	 */
	@Parameter(property = "maven.spring-boot-extend.commonDependencyPatternSet")
	private Set<String> commonDependencyPatternSet;

	/**
	 * 是否将排除掉的lib打包进lib.zip中。
	 * {@link #includeGroupIds} 不为空时，才有作用。
	 */
	@Parameter(property = "maven.spring-boot-extend.zipLib", defaultValue = "true")
	private boolean zipLib;

	//endregion


	//region 功能3：创建startup文件

	/**
	 * 是否需要创建startup文件
	 */
	@Parameter(property = "maven.spring-boot-extend.needCreateStartupFile", defaultValue = "true")
	private boolean needCreateStartupFile;

	/**
	 * spring-boot应用的startup脚本
	 */
	@Parameter(property = "maven.spring-boot-extend.startupScript", defaultValue = "java -jar ^ {loaderPath} ^ {finalName}.jar")
	private String startupScript;

	//endregion


	@Override
	public void doExecute() throws MojoExecutionException {
		this.info("The current project is a springboot application.");


		// 功能1：skip install or deploy
		this.skipInstallAndDeploy();


		// 功能2：includeGroupIds
		String loaderPath = this.includeDependencies();


		// 功能3：创建startup文件
		this.createStartupFile(loaderPath);
	}


	//region 功能1：skip install or deploy

	private void skipInstallAndDeploy() {
		Properties properties = project.getProperties();

		if ((skipInstall && !"true".equalsIgnoreCase(properties.getProperty("maven.install.skip")))
				|| (skipDeploy && !"true".equalsIgnoreCase(properties.getProperty("maven.deploy.skip")))) {
			this.emptyLine();
			this.info("It will skip `install` and `deploy`:");

			if (skipInstall && !"true".equalsIgnoreCase(properties.getProperty("maven.install.skip"))) {
				properties.put("maven.install.skip", "true");
				this.info("  > Put property 'maven.install.skip = true' for skip `install`.");
			}

			if (skipDeploy && !"true".equalsIgnoreCase(properties.getProperty("maven.deploy.skip"))) {
				properties.put("maven.deploy.skip", "true");
				this.info("  > Put property 'maven.deploy.skip = true' for skip `deploy`.");
			}
		}
	}

	//endregion


	//region 功能2：includeGroupIds

	private String includeDependencies() {
		if (ObjectUtils.isEmpty(includeGroupIds)) {
			return null;
		}

		// string 转为 set
		Set<String> includeGroupIds = StringUtils.toTreeSet(this.includeGroupIds);
		if (includeGroupIds.isEmpty()) {
			return null;
		}
		// 打印 includeGroupIds
		this.emptyLine();
		this.info("The includeGroupIds: " + this.collectionToStr(includeGroupIds));

		// 因为spring-boot-maven-plugin:repackage没有includeGroupIds，所以反过来使用excludeGroupIds来达到include的效果
		Set<String> excludeGroupIds = new TreeSet<>(String::compareTo); // 使用TreeSet，为了下面的日志按groupId顺序打印
		AtomicInteger includeCount = new AtomicInteger(0);
		// 设置过滤器
		project.setArtifactFilter(artifact -> {
			if (this.isRuntimeArtifact(artifact)) {
				if (!includeGroupIds.contains(artifact.getGroupId())) {
					return true;
				} else {
					includeCount.incrementAndGet();
				}
			}
			return false;
		});
		// 获取需排除的artifacts
		Set<Artifact> excludeArtifacts = project.getArtifacts();
		// 需排除的artifacts的所有groupId添加到excludeGroupIds
		for (Artifact excludeArtifact : excludeArtifacts) {
			excludeGroupIds.add(excludeArtifact.getGroupId());
		}
		// 清空过滤器
		project.setArtifactFilter(null);

		// 设置 'spring-boot.excludeGroupIds'
		if (excludeGroupIds.isEmpty()) {
			this.info("The 'excludeGroupIds' is empty, do not put the property 'spring-boot.excludeGroupIds'.");
			return null;
		}


		Properties properties = project.getProperties();


		//region 设置 property 'spring-boot.excludeGroupIds'，用于 spring-boot-maven-plugin:repackage

		// 打印下当前值
		String propertyValue = this.getProperty("spring-boot.excludeGroupIds");
		if (ObjectUtils.isNotEmpty(propertyValue)) {
			this.emptyLine();
			this.info("The origin values of the property 'spring-boot.excludeGroupIds' for the goal 'spring-boot-maven-plugin:repackage':" + this.handleListStr(propertyValue.trim()));
		}

		// 设置 property 'spring-boot.excludeGroupIds'
		this.emptyLine();
		this.info("Put the following values to the property 'spring-boot.excludeGroupIds' for the goal 'spring-boot-maven-plugin:repackage': (%d)%s",
				excludeGroupIds.size(), this.collectionToStr(excludeGroupIds));
		properties.put("spring-boot.excludeGroupIds", StringUtils.toString(excludeGroupIds));

		//endregion


		// 设置 'spring-boot.repackage.layout = ZIP'
		if (!this.containsProperty("spring-boot.repackage.layout", "ZIP")) {
			properties.put("spring-boot.repackage.layout", "ZIP");
			this.emptyLine();
			this.info("Put property 'spring-boot.repackage.layout' = 'ZIP' for the goal 'spring-boot-maven-plugin:repackage'.");
			// spring-boot-maven-plugin
			if (springBootMavenPluginVersion.startsWith("0")
					|| springBootMavenPluginVersion.startsWith("1.")
					|| springBootMavenPluginVersion.startsWith("2.0.")
					|| springBootMavenPluginVersion.startsWith("2.1.")) {
				// 不使用WARN日志
				this.info("WARN: The version of the 'spring-boot-maven-plugin' is less than '2.2.0.RELEASE', please set 'layout' to 'ZIP' by yourself.");
			}
		}


		//region lib 和 lib-common，根据 commonDependencyPatterns 配置，分开来

		List<File> jarFiles = new ArrayList<>();
		List<File> commonJarFiles = new ArrayList<>();

		Set<String> commonDependencyPatternSet = StringUtils.toSet(this.commonDependencyPatterns);
		if (ObjectUtils.isNotEmpty(this.commonDependencyPatternSet)) {
			commonDependencyPatternSet.addAll(this.commonDependencyPatternSet);
		}
		if (!commonDependencyPatternSet.isEmpty()) {
			this.emptyLine();
			this.info("The commonDependencyPatterns: " + this.collectionToStr(commonDependencyPatternSet));
		}

		// 从 artifact 中获取 file
		for (Artifact excludeArtifact : excludeArtifacts) {
			if (this.isCommonJar(excludeArtifact, commonDependencyPatternSet)) {
				commonJarFiles.add(excludeArtifact.getFile());
			} else {
				jarFiles.add(excludeArtifact.getFile());
			}
		}

		//endregion

		int total = (includeCount.get() + excludeArtifacts.size());
		this.emptyLine();
		this.info("  Total: %d JARs", total);
		this.info("Include: %s JARs", StringUtils.padLeft(includeCount.get(), String.valueOf(total).length()));
		this.info("Exclude: %s JARs（lib: %d, lib-common: %d）",
				StringUtils.padLeft(excludeArtifacts.size(), String.valueOf(total).length()), jarFiles.size(), commonJarFiles.size());

		String loaderPath = "";

		if (this.createLibDirAndZip("lib", jarFiles)) {
			loaderPath = "lib/";
		}
		if (this.createLibDirAndZip("lib-common", commonJarFiles)) {
			if (loaderPath.length() > 0) loaderPath += ", ";
			loaderPath += "lib-common/";
		}

		return loaderPath;
	}

	private boolean createLibDirAndZip(String libDirName, List<File> jarFiles) {
		if (jarFiles.isEmpty()) {
			return false;
		}

		// 创建lib目录实例
		File libDir = this.createLibDir(libDirName);

		// 将依赖复制到lib目录下
		this.emptyLine();
		this.info("Copy %d JARs to the directory: %s", jarFiles.size(), libDir.getPath());
		this.copyFilesToDir(jarFiles, libDir);

		// 将依赖打包进lib.zip中
		if (zipLib) {
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(outputDirectory.getPath() + "\\target\\" + libDirName + "---" + jarFiles.size() + "-JARs.zip");
			} catch (FileNotFoundException e) {
				throw new RuntimeException("New FileOutputStream of '" + libDirName + ".zip' failed.", e);
			}

			try {
				ZipUtils.toZip(jarFiles, fos, false, libDirName);
			} catch (IOException e) {
				throw new RuntimeException("Package '" + libDirName + ".zip' failed.", e);
			}

			this.info("Package '%s.zip' succeeded, contains %d JARs.", libDirName, jarFiles.size());
		}

		return true;
	}

	private boolean isCommonJar(Artifact artifact, Set<String> commonDependencyPatternSet) {
		for (String commonArtifactPattern : commonDependencyPatternSet) {
			if (MatchUtils.match(commonArtifactPattern, artifact.getGroupId() + ":" + artifact.getArtifactId())) {
				return true;
			}
		}
		return false;
	}

	//endregion


	//region 功能3：创建startup文件

	private void createStartupFile(String loaderPath) {
		if (!needCreateStartupFile) {
			return;
		}

		this.emptyLine();

		String startupScript = this.replacePlaceholder(this.startupScript)
				.replaceAll("\\s*\\{\\s*loaderPath\\s*\\}", (ObjectUtils.isNotEmpty(loaderPath) ? " -Dloader.path=\"" + loaderPath + "\" ^" : ""))
				.replaceAll("\\s*(\\^|\\<br\\s*\\/\\>)(\\s|\\^|\\<br\\s*\\/\\>)*", " ^\r\n     ");

		// 创建startup.bat文件
		createStartupFile("bat", startupScript + "\r\n\r\ncmd\r\n");
		// 创建startup.sh文件
		createStartupFile("sh", "#!/bin/sh\r\n\r\n" + startupScript.replace('^', '\\') + "\r\n");
	}

	private void createStartupFile(String fileSuffix, String startupScriptText) {
		File file = new File(outputDirectory.getPath() + "\\target\\startup." + fileSuffix);
		try {
			IOUtils.createFile(file, startupScriptText);
			this.info("Create startup file succeeded: %s, the startup script:\r\n===>\r\n%s\r\n<===\r\n",
					file.getName(), startupScriptText.trim());
		} catch (IOException e) {
			this.error("Create startup file failed: %s", file.getName(), e);
		}
	}

	//endregion
}
