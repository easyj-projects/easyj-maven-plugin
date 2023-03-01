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
package icu.easyj.maven.plugin.mojo.print;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 打印project信息 的 Goal
 *
 * @author wangliang181230
 * @since 0.5.4
 */
@Mojo(name = "print-project", threadSafe = true)
public class PrintProjectMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(property = "maven.print.skip", defaultValue = "false")
	private boolean skip;

	@Parameter(defaultValue = "true")
	private boolean printModel;

	@Parameter(defaultValue = "true")
	private boolean printOriginalModel;

	@Parameter(property = "maven.print.detail", defaultValue = "false")
	private boolean printDetail;

	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("Print-Project has been skipped.");
			return;
		}

		if (this.project == null) {
			getLog().info("The project is null");
			return;
		}

		if (this.printModel) {
			this.printModelStr(this.project.getModel(), "model");
		}

		getLog().info("");
		getLog().info("");
		getLog().info("=====================================================================================================================");
		getLog().info("");
		getLog().info("");

		if (this.printOriginalModel) {
			this.printModelStr(this.project.getOriginalModel(), "originalModel");
		}
	}

	private void printModelStr(Model m, String modelName) {
		printLine();

		try {
			if (m == null) {
				getLog().info("Print the project." + modelName + ": null");
				return;
			}

			getLog().info("Print the project." + modelName + ":");

			// 打印 parent
			Parent parent = m.getParent();
			if (parent != null) {
				getLog().info(String.format("parent: %s, %s", parent.getId(), parent.getRelativePath() != null && parent.getRelativePath().isEmpty() ? "<empty>" : parent.getRelativePath()));
			} else {
				getLog().info("parent: null");
			}

			// 打印 artifact
			getLog().info(String.format("artifact: %s", m.getId()));


			// 打印 properties
			if (m.getProperties() == null) {
				getLog().info("properties: null");
			} else if (m.getProperties().isEmpty()) {
				getLog().info("properties: 0");
			} else {
				Properties properties = m.getProperties();
				if (this.printDetail) printLine();
				getLog().info("properties: " + properties.size());
				if (this.printDetail) {
					for (Map.Entry<Object, Object> entry : properties.entrySet()) {
						getLog().info("  "
								+ (entry.getKey() == null ? "<null>" : entry.getKey())
								+ " = "
								+ (entry.getValue() == null ? "<null>" : entry.getValue()));
					}
					printLine();
				}
			}


			// 打印 dependencyManagement
			if (m.getDependencyManagement() == null) {
				getLog().info("dependencyManagement: null");
			} else if (m.getDependencyManagement().getDependencies() == null) {
				getLog().info("dependencyManagement.dependencies: null");
			} else if (m.getDependencyManagement().getDependencies().isEmpty()) {
				getLog().info("dependencyManagement.dependencies: 0");
			} else {
				List<Dependency> dependencies = m.getDependencyManagement().getDependencies();
				if (this.printDetail) printLine();
				getLog().info("dependencyManagement: " + dependencies.size());
				if (this.printDetail) {
					this.printDependencies(dependencies);
					printLine();
				}
			}

			// 打印 dependencies
			if (m.getDependencies() == null) {
				getLog().info("dependencies: null");
			} else {
				List<Dependency> dependencies = m.getDependencies();
				if (this.printDetail) printLine();
				getLog().info("dependencies: " + dependencies.size());
				if (this.printDetail) {
					this.printDependencies(dependencies);
					printLine();
				}
			}

			// 打印 build
			if (m.getBuild() == null) {
				getLog().info("build: null");
				getLog().info("build.pluginManagement: null");
				getLog().info("build.plugins: null");
			} else {
				getLog().info("build:");
				if (m.getBuild().getPluginManagement() == null) {
					getLog().info("build.pluginManagement: null");
				} else if (m.getBuild().getPluginManagement().getPlugins() == null) {
					getLog().info("build.pluginManagement.plugins: null");
				} else if (m.getBuild().getPluginManagement().getPlugins().isEmpty()) {
					getLog().info("build.pluginManagement.plugins: 0");
				} else {
					if (this.printDetail) printLine();
					getLog().info("build.pluginManagement.plugins: " + m.getBuild().getPluginManagement().getPlugins().size());
					if (this.printDetail) {
						for (Plugin plugin : m.getBuild().getPluginManagement().getPlugins()) {
							String id = plugin.getId();
							String config = (plugin.getConfiguration() == null ? null : plugin.getConfiguration().toString().replace("\r", "").replace("\n", "\r\n" + createStr(' ', id.length() + 14)));
							getLog().info("  " + id + " --- " + config);
						}

						printLine();
					}
				}

				if (m.getBuild().getPlugins() == null) {
					getLog().info("build.plugins: null");
				} else if (m.getBuild().getPlugins().isEmpty()) {
					getLog().info("build.plugins: 0");
				} else {
					if (this.printDetail) printLine();
					getLog().info("build.plugins: " + m.getBuild().getPlugins().size());
					if (this.printDetail) {
						for (Plugin plugin : m.getBuild().getPlugins()) {
							String id = plugin.getId();
							String config = (plugin.getConfiguration() == null ? null : plugin.getConfiguration().toString().replace("\r", "").replace("\n", "\r\n" + createStr(' ', id.length() + 14)));
							getLog().info("  " + id + " --- " + config);
						}
						printLine();
					}
				}
			}

			// 打印 di
		} finally {
			printLine();
		}
	}

	private String createStr(char c, int length) {
		StringBuilder sb = new StringBuilder(length);
		while (length > 0) {
			sb.append(c);
			length--;
		}
		return sb.toString();
	}

	private void printDependencies(List<Dependency> dependencies) {
		if (dependencies == null || dependencies.isEmpty()) {
			return;
		}

		for (Dependency dependency : dependencies) {
			getLog().info("  " + dependency.getGroupId()
					+ ":" + dependency.getArtifactId()
					+ ":" + dependency.getVersion()
					+ ":" + dependency.getType()
					+ (dependency.getClassifier() != null ? ":" + dependency.getClassifier() : "")
					+ " : " + dependency.getScope()
					+ " : " + dependency.getOptional()
					+ " : " + dependency.getSystemPath()
					+ " : " + (dependency.getExclusions() == null ? "null" : dependency.getExclusions().size())
			);
		}
	}

	private void printLine() {
		this.getLog().info("-------------------------------------------");
	}
}
