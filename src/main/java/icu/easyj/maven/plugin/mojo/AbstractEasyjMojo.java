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
package icu.easyj.maven.plugin.mojo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import icu.easyj.maven.plugin.mojo.utils.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 抽象Mojo
 *
 * @author wangliang181230
 * @since 1.0.0
 */
public abstract class AbstractEasyjMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${project.basedir}")
	protected File outputDirectory;


	/**
	 * 替换 占位符
	 *
	 * @param str 字符串
	 * @return 替换后的字符串
	 */
	protected String replacePlaceholder(String str) {
		return str
				.replaceAll("\\{\\s*(groupId)\\s*\\}", project.getGroupId())
				.replaceAll("\\{\\s*(artifactId)\\s*\\}", project.getArtifactId())
				.replaceAll("\\{\\s*(version)\\s*\\}", project.getVersion())
				.replaceAll("\\{\\s*(finalName)\\s*\\}", project.getBuild().getFinalName())
				;
	}


	//region 构件相关

	protected boolean isRuntimeArtifact(Artifact artifact) {
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


	//region property相关

	protected Properties getOriginalProperties() {
		return project.getOriginalModel().getProperties();
	}

	protected String getProperty(String key) {
		return project.getProperties().getProperty(key);
	}

	protected void putProperty(Properties properties, String key, Object value, String info) {
		properties.put(key, value);
		this.info("Put property '%s' = '%s' %s.", key, value == null ? "null" : value.toString(), info == null ? "" : info);
	}

	protected void putProperty(Properties properties, String key, Object value) {
		putProperty(properties, key, value, null);
	}

	protected void putProperty(String key, Object value, String info) {
		this.putProperty(project.getProperties(), key, value, info);
	}

	protected void putProperty(String key, Object value) {
		this.putProperty(key, value, null);
	}

	protected boolean containsProperty(String key) {
		return project.getProperties().containsKey(key);
	}

	protected boolean containsProperty(String key, String targetValue) {
		String value = project.getProperties().getProperty(key);
		return targetValue.equalsIgnoreCase(value);
	}

	protected boolean containsProperty(String key, String targetValue, boolean ignoreCase) {
		String value = project.getProperties().getProperty(key);
		if (value == null) {
			return false;
		}
		return ignoreCase ? targetValue.equalsIgnoreCase(value) : targetValue.equals(value);
	}

	//endregion


	//region 创建目录实例

	protected File getTargetDir() {
		return new File(this.outputDirectory, "target\\");
	}

	protected File createTargetDir() {
		File targetDir = this.getTargetDir();
		if (!targetDir.exists()) {
			if (!targetDir.mkdirs()) {
				throw new RuntimeException("Failed to create '" + targetDir.getPath() + "' directory.");
			}
		}
		return targetDir;
	}

	protected File createLibDir() {
		return createLibDir("lib");
	}

	protected File createLibDir(String libDirName) {
		File libDir = new File(this.outputDirectory, "target\\" + libDirName);
		if (!libDir.exists()) {
			if (!libDir.mkdirs()) {
				throw new RuntimeException("Failed to create '" + libDir.getPath() + "' directory.");
			}
		}
		return libDir;
	}

	//endregion


	//region 日志相关

	protected void emptyLine() {
		getLog().info("");
	}

	protected void debug(String content) {
		getLog().debug(content);
	}

	protected void debug(String content, Throwable cause) {
		getLog().debug(content, cause);
	}

	protected void debug(String format, Object... args) {
		Object[] newArgs = this.handleArgs(args);
		if (newArgs == args) {
			getLog().debug(String.format(format, args));
		} else {
			getLog().debug(String.format(format, args), (Throwable)args[args.length - 1]);
		}
	}

	protected void info(String content) {
		getLog().info(content);
	}

	protected void info(String content, Throwable cause) {
		getLog().info(content, cause);
	}


	protected void info(String format, Object... args) {
		Object[] newArgs = this.handleArgs(args);
		if (newArgs == args) {
			getLog().info(String.format(format, args));
		} else {
			getLog().info(String.format(format, args), (Throwable)args[args.length - 1]);
		}
	}

	protected void warn(String content) {
		getLog().warn(content);
	}

	protected void warn(String content, Throwable cause) {
		getLog().warn(content, cause);
	}

	protected void warn(String format, Object... args) {
		Object[] newArgs = this.handleArgs(args);
		if (newArgs == args) {
			getLog().warn(String.format(format, args));
		} else {
			getLog().warn(String.format(format, args), (Throwable)args[args.length - 1]);
		}
	}

	protected void error(String content) {
		getLog().error(content);
	}

	protected void error(String content, Throwable cause) {
		getLog().error(content, cause);
	}

	protected void error(String format, Object... args) {
		Object[] newArgs = this.handleArgs(args);
		if (newArgs == args) {
			getLog().error(String.format(format, args));
		} else {
			getLog().error(String.format(format, args), (Throwable)args[args.length - 1]);
		}
	}


	private Object[] handleArgs(Object... args) {
		if (args == null || args.length == 0) {
			return args;
		}

		if (args[args.length - 1] instanceof Throwable) {
			return Arrays.copyOf(args, args.length - 1);
		}

		return args;
	}

	protected String collectionToStr(Collection coll) {
		if (coll == null) {
			return "null";
		}

		if (coll.isEmpty()) {
			return "<empty>";
		}

		String collStr = coll.toString();
		return handleListStr(collStr.substring(1, collStr.length() - 1));
	}

	protected String handleListStr(String arrStr) {
		return arrStr.replaceAll("^|\\s*,\\s*", "\r\n         - ");
	}

	//endregion


	//region 文件操作

	protected void copyFile(File sourceFile, File targetFile) {
		try {
			IOUtils.copy(sourceFile, targetFile);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Copy file failed: %s -> %s", sourceFile.getPath(), targetFile.getPath()));
		}
	}

	protected void copyFileToDir(File sourceFile, File targetDir, boolean needLog) {
		copyFile(sourceFile, new File(targetDir, sourceFile.getName()));
		if (needLog) {
			this.info("Copy file '%s' to the directory '%s'.", sourceFile.getName(), targetDir.getPath());
		}
	}

	protected void copyFileToDir(File sourceFile, File targetDir) {
		this.copyFileToDir(sourceFile, targetDir, false);
	}

	protected void copyFilesToDir(Collection<File> sourceFiles, File targetDir, boolean needLog) {
		for (File sourceFile : sourceFiles) {
			this.copyFileToDir(sourceFile, targetDir, needLog);
		}
	}

	protected void copyFilesToDir(Collection<File> sourceFiles, File targetDir) {
		this.copyFilesToDir(sourceFiles, targetDir, false);
	}

	protected void copyFilesToDir2(Collection<Artifact> sourceArtifacts, File targetDir, boolean needLog) {
		for (Artifact sourceArtifact : sourceArtifacts) {
			this.copyFileToDir(sourceArtifact.getFile(), targetDir, needLog);
		}
	}

	protected void copyFilesToDir2(Collection<Artifact> sourceArtifacts, File targetDir) {
		this.copyFilesToDir2(sourceArtifacts, targetDir, false);
	}

	//endregion
}
