/*
 * Copyright 2021-2023 the original author or authors.
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

import static icu.easyj.maven.plugin.mojo.Version.VERSION;
import static icu.easyj.maven.plugin.mojo.utils.IOUtils.LINE_SEPARATOR;
import static icu.easyj.maven.plugin.mojo.utils.IOUtils.LINE_SEPARATOR2;

/**
 * spring-boot插件的协助插件
 *
 * @author wangliang181230
 * @since 0.6.8
 */
@Mojo(name = "spring-boot-extend", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class SpringBootExtendMojo extends AbstractSpringBootMojo {

	private static final String SEPARATOR = " | ";


	//region 功能1：skip install or deploy

	@Parameter(defaultValue = "false")
	private boolean skipInstall;

	@Parameter(defaultValue = "false")
	private boolean skipDeploy;

	//endregion


	//region 功能2：includeGroupIds

	/**
	 * 是否跳过 includeGroupIds 功能
	 *
	 * @since 1.0.9
	 */
	@Parameter(property = "maven.spring-boot-extend.skipIncludeGroupIds", defaultValue = "false")
	private boolean skipIncludeGroupIds;

	/**
	 * 由于springboot官方不提供该功能，只提供 excludeGroupIds，所以这里提供该功能
	 */
	@Parameter(property = "maven.spring-boot-extend.includeGroupIds")
	private String includeGroupIds;

	/**
	 * 由于 {@link #includeGroupIds} 经常用于配置在框架中，所以添加了此属性，在项目中个性化配置。<br>
	 * 当 {@link #includeGroupIds} 未配置时，此配置也无效。
	 *
	 * @since 1.0.4
	 */
	@Parameter(property = "maven.spring-boot-extend.additionalIncludeGroupIds")
	private String additionalIncludeGroupIds;

	/**
	 * 是否include 所有snapshot的依赖
	 *
	 * @since 1.0.9
	 */
	@Parameter(property = "maven.spring-boot-extend.includeSnapshotDependencies", defaultValue = "false")
	private boolean includeSnapshotDependencies;

	@Parameter(property = "maven.spring-boot-extend.commonDependencyPatterns")
	private String commonDependencyPatterns;

	/**
	 * @since 0.7.3
	 */
	@Parameter(property = "maven.spring-boot-extend.commonDependencyPatternSet")
	private Set<String> commonDependencyPatternSet;

	/**
	 * 与 maven-dependency-plugin 插件的 includeScope 效果一致。
	 *
	 * @since 1.1.0
	 */
	@Parameter(property = "maven.spring-boot-extend.libIncludeScope", defaultValue = Artifact.SCOPE_RUNTIME)
	private String libIncludeScope;

	/**
	 * 与 maven-dependency-plugin 插件的 excludeScope 效果一致。
	 *
	 * @since 1.1.0
	 */
	@Parameter(property = "maven.spring-boot-extend.libExcludeScope")
	private String libExcludeScope;

	/**
	 * 是否需要optional=true的依赖放入外置lib目录中
	 *
	 * @since 1.1.0
	 */
	@Parameter(property = "maven.spring-boot-extend.libIncludeOptional", defaultValue = "false")
	private boolean libIncludeOptional;

	/**
	 * 是否移除 标记了 ”Spring-Boot-Jar-Type: dependencies-starter“ 的JAR
	 *
	 * @since 1.1.0
	 */
	@Parameter(property = "maven.spring-boot-extend.libExcludeDependenciesStarter", defaultValue = "true")
	private boolean libExcludeDependenciesStarter;

	/**
	 * 是否将排除掉的lib打包进lib.zip中。
	 * {@link #includeGroupIds} 不为空时，才有作用。
	 */
	@Parameter(property = "maven.spring-boot-extend.zipLib", defaultValue = "true")
	private boolean zipLib;

	/**
	 * 生成lib历史文件。主要目的是为了让开发人员或运维人员知道lib是否 '已变更且需要更新'。
	 */
	@Parameter(property = "maven.spring-boot-extend.createLibHistory", defaultValue = "true")
	private boolean createLibHistory;

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
	@Parameter(
			property = "maven.spring-boot-extend.startupScript",
			defaultValue = "" +
					"java -Xms64m -Xmx128m ^" + LINE_SEPARATOR2 +
					"     -jar ^" + LINE_SEPARATOR2 +
					"     {loaderPath} ^" + LINE_SEPARATOR2 +
					"     -Dspring.profiles.active={activeProfile} ^" + LINE_SEPARATOR2 +
					"     -Dspring.config.location=application.yml ^" + LINE_SEPARATOR2 +
					"     -Dspring.config.additional-location=application-{activeProfile}.yml ^" + LINE_SEPARATOR2 +
					"     {startupScriptAdditionalParameter} ^" + LINE_SEPARATOR2 +
					"     {finalName}.jar"
	)
	private String startupScript;

	@Parameter(property = "maven.spring-boot-extend.startupScriptAdditionalParameter")
	private String startupScriptAdditionalParameter;

	@Parameter(property = "maven.spring-boot-extend.activeProfile", defaultValue = "prod")
	private String activeProfile;

	//endregion


	@Override
	public void doExecute() throws MojoExecutionException, IOException {
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

	private String includeDependencies() throws IOException {
		if (this.skipIncludeGroupIds) {
			return null;
		}

		// 获取 includeGroupIds
		Set<String> includeGroupIds = this.getIncludeGroupIds();
		if (ObjectUtils.isEmpty(includeGroupIds)) {
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
			if (this.isNotTestArtifact(artifact)) {
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

		// 将scope=provided、optional=true和无用的jar包丢弃掉
		excludeArtifacts.removeIf(art ->
				!filter(art, this.libIncludeScope, this.libExcludeScope)
						|| isUnnecessaryArtifact(art, this.libExcludeDependenciesStarter) // 一些不需要的依赖，如：编译期起才作用的依赖
						|| (!libIncludeOptional && art.isOptional()) // optional=true
		);

		List<Artifact> jarArtifacts = new ArrayList<>();
		List<Artifact> commonJarArtifacts = new ArrayList<>();

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
				commonJarArtifacts.add(excludeArtifact);
			} else {
				jarArtifacts.add(excludeArtifact);
			}
		}

		//endregion

		int total = (includeCount.get() + excludeArtifacts.size());
		this.emptyLine();
		this.info("  Total: %d JARs", total);
		this.info("Include: %s JARs", StringUtils.padLeft(includeCount.get(), String.valueOf(total).length()));
		this.info("Exclude: %s JARs（lib: %d, lib-common: %d）",
				StringUtils.padLeft(excludeArtifacts.size(), String.valueOf(total).length()), jarArtifacts.size(), commonJarArtifacts.size());

		String loaderPath = "";

		if (this.createLibDirAndZip("lib", jarArtifacts)) {
			loaderPath = "lib/";
		}
		if (this.createLibDirAndZip("lib-common", commonJarArtifacts)) {
			if (loaderPath.length() > 0) loaderPath += ", ";
			loaderPath += "lib-common/";
		}

		return loaderPath;
	}

	@Nonnull
	private Set<String> getIncludeGroupIds() {
		String includeGroupIdsStr = this.includeGroupIds;
		if (includeGroupIdsStr == null) {
			includeGroupIdsStr = "";
		} else {
			includeGroupIdsStr = includeGroupIdsStr.trim();
		}

		if (ObjectUtils.isNotEmpty(this.additionalIncludeGroupIds)) {
			if (includeGroupIdsStr.length() > 0 && !includeGroupIdsStr.endsWith(",")) {
				includeGroupIdsStr += ",";
			}
			includeGroupIdsStr += this.additionalIncludeGroupIds;
		}

		// string 转为 set
		Set<String> includeGroupIds = StringUtils.toTreeSet(includeGroupIdsStr);

		if (this.includeSnapshotDependencies) {
			// 设置过滤器
			project.setArtifactFilter(artifact -> this.isNotTestArtifact(artifact) && artifact.getVersion().endsWith("-SNAPSHOT"));
			// 获取SNAPSHOT版本的artifacts
			Set<Artifact> snapshotArtifacts = project.getArtifacts();
			// 清空过滤器
			project.setArtifactFilter(null);
			// 获取groupIds
			for (Artifact snapshotArtifact : snapshotArtifacts) {
				this.info("snapshotArtifact: %s", snapshotArtifact.getArtifactId());
				includeGroupIds.add(snapshotArtifact.getGroupId());
			}
		}

		return includeGroupIds;
	}

	private boolean createLibDirAndZip(String libDirName, List<Artifact> jarArtifacts) throws IOException {
		if (jarArtifacts.isEmpty()) {
			return false;
		}

		// 创建lib目录实例
		File libDir = this.createLibDir(libDirName);

		// 将依赖复制到lib目录下
		this.emptyLine();
		this.info("Copy %d JARs to the directory: %s", jarArtifacts.size(), libDir.getPath());
		this.copyFilesToDir2(jarArtifacts, libDir);

		// 生成lib(-common).history.md
		if (this.createLibHistory) {
			this.createLibHistoryFile(libDirName, jarArtifacts);
		}

		// 将依赖打包进lib(-common).zip中
		if (zipLib) {
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(outputDirectory.getPath() + "/target/" + libDirName + "---" + jarArtifacts.size() + "-JARs.zip");
			} catch (FileNotFoundException e) {
				throw new RuntimeException("New FileOutputStream of '" + libDirName + ".zip' failed.", e);
			}

			try {
				ZipUtils.toZip3(jarArtifacts, fos, false, libDirName);
			} catch (IOException e) {
				throw new RuntimeException("Package '" + libDirName + ".zip' failed.", e);
			}

			this.info("Package '%s.zip' succeeded, contains %d JARs.", libDirName, jarArtifacts.size());
		}

		return true;
	}

	private void createLibHistoryFile(String libDirName, List<Artifact> jarArtifacts) throws IOException {
		// 根据 文件名 排序
		jarArtifacts.sort(Comparator.comparing(a -> a.getFile().getName().toLowerCase()));
		// 根据 组名+文件名 排序
		/*jarArtifacts.sort((a, b) -> {
			int ret = a.getGroupId().toLowerCase().compareTo(b.getGroupId().toLowerCase());
			if (ret != 0) {
				return ret;
			} else {
				return a.getFile().getName().toLowerCase().compareTo(b.getFile().getName().toLowerCase());
			}
		});*/

		// 获取各各最大长度
		int maxNameLength = 0;
		int maxGroupIdLength = 0;
		int maxBLength = 0;
		int maxKBLength = 0;
		long totalLength = 0;
		for (Artifact jarArtifact : jarArtifacts) {
			File jarFile = jarArtifact.getFile();
			if (maxNameLength < jarFile.getName().length()) {
				maxNameLength = jarFile.getName().length();
			}
			if (maxGroupIdLength < jarArtifact.getGroupId().length()) {
				maxGroupIdLength = jarArtifact.getGroupId().length();
			}
			if (maxBLength < String.valueOf(jarFile.length()).length()) {
				maxBLength = String.valueOf(jarFile.length()).length();
			}
			if (maxKBLength < String.valueOf(jarFile.length() / 1024).length()) {
				maxKBLength = String.valueOf(jarFile.length() / 1024).length();
			}
			totalLength += jarFile.length();
		}
		maxNameLength = Math.max(maxNameLength, 9);
		maxGroupIdLength = Math.max(maxGroupIdLength, 8);
		maxBLength = Math.max(maxBLength, 5);
		maxKBLength = Math.max(maxKBLength, 5);

		// 组装文件内容
		StringBuilder history = new StringBuilder();
		// 文件来源说明与创建时间
		history.append("```yaml").append(LINE_SEPARATOR)
				.append("Created-By: icu.easyj.maven.plugins:easyj-maven-plugin:").append(VERSION).append("(goal:spring-boot-extend)").append(LINE_SEPARATOR)
				.append("Created-On: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis())).append(LINE_SEPARATOR)
				.append("Tips: Please push this file to the VCS(Version Control System), it can be used to compare future changes to libs.").append(LINE_SEPARATOR)
				.append("```").append(LINE_SEPARATOR)
				.append(LINE_SEPARATOR);
		// 项目信息：groupId、artifactId、version
		history.append("```yaml").append(LINE_SEPARATOR)
				.append("groupId: ").append(project.getGroupId()).append(LINE_SEPARATOR)
				.append("artifactId: ").append(project.getArtifactId()).append(LINE_SEPARATOR)
				.append("version: ").append(project.getVersion()).append(LINE_SEPARATOR)
				.append("```").append(LINE_SEPARATOR)
				.append(LINE_SEPARATOR);
		// lib信息
		history.append("```yaml").append(LINE_SEPARATOR);
		// libs总数量
		history.append("Number of libs: ").append(jarArtifacts.size()).append(LINE_SEPARATOR);
		// libs总大小：分三个单位展示（B、KB、MB）
		history.append("Size of libs: ")
				.append(totalLength).append(" B | ")
				.append(totalLength / 1024).append(" KB | ")
				.append(totalLength / 1024 / 1024).append(" MB").append(LINE_SEPARATOR);
		history.append("```").append(LINE_SEPARATOR)
				.append(LINE_SEPARATOR);
		// 表头
		history.append("| File Name").append(this.buildStr(maxNameLength - 9, ' ')).append(" ")
				.append("| Group ID").append(this.buildStr(maxGroupIdLength - 8, ' ')).append(" ")
				.append("|    Last Modified    ")
				.append("| ").append(this.buildStr(maxBLength - 5, ' ')).append("Size(B) ")
				.append("| ").append(this.buildStr(maxKBLength - 5, ' ')).append("Size(KB) |")
				.append(LINE_SEPARATOR)
				.append("|:").append(this.buildStr(maxNameLength, '-')).append("-") // JAR文件名
				.append("|:").append(this.buildStr(maxGroupIdLength, '-')).append("-") // 所属组ID
				.append("|:-------------------:") // 创建时间
				.append("|-").append(this.buildStr(maxBLength + 2, '-')).append(":") // 文件大小（B）
				.append("|-").append(this.buildStr(maxKBLength + 3, '-')).append(":|") // 文件大小（KB）
				.append(LINE_SEPARATOR);
		// 表内容
		for (Artifact jarArtifact : jarArtifacts) {
			File jarFile = jarArtifact.getFile();
			long fileLength = jarFile.length();
			history.append("| ") // 行首符号
					.append(jarFile.getName()).append(this.buildIndent(maxNameLength, jarFile.getName())) // JAR文件名
					.append(SEPARATOR) // 分隔符
					.append(jarArtifact.getGroupId()).append(this.buildIndent(maxGroupIdLength, jarArtifact.getGroupId())) // 所属组ID
					.append(SEPARATOR) // 分隔符
					.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(IOUtils.getFileLastModified(jarFile))) // 文件最后修改时间
					.append(SEPARATOR) // 分隔符
					.append(this.buildIndent(maxBLength, fileLength)).append(fileLength).append(" B") // B
					.append(SEPARATOR) // 分隔符
					.append(this.buildIndent(maxKBLength, fileLength / 1024)).append(fileLength / 1024).append(" KB").append(" |") // KB
					.append(LINE_SEPARATOR); // 换行
		}
		String newHistoryTxt = history.toString().trim();

		// 读取现有的文件内容并与新的文件内容作比较，如果不一样，则提示警告，告知开发或运维人员需要更新外置lib了
		File historyFile = new File(this.outputDirectory, libDirName + ".history.md");
		String historyTxt = IOUtils.getFileTxt(historyFile);
		if (historyTxt != null) { // 为null时，文件不存在，说明是第一次生成
			if (!getLibHistoryTableTxt(historyTxt).equals(getLibHistoryTableTxt(newHistoryTxt))) {
				// 打印 WARNING 日志
				this.warn("'%s/' 目录中的JAR文件已变更，请检查 '%s/%s.history.md' 文件中已变更的JAR，并务必与应用一起更新，避免应用运行异常！(%s)",
						libDirName, this.outputDirectory.getName(), libDirName,
						new SimpleDateFormat("HH:mm:ss.SSS").format(new Date())); // 后面加个时间，避免IDE不提示WARN警告。

				// 将原文件重命名为 *.bak 文件，并设为可写
				File historyFileBak = new File(this.outputDirectory, libDirName + ".history.md.bak");
				int i = 1;
				while (historyFileBak.exists()) {
					historyFileBak.setWritable(true);
					historyFileBak = new File(this.outputDirectory, libDirName + ".history.md(" + i + ").bak");
					i++;
				}
				historyFile.renameTo(historyFileBak);
				this.updateLibHistoryFileLastModified(historyFileBak, historyTxt);

				// 创建新文件
				historyFile.setWritable(true);
				IOUtils.createFile(historyFile, newHistoryTxt);
			}
		} else {
			IOUtils.createFile(historyFile, newHistoryTxt);
		}
		// 最终设置为只读
		historyFile.setReadOnly();
	}

	private boolean isCommonJar(Artifact artifact, Set<String> commonDependencyPatternSet) {
		for (String commonArtifactPattern : commonDependencyPatternSet) {
			if (MatchUtils.match(commonArtifactPattern, artifact.getGroupId() + ":" + artifact.getArtifactId())) {
				return true;
			}
		}
		return false;
	}

	private String buildIndent(int maxNameLength, String name) {
		int diff = maxNameLength - name.length();
		return buildStr(diff, ' ');
	}

	private String buildIndent(int maxSizeLength, long size) {
		int diff = maxSizeLength - String.valueOf(size).length();

		return buildStr(diff, ' ');
	}

	private String buildStr(int length, char c) {
		StringBuilder sb = new StringBuilder();
		while (length > 0) {
			length--;
			sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * 获取lib历史信息主要内容
	 *
	 * @param libHistoryTxt lib历史信息
	 * @return 主要历史信息
	 */
	String getLibHistoryTableTxt(String libHistoryTxt) {
		libHistoryTxt = libHistoryTxt
				.replaceAll("[ \t\r]+", "") // 去除所有空格
				.replaceAll("[:\\-]+(?=\\|)", "") // 去除表头的
				.replaceAll("\\|{5,}", "||||"); // 把表头和表内容的分隔行的符号替换成4个，方便substring，避免上面内容改了造成BUG。
		return libHistoryTxt.substring(libHistoryTxt.indexOf("||||") + 5).trim(); // 获取表格内容
	}

	@Nullable
	String readLibHistoryFileCreatedOn(String historyTxt) {
		int idx = historyTxt.indexOf("Created-On:");
		if (idx < 0) {
			return null;
		}

		historyTxt = historyTxt.substring(idx + 11).trim();
		if (historyTxt.length() < 23) {
			return null;
		}

		return historyTxt.substring(0, 23);
	}

	@Nullable
	Date readLibHistoryFileCreatedTime(String historyTxt) {
		String timeStr = this.readLibHistoryFileCreatedOn(historyTxt);
		if (timeStr == null) {
			return null;
		}
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(timeStr);
		} catch (ParseException ignore) {
			return null;
		}
	}

	void updateLibHistoryFileLastModified(File libHistoryFile, String libHistoryTxt) {
		Date historyFileCreatedTime = readLibHistoryFileCreatedTime(libHistoryTxt);
		if (historyFileCreatedTime != null) {
			libHistoryFile.setLastModified(historyFileCreatedTime.getTime());
		}
	}

	//endregion


	//region 功能3：创建startup文件

	private void createStartupFile(String loaderPath) throws IOException {
		if (!needCreateStartupFile) {
			return;
		}

		this.emptyLine();

		// 替换启动脚本的占位符
		String startupScript = this.replacePlaceholder(
						this.startupScript.replaceAll("\\s*\\{\\s*startupScriptAdditionalParameter\\s*\\}", (ObjectUtils.isNotEmpty(this.startupScriptAdditionalParameter) ? this.startupScriptAdditionalParameter : ""))
				)
				.replaceAll("\\s*\\{\\s*loaderPath\\s*\\}", (ObjectUtils.isNotEmpty(loaderPath) ? " -Dloader.path=\"" + loaderPath + "\" ^" : ""))
				.replaceAll("\\s*\\{\\s*activeProfile\\s*\\}", this.activeProfile)
				.replaceAll("\\s*(\\^|\\<br\\s*\\/\\>|\\\\)(\\s|\\^|\\<br\\s*\\/\\>|\\\\)*", " ^\r\n     ")
				.trim();

		// 如果指定环境配置文件不存在，则自动创建一个
		File activeProfileFile = new File(this.outputDirectory + "/target/classes/application-" + this.activeProfile + ".yml");
		if (!activeProfileFile.exists() || !activeProfileFile.isFile()) {
			IOUtils.createFile(activeProfileFile, "# " + this.activeProfile + "环境" + LINE_SEPARATOR2 + LINE_SEPARATOR2);
		}

		// 创建startup.bat文件
		createStartupFile("bat",
				"chcp 65001\r\n\r\ntitle \"" + project.getBuild().getFinalName() + "\"" + LINE_SEPARATOR2 + LINE_SEPARATOR2
						+ startupScript
						+ "\r\n\r\ncmd\r\n"
		);
		// 创建startup.sh文件
		createStartupFile("sh",
				"#!/bin/sh\r\n\r\n"
						+ startupScript.replace('^', '\\') + "\r\n"
		);
	}

	private void createStartupFile(String fileSuffix, String startupScriptText) {
		File file = new File(outputDirectory.getPath() + "/target/startup." + fileSuffix);
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
