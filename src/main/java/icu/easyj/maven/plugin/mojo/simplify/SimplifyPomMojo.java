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
import java.util.Map;
import java.util.Set;

import icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.PomSimplifierFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.AUTO;

/**
 * 简化 POM 的 Goal
 *
 * @author wangliang181230
 * @since 0.4.0
 */
@Mojo(name = "simplify-pom", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class SimplifyPomMojo extends AbstractSimplifyPomMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(property = "maven.simplify.skip", defaultValue = "false")
	private boolean skip;


	/**
	 * 简化模式
	 */
	@Parameter(property = "maven.simplify.mode", defaultValue = AUTO)
	private String simplifyMode;

	@Parameter(property = "maven.simplify.expandImportDependencyManagement", defaultValue = "false")
	boolean expandImportDependencyManagement;

	/**
	 * 是否更新POM文件
	 */
	@Parameter(defaultValue = "true")
	boolean updatePomFile;

	/**
	 * 是否用于开源项目
	 */
	@Parameter(property = "maven.simplify.isOpenSourceProject", defaultValue = "true")
	boolean isOpenSourceProject;

	@Parameter
	Boolean removeParent;

	@Parameter
	String artifactNameTemplate;


	@Parameter(defaultValue = "false")
	boolean keepProvidedDependencies;
	@Parameter(defaultValue = "false")
	boolean keepOptionalDependencies;

	@Parameter(defaultValue = "false")
	boolean keepTestDependencies;

	@Parameter
	Set<String> excludeDependencies;

	@Parameter(property = "maven.simplify.removeLocalProperties")
	Set<String> removeLocalProperties;

	@Parameter
	Map<String, String> createProperties;


	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("Simplify-POM has been skipped.");
			return;
		}

		// 读取配置
		SimplifyPomMojoConfig config = new SimplifyPomMojoConfig(this);

		// 创建简化器
		getLog().info("Create PomSimplifier by mode: " + this.simplifyMode);
		IPomSimplifier pomSimplifier = PomSimplifierFactory.create(this.project, this.simplifyMode, config, getLog());
		getLog().info("Do simplify by the POM simplifier: " + pomSimplifier.getClass().getSimpleName());
		getLog().info("");
		getLog().info("==================================  start simplify  ==================================");

		// 使用简化器处理pom.xml
		pomSimplifier.beforeSimplify();
		pomSimplifier.doSimplify();
		pomSimplifier.doSimplifyByConfig();
		pomSimplifier.afterSimplify();

		getLog().info("==================================   end  simplify  ==================================");
		getLog().info("");

		// Create simplified POM file
		getLog().info("Create the POM file '" + this.simplifiedPomFileName + "'.");

		File simplifiedPomFile = getSimplifiedPomFile();
		this.writePom(this.project.getOriginalModel(), simplifiedPomFile);

		if (updatePomFile) {
			getLog().info("Set the POM file '" + this.simplifiedPomFileName + "' to the project object.");
			project.setFile(simplifiedPomFile);
		}
	}
}
