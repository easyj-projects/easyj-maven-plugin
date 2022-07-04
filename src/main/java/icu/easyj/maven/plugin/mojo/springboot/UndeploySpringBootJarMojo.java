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

import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 设置不上传springboot打的包 的 Goal，因为springboot打的包并不是依赖包，无法作为依赖被引用，deploy了也没有用处，而且非常的大，很占用磁盘空间。
 *
 * @author wangliang181230
 * @since 0.5.9
 * @deprecated 请使用 {@link SpringBootExtendMojo}
 */
@Deprecated
@Mojo(name = "undeploy-spring-boot-jar", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class UndeploySpringBootJarMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;


	@Parameter(defaultValue = "true")
	private boolean skipInstall;

	@Parameter(defaultValue = "true")
	private boolean skipDeploy;


	@Override
	public void execute() throws MojoExecutionException {
		if (!skipInstall && !skipDeploy) {
			getLog().info("Skip this goal, cause by \"skipInstall == false && skipDeploy == false\".");
			return;
		}

		if (!"jar".equalsIgnoreCase(project.getPackaging())) {
			getLog().info("Skip this goal, cause by \"packaging != 'jar'\"'.");
			return;
		}

		boolean isSpringBootJar = false;

		List<Plugin> plugins = project.getBuildPlugins();
		for (Plugin plugin : plugins) {
			if ("org.springframework.boot".equalsIgnoreCase(plugin.getGroupId())
					&& "spring-boot-maven-plugin".equalsIgnoreCase(plugin.getArtifactId())) {
				isSpringBootJar = true;
				break;
			}
		}

		if (isSpringBootJar) {
			getLog().info("The current project is a springboot application, it will not be installed or deployed.");

			Properties properties = project.getProperties();
			if (skipInstall && !"true".equalsIgnoreCase(properties.getProperty("maven.install.skip"))) {
				properties.put("maven.install.skip", "true");
				getLog().info("Put property 'maven.install.skip = true'.");
			}

			if (skipDeploy && !"true".equalsIgnoreCase(properties.getProperty("maven.deploy.skip"))) {
				properties.put("maven.deploy.skip", "true");
				getLog().info("Put property 'maven.deploy.skip = true'.");
			}
		} else {
			getLog().info("Skip this goal, cause by this project is not a spring-boot application.");
		}
	}
}
