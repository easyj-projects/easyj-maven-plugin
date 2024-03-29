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
package icu.easyj.maven.plugin.mojo.simplify.simplifier.jar;

import java.util.function.Function;

import icu.easyj.maven.plugin.mojo.simplify.SimplifyPomMojoConfig;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.AbstractPomSimplifier;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * JAR的pom.xml 简化器
 *
 * @author wangliang181230
 * @since 0.4.0
 */
public class JarPomSimplifier extends AbstractPomSimplifier {

	public JarPomSimplifier(MavenProject project, SimplifyPomMojoConfig config, Log log) {
		super(project, config, log);
	}


	@Override
	public void doSimplify() {
		this.removeParent();

		this.copyProjectInfoFromParentForOpenSourceProject();

		this.removeDependencyManagement();
		this.resetDependencies();

		this.removeProperties();

		this.removePrerequisites();
		this.removeBuild();
		this.removeReporting();
		this.removeReports();

		this.removeRepositories();
		this.removePluginRepositories();
		this.removeDistributionManagement();

		this.removeProfiles();
	}

	@Override
	protected Function<String, String> getReplaceVariableFunction() {
		this.log.info(" - Optimize with 'replaceVariable'");
		return this::replaceVariable;
	}
}
