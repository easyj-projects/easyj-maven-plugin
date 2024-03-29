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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 用于清理 {@link SimplifyPomMojo} 生成的POM文件
 *
 * @author wangliang181230
 * @since 0.4.0
 */
@Mojo(name = "clean-simplify-pom", defaultPhase = LifecyclePhase.CLEAN, threadSafe = true)
public class CleanSimplifyPomMojo extends AbstractSimplifyPomMojo {

	public void execute() throws MojoExecutionException {
		File simplifiedPomFile = getSimplifiedPomFile();
		if (simplifiedPomFile.isFile()) {
			getLog().info("Deleting " + simplifiedPomFile.getPath());
			boolean deleted = simplifiedPomFile.delete();
			if (!deleted) {
				throw new MojoExecutionException("Could not delete " + simplifiedPomFile.getAbsolutePath());
			}
		}
	}

}
