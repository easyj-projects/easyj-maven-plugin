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

import icu.easyj.maven.plugin.mojo.AbstractEasyjMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 跳过 install和deploy 的goal
 *
 * @author wangliang181230
 * @since 0.6.3
 */
@Mojo(name = "skip-install-deploy", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class SkipInstallDeployMojo extends AbstractEasyjMojo {

	@Parameter(defaultValue = "true")
	private boolean skipInstall;

	@Parameter(defaultValue = "true")
	private boolean skipDeploy;


	@Override
	public void execute() throws MojoExecutionException {
		if (!skipInstall && !skipDeploy) {
			this.info("Skip this goal, cause by \"skipInstall == false && skipDeploy == false\".");
			return;
		}

		if (skipInstall && !this.containsProperty("maven.install.skip", "true")) {
			putProperty("maven.install.skip", "true");
		}

		if (skipDeploy && !this.containsProperty("maven.deploy.skip", "true")) {
			putProperty("maven.deploy.skip", "true");
		}
	}
}
