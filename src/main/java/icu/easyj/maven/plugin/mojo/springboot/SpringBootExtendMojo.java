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
import org.apache.maven.model.Plugin;
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
		//region 判断是否为springboot应用

		boolean isSpringBootJar = false;
		String springBootMavenPluginVersion = null;

		if ("jar".equalsIgnoreCase(project.getPackaging())) {
			List<Plugin> plugins = project.getBuildPlugins();
			for (Plugin plugin : plugins) {
				if ("org.springframework.boot".equalsIgnoreCase(plugin.getGroupId())
						&& "spring-boot-maven-plugin".equalsIgnoreCase(plugin.getArtifactId())) {
					isSpringBootJar = true;
					springBootMavenPluginVersion = plugin.getVersion();
					break;
				}
			}
		}

		if (!isSpringBootJar) {
			getLog().info("Skip this goal, cause by this project is not a spring-boot application.");
			return;
		}

		//endregion

		getLog().info("The current project is a springboot application.");


		// 功能1：skip install or deploy
		this.skipInstallAndDeploy();


		// 功能2：includeGroupIds
		String loaderPath = this.includeDependencies(springBootMavenPluginVersion);


		// 功能3：创建startup文件
		this.createStartupFile(loaderPath);
	}


	//region 功能1：skip install or deploy

	private void skipInstallAndDeploy() {
		Properties properties = project.getProperties();

		if ((skipInstall && !"true".equalsIgnoreCase(properties.getProperty("maven.install.skip")))
				|| (skipDeploy && !"true".equalsIgnoreCase(properties.getProperty("maven.deploy.skip")))) {
			this.emptyLine();
			getLog().info("It will skip `install` and `deploy`:");

			if (skipInstall && !"true".equalsIgnoreCase(properties.getProperty("maven.install.skip"))) {
				properties.put("maven.install.skip", "true");
				getLog().info("  - Put property 'maven.install.skip = true' for skip `install`.");
			}

			if (skipDeploy && !"true".equalsIgnoreCase(properties.getProperty("maven.deploy.skip"))) {
				properties.put("maven.deploy.skip", "true");
				getLog().info("  - Put property 'maven.deploy.skip = true' for skip `deploy`.");
			}
		}
	}

	//endregion


	//region 功能2：includeGroupIds

	private String includeDependencies(String springBootMavenPluginVersion) {
		if (ObjectUtils.isEmpty(includeGroupIds)) {
			return null;
		}

		// string 转为 set
		Set<String> includeGroupIds = StringUtils.toTreeSet(this.includeGroupIds);
		if (includeGroupIds.isEmpty()) {
			return null;
		}
		String includeGroupIdsStr = includeGroupIds.toString();
		// 打印 includeGroupIds
		this.emptyLine();
		getLog().info("The includeGroupIds: " + includeGroupIdsStr.substring(1, includeGroupIdsStr.length() - 1).replaceAll("^|\\s*,\\s*", "\r\n[INFO]   - "));

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
		if (!excludeGroupIds.isEmpty()) {
			Properties properties = project.getProperties();

			//region 设置 'spring-boot.excludeGroupIds'，用于 spring-boot-maven-plugin:repackage

			// 打印下当前值
			String propertyValue = properties.getProperty("spring-boot.excludeGroupIds");
			if (ObjectUtils.isNotEmpty(propertyValue)) {
				this.emptyLine();
				getLog().info("The origin values of the property 'spring-boot.excludeGroupIds' for the goal 'spring-boot-maven-plugin:repackage':" + propertyValue.trim().replaceAll("^|\\s*,\\s*", "\r\n[INFO]   - "));
			}

			// set转string
			this.emptyLine();
			getLog().info("Put the following values to the property 'spring-boot.excludeGroupIds' for the goal 'spring-boot-maven-plugin:repackage': (" + excludeGroupIds.size() + ")");
			StringBuilder sb = new StringBuilder();
			for (String excludeGroupId : excludeGroupIds) {
				getLog().info("  - " + excludeGroupId);
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(excludeGroupId);
			}

			// 设置 properties
			properties.put("spring-boot.excludeGroupIds", sb.toString());

			//endregion

			// 设置 'spring-boot.repackage.layout = ZIP'
			if (!"ZIP".equals(properties.getProperty("spring-boot.repackage.layout"))) {
				properties.put("spring-boot.repackage.layout", "ZIP");
				this.emptyLine();
				getLog().info("Put property 'spring-boot.repackage.layout' = 'ZIP' for the goal 'spring-boot-maven-plugin:repackage'.");
				// spring-boot-maven-plugin
				if (springBootMavenPluginVersion.startsWith("0")
						|| springBootMavenPluginVersion.startsWith("1.")
						|| springBootMavenPluginVersion.startsWith("2.0.")
						|| springBootMavenPluginVersion.startsWith("2.1.")) {
					// 不使用WARN日志
					getLog().info("WARN: The version of the 'spring-boot-maven-plugin' is less than '2.2.0.RELEASE', please set 'layout' to 'ZIP' by yourself.");
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
				String str = commonDependencyPatternSet.toString();
				this.emptyLine();
				getLog().info("The commonDependencyPatterns: " + str.substring(1, str.length() - 1).replaceAll("^|\\s*,\\s*", "\r\n[INFO]   - "));
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
			getLog().info("  Total: " + total + " JARs");
			getLog().info("Include: " + StringUtils.padLeft(includeCount.get(), String.valueOf(total).length()) + " JARs");
			getLog().info("Exclude: " + StringUtils.padLeft(excludeArtifacts.size(), String.valueOf(total).length()) + " JARs（lib: " + jarFiles.size() + ", lib-common: " + commonJarFiles.size() + "）");

			String loaderPath = "";

			if (this.createLibDirAndZip("lib", jarFiles)) {
				loaderPath = "lib/";
			}
			if (this.createLibDirAndZip("lib-common", commonJarFiles)) {
				if (loaderPath.length() > 0) loaderPath += ", ";
				loaderPath += "lib-common/";
			}

			return loaderPath;
		} else {
			getLog().info("The 'excludeGroupIds' is empty, do not put the property 'spring-boot.excludeGroupIds'.");
			return null;
		}
	}

	private boolean createLibDirAndZip(String libDirName, List<File> jarFiles) {
		if (jarFiles.isEmpty()) {
			return false;
		}

		// 将依赖复制到 /target/lib 目录下
		this.emptyLine();
		File libDir = new File(outputDirectory.getPath() + "\\target\\" + libDirName);
		if (!libDir.exists()) {
			getLog().info("Create directory: " + libDir.getPath());
			if (!libDir.mkdir()) {
				throw new RuntimeException("Create directory failed: " + libDir.getPath());
			}
		}
		getLog().info("Copy " + jarFiles.size() + " JARs to the directory: " + libDir.getPath());
		for (File jarFile : jarFiles) {
			try {
				IOUtils.copy(jarFile, new File(libDir, jarFile.getName()));
			} catch (IOException e) {
				throw new RuntimeException("Copy '" + jarFile.getName() + "' to the directory '" + libDir.getPath() + "' failed.", e);
			}
		}

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

			getLog().info("Package '" + libDirName + ".zip' succeeded, contains " + jarFiles.size() + " JARs.");
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

	private boolean isRuntimeArtifact(Artifact artifact) {
		if (artifact.isOptional()) {
			return false;
		}

		String scope = artifact.getScope();
		return (scope == null
				|| scope.trim().isEmpty()
				|| "compile".equalsIgnoreCase(scope)
				|| "runtime".equalsIgnoreCase(scope));
	}

	//endregion


	//region 功能3：创建startup文件

	private void createStartupFile(String loaderPath) {
		if (!needCreateStartupFile) {
			return;
		}

		this.emptyLine();

		String startupScript = this.startupScript
				.replaceAll("\\s*\\{\\s*loaderPath\\s*\\}", (ObjectUtils.isNotEmpty(loaderPath) ? " -Dloader.path=\"" + loaderPath + "\" ^" : ""))
				.replaceAll("\\s*\\{\\s*(finalName)\\s*\\}", " " + project.getBuild().getFinalName())
				.replaceAll("\\s*\\{\\s*(artifactId)\\s*\\}", " " + project.getArtifactId())
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
			getLog().info("Create startup file succeeded: " + file.getName() + ", the startup script:\r\n===>\r\n" + startupScriptText.trim() + "\r\n<===\r\n");
		} catch (IOException e) {
			getLog().error("Create startup file failed: " + file.getName(), e);
		}
	}

	//endregion
}
