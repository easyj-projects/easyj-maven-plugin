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
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 抽象的SpringBoot相关Mojo
 *
 * @author wangliang181230
 * @since 0.7.4
 */
public abstract class AbstractSpringBootMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${project.basedir}")
	protected File outputDirectory;


	protected String springBootMavenPluginVersion;


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!this.isSpringBootApplication()) {
			getLog().info("Skip this goal, cause by this project is not a spring-boot application.");
			return;
		}

		this.doExecute();
	}

	abstract void doExecute() throws MojoExecutionException, MojoFailureException;


	/**
	 * 判断是否为springboot应用
	 *
	 * @return 返回是否为springboot应用
	 */
	private boolean isSpringBootApplication() {
		if ("jar".equalsIgnoreCase(project.getPackaging())) {
			List<Plugin> plugins = project.getBuildPlugins();
			for (Plugin plugin : plugins) {
				if ("org.springframework.boot".equalsIgnoreCase(plugin.getGroupId())
						&& "spring-boot-maven-plugin".equalsIgnoreCase(plugin.getArtifactId())) {
					springBootMavenPluginVersion = plugin.getVersion();
					return true;
				}
			}
		}

		return false;
	}


	protected void emptyLine() {
		getLog().info("");
	}
}
