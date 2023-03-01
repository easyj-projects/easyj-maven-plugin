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
package icu.easyj.maven.plugin.mojo.simplify;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import icu.easyj.maven.plugin.mojo.utils.MatchUtils;
import icu.easyj.maven.plugin.mojo.utils.ObjectUtils;
import org.apache.maven.model.Dependency;

/**
 * 简化器配置
 *
 * @author wangliang181230
 * @since 0.4.2
 */
public class SimplifyPomMojoConfig {

	private final SimplifyPomMojo mojo;

	private SimplifyMode mode;


	public SimplifyPomMojoConfig(SimplifyPomMojo mojo) {
		this.mojo = mojo;
	}


	//region Getter、Setter

	public SimplifyMode getMode() {
		return mode;
	}

	public void setMode(SimplifyMode mode) {
		this.mode = mode;
	}

	public Boolean needRemoveParent() {
		return mojo.removeParent;
	}

	public boolean isOpenSourceProject() {
		return mojo.isOpenSourceProject;
	}

	public boolean isKeepProvidedDependencies() {
		return mojo.keepProvidedDependencies;
	}

	public boolean isKeepOptionalDependencies() {
		return mojo.keepOptionalDependencies;
	}

	public boolean isKeepTestDependencies() {
		return mojo.keepTestDependencies;
	}

	public boolean isExcludeDependency(Dependency dependency) {
		if (ObjectUtils.isEmpty(mojo.excludeDependencies)) {
			return false;
		}

		for (String exclude : mojo.excludeDependencies) {
			if (MatchUtils.match(exclude, dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())) {
				return true;
			}
		}

		return false;
	}

	public Set<String> getRemoveLocalProperties() {
		return mojo.removeLocalProperties == null ? Collections.emptySet() : mojo.removeLocalProperties;
	}

	public Map<String, String> getCreateProperties() {
		return mojo.createProperties;
	}

	public boolean isExpandImportDependencyManagement() {
		return mojo.expandImportDependencyManagement;
	}

	public String getArtifactNameTemplate() {
		return mojo.artifactNameTemplate;
	}

	//endregion
}
