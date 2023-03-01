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
package icu.easyj.maven.plugin.mojo.skipinstalldeploy;

import java.util.Properties;

import icu.easyj.maven.plugin.mojo.AbstractEasyjMojo;
import icu.easyj.maven.plugin.mojo.utils.StringUtils;
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
@Mojo(name = "skip-install-deploy", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class SkipInstallDeployMojo extends AbstractEasyjMojo {

	@Parameter
	private Boolean skipInstallAndDeploy;

	@Parameter
	private Boolean skipInstall;

	@Parameter
	private Boolean skipDeploy;


	@Override
	public void execute() throws MojoExecutionException {
		// 以下这三个properties，仅对当前POM有效。'isTrue(key)' 方法中，获取的是 'originalModel' 中的 `Properties`
		boolean skipInstallAndDeploy = this.skipInstallAndDeploy != null ? this.skipInstallAndDeploy : Boolean.TRUE.equals(isTrue("maven.easyj.skipInstallAndDeploy"));

		Boolean skipInstall = this.skipInstall != null ? this.skipInstall : isTrue("maven.easyj.skipInstall");
		Boolean skipDeploy = this.skipDeploy != null ? this.skipDeploy : isTrue("maven.easyj.skipDeploy");

		if (skipInstall != null ? skipInstall : skipInstallAndDeploy) {
			putProperty("maven.install.skip", "true");
		}
		if (skipDeploy != null ? skipDeploy : skipInstallAndDeploy) {
			putProperty("maven.deploy.skip", "true");
		}
	}

	public Boolean isTrue(String originalPropertyKey) {
		Properties originalProperties = getOriginalProperties();
		if (originalProperties == null) {
			return null;
		}

		String propertyValue = originalProperties.getProperty(originalPropertyKey);
		if (StringUtils.isEmpty(propertyValue)) {
			return null;
		}

		return "true".equalsIgnoreCase(propertyValue);
	}
}
