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
package icu.easyj.maven.plugin.mojo.simplify;

import java.io.File;

import icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.pom.PomSimplifier;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 根据originalModel重新生成pom.xml 的 Goal
 *
 * @author wangliang181230
 * @since 0.5.3
 */
@Mojo(name = "create-pom-file", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class CreatePomFileMojo extends AbstractSimplifyPomMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException {
		// Create simplified POM file
		getLog().info("Create the POM file '" + this.simplifiedPomFileName + "'.");

		IPomSimplifier pomSimplifier = new PomSimplifier(this.project, null, getLog());
		pomSimplifier.afterSimplify();

		File simplifiedPomFile = getSimplifiedPomFile();
		writePom(this.project.getOriginalModel(), simplifiedPomFile);

		getLog().info("Set the POM file '" + this.simplifiedPomFileName + "' to the project object.");
		project.setFile(simplifiedPomFile);
	}
}
