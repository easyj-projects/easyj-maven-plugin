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
package icu.easyj.maven.plugin.mojo.utils;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;

/**
 * 范围过滤器
 *
 * @author wangliang181230
 * @since 1.1.0
 */
public class ScopeFilter {

	private String includeScope;

	private String excludeScope;

	public ScopeFilter(String includeScope, String excludeScope) {
		this.includeScope = includeScope;
		this.excludeScope = excludeScope;
	}

	public boolean doFilter(Artifact artifact) {
		if (StringUtils.isNotEmpty(includeScope)) {
			if (!Artifact.SCOPE_COMPILE.equals(includeScope) && !Artifact.SCOPE_TEST.equals(includeScope)
					&& !Artifact.SCOPE_PROVIDED.equals(includeScope) && !Artifact.SCOPE_RUNTIME.equals(includeScope)
					&& !Artifact.SCOPE_SYSTEM.equals(includeScope)) {
				throw new RuntimeException("Invalid Scope in includeScope: " + includeScope);
			}

			if (Artifact.SCOPE_PROVIDED.equals(includeScope) || Artifact.SCOPE_SYSTEM.equals(includeScope)) {
				return includeSingleScope(artifact, includeScope);
			} else {
				ArtifactFilter saf = new ScopeArtifactFilter(includeScope);
				return saf.include(artifact);
			}
		} else if (StringUtils.isNotEmpty(excludeScope)) {
			if (!Artifact.SCOPE_COMPILE.equals(excludeScope) && !Artifact.SCOPE_TEST.equals(excludeScope)
					&& !Artifact.SCOPE_PROVIDED.equals(excludeScope) && !Artifact.SCOPE_RUNTIME.equals(excludeScope)
					&& !Artifact.SCOPE_SYSTEM.equals(excludeScope)) {
				throw new RuntimeException("Invalid Scope in excludeScope: " + excludeScope);
			}
			if (Artifact.SCOPE_TEST.equals(excludeScope)) {
				throw new RuntimeException(" Can't exclude Test scope, this will exclude everything.");
			} else if (!Artifact.SCOPE_PROVIDED.equals(excludeScope) && !Artifact.SCOPE_SYSTEM.equals(excludeScope)) {
				ArtifactFilter saf = new ScopeArtifactFilter(excludeScope);
				return !saf.include(artifact);
			} else {
				return excludeSingleScope(artifact, excludeScope);
			}
		}

		return false;
	}

	private boolean includeSingleScope(Artifact artifact, String scope) {
		return scope.equals(artifact.getScope());
	}

	private boolean excludeSingleScope(Artifact artifact, String scope) {
		return !includeSingleScope(artifact, scope);
	}
}
