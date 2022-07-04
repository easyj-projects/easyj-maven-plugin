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
package icu.easyj.maven.plugin.mojo.skipinstalldeploy;

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 跳过 install和deploy 的goal
 *
 * @author wangliang181230
 * @since 0.6.3
 */
@Mojo(name = "skip-install-deploy", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class SkipInstallDeployMojo extends AbstractMojo {

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

		Properties properties = project.getProperties();
		if (skipInstall && !"true".equalsIgnoreCase(properties.getProperty("maven.install.skip"))) {
			properties.put("maven.install.skip", "true");
			getLog().info("Put property 'maven.install.skip = true'.");
		}

		if (skipDeploy && !"true".equalsIgnoreCase(properties.getProperty("maven.deploy.skip"))) {
			properties.put("maven.deploy.skip", "true");
			getLog().info("Put property 'maven.deploy.skip = true'.");
		}
	}
}
