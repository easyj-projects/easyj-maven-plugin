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
package icu.easyj.maven.plugin.mojo.simplify.simplifier;

import icu.easyj.maven.plugin.mojo.simplify.SimplifyMode;
import icu.easyj.maven.plugin.mojo.simplify.SimplifyPomMojoConfig;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.jar.JarPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.jar.ShadeJarPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.jar.StarterPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.mavenplugin.MavenPluginPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.noop.NoopPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.pom.BomPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.pom.DependenciesPomSimplifier;
import icu.easyj.maven.plugin.mojo.simplify.simplifier.pom.PomSimplifier;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.AUTO;
import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.BOM;
import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.DEPENDENCIES;
import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.JAR;
import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.MAVEN_PLUGIN;
import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.POM;
import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.SHADE;
import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.STARTER;
import static icu.easyj.maven.plugin.mojo.simplify.simplifier.IPomSimplifier.WAR;

/**
 * POM简化器工厂类
 *
 * @author wangliang181230
 * @since 0.4.0
 */
public abstract class PomSimplifierFactory {

	public static AbstractPomSimplifier create(MavenProject project, String modeStr, SimplifyPomMojoConfig config, Log log) {
		SimplifyMode mode = null;

		// auto模式时，自动根据构建标识判断
		if (AUTO.equalsIgnoreCase(modeStr)) {
			modeStr = null;

			if (isBom(project)) {
				modeStr = BOM;
				log.info("Set mode to '" + modeStr + "'," +
						" because the artifactId \"" + project.getArtifactId() + "\".endsWith(\"-bom\").");
			} else if (isShade(project)) {
				modeStr = SHADE;
				log.info("Set mode to '" + modeStr + "'," +
						" because the artifactId \"" + project.getArtifactId() + "\".endsWith(\"-all\").");
			} else if (isStarter(project)) {
				modeStr = STARTER;
				if (project.getArtifactId().endsWith("-starter")) {
					log.info("Set mode to '" + modeStr + "'," +
							" because the artifactId \"" + project.getArtifactId() + "\".endsWith(\"-starter\").");
				} else {
					log.info("Set mode to '" + modeStr + "'," +
							" because the artifactId \"" + project.getArtifactId() + "\".contains(\"-starter-\").");
				}
			}
		}

		if (modeStr == null || modeStr.isEmpty()) {
			modeStr = project.getPackaging();
		} else {
			// 校验模式是否与当前pom匹配，如果不匹配，则打印warn日志
			switch (modeStr.toLowerCase().replace('_', '-')) {
				case POM:
				case JAR:
				case WAR:
				case MAVEN_PLUGIN:
					if (!modeStr.equalsIgnoreCase(project.getPackaging())) {
						printWarnLog(project, modeStr, log);
					}
					break;
				case SHADE:
					if (!isJar(project)) {
						printWarnLog(project, modeStr, log);
					}
					break;
				case DEPENDENCIES:
				case BOM:
					if (!isPom(project)) {
						printWarnLog(project, modeStr, log);
					}
					break;
				case STARTER:
					if (!isJarOrPom(project)) {
						printWarnLog(project, modeStr, log);
					}
					break;
				default:
					break;
			}
		}

		// string to enum
		try {
			mode = SimplifyMode.getByModeStr(modeStr);
		} catch (Exception e) {
			log.warn("Get the mode by string '" + modeStr + "' failed: " + e);
		}

		if (mode == null) {
			mode = SimplifyMode.NOOP;
		}

		log.info("The simplify mode is: " + mode);

		config.setMode(mode);
		return createInternal(project, config, log);
	}


	private static void printWarnLog(MavenProject project, String modeStr, Log log) {
		log.warn("The mode '" + modeStr + "' can't used for packaging '" + project.getPackaging() + "'.");
	}

	private static AbstractPomSimplifier createInternal(MavenProject project, SimplifyPomMojoConfig config, Log log) {
		switch (config.getMode()) {
			case JAR:
			case WAR:
				return new JarPomSimplifier(project, config, log);
			case STARTER:
				return new StarterPomSimplifier(project, config, log);
			case SHADE:
				return new ShadeJarPomSimplifier(project, config, log);

			case MAVEN_PLUGIN:
				return new MavenPluginPomSimplifier(project, config, log);

			case POM:
				return new PomSimplifier(project, config, log);
			case DEPENDENCIES:
				return new DependenciesPomSimplifier(project, config, log);
			case BOM:
				return new BomPomSimplifier(project, config, log);

			case NOOP:
			case NONE:
			default:
				return new NoopPomSimplifier(project, config, log);
		}
	}


	private static boolean isJar(MavenProject project) {
		return JAR.equalsIgnoreCase(project.getPackaging());
	}

	private static boolean isPom(MavenProject project) {
		return POM.equalsIgnoreCase(project.getPackaging());
	}

	private static boolean isJarOrPom(MavenProject project) {
		return isJar(project) || isPom(project);
	}


	private static boolean isBom(MavenProject project) {
		return isPom(project) && project.getArtifactId().endsWith("-bom");
	}

	private static boolean isStarter(MavenProject project) {
		return isJarOrPom(project) && (project.getArtifactId().endsWith("-starter") || project.getArtifactId().contains("-starter-"));
	}

	private static boolean isShade(MavenProject project) {
		return isJar(project) && project.getArtifactId().endsWith("-all");
	}
}
