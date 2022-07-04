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

import javax.annotation.Nullable;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * maven-shade-plugin 兼容 flatten-maven-plugin（resolveCiFriendliesOnly模式） 的 Goal
 *
 * @author wangliang181230
 * @since 0.3.9
 * @deprecated 0.4.2 直接使用 {@link SimplifyPomMojo} 替换掉 flatten 插件，不再需要此插件。
 */
@Deprecated
@Mojo(name = "shade-compatible-flatten", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class ShadeCompatibleFlattenMojo extends AbstractMojo {

	private static final String REVISION = "${revision}";

	private static final String GROUP_ID = "icu.easyj.maven.plugins";
	private static final String ARTIFACT_ID = "easyj-maven-plugin";


	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "false")
	private boolean skip;

	@Parameter(defaultValue = "false")
	private boolean removeCurrentPluginFromPom;


	@Override
	public void execute() {
		try {
			if (skip) {
				getLog().info("Shade-Compatible-Flatten has been skipped.");
				return;
			}

			Parent originalModelParent = this.getModelParent(project.getOriginalModel());
			Parent modelParent = this.getModelParent(project.getModel());

			if (originalModelParent != null && REVISION.equals(originalModelParent.getVersion())
					&& modelParent != null && modelParent.getVersion() != null
					&& !REVISION.equals(modelParent.getVersion())) {
				getLog().info("Set the version of the original model parent" +
						" from '" + originalModelParent.getVersion() + "' to '" + modelParent.getVersion() + "'.");
				originalModelParent.setVersion(modelParent.getVersion());
			} else {
				getLog().info("There are no conflicts to deal with.");
			}
		} finally {
			if (removeCurrentPluginFromPom) {
				removeCurrentPlugin();
			}
		}
	}

	@Nullable
	private Parent getModelParent(Model model) {
		return model == null ? null : model.getParent();
	}

	private void removeCurrentPlugin() {
		getLog().info("Remove current plugin from the POM file.");

		try {
			this.project.getModel().getBuild().getPlugins().removeIf(this::checkIsCurrentPlugin);
		} catch (NullPointerException e) {
			// do nothing
		}

		try {
			this.project.getOriginalModel().getBuild().getPlugins().removeIf(this::checkIsCurrentPlugin);
		} catch (NullPointerException e) {
			// do nothing
		}
	}

	private boolean checkIsCurrentPlugin(Plugin p) {
		return GROUP_ID.equals(p.getGroupId()) && ARTIFACT_ID.equals(p.getArtifactId());
	}
}
