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
package icu.easyj.maven.plugin.mojo.simplify.simplifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import icu.easyj.maven.plugin.mojo.simplify.SimplifyPomMojoConfig;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static icu.easyj.maven.plugin.mojo.utils.ObjectUtils.isEmpty;
import static icu.easyj.maven.plugin.mojo.utils.ObjectUtils.isNotEmpty;

/**
 * 抽象POM 简化器
 *
 * @author wangliang181230
 * @since 0.4.0
 */
public abstract class AbstractPomSimplifier implements IPomSimplifier {

	protected final Log log;

	protected final SimplifyPomMojoConfig config;


	protected final MavenProject project;
	protected final MavenProject parent;

	protected final Model originalModel;
	protected final Parent originalModelParent;
	protected final List<Dependency> originalDependencies;

	protected final Model model;
	protected final Parent modelParent;


	private boolean isCopiedParentItems = false;
	private boolean isCopiedParentItemsForOpenSourceProject = false;
	private boolean isResetDependencies = false;


	protected AbstractPomSimplifier(MavenProject project, SimplifyPomMojoConfig config, Log log) {
		this.project = project;
		this.parent = project.getParent();
		this.originalModel = project.getOriginalModel();
		this.originalModelParent = this.originalModel.getParent();
		this.originalDependencies = originalModel.getDependencies();
		this.model = project.getModel();
		this.modelParent = this.model.getParent();

		this.config = config;

		this.log = log;
	}


	@Override
	public void afterSimplify() {
		this.replaceParentRevision();
		this.removeGroupIdAndVersionIfEqualsToParent();
		this.optimizeDependencyManagement();
		this.optimizeDependencies();
	}

	/**
	 * 根据配置进行一些操作
	 */
	@Override
	public void doSimplifyByConfig() {
		this.copyProjectInfoFromParentForOpenSourceProject();
		this.createPropertiesByConfig();
	}


	private void copyParentItems(String... itemNameArr) {
		for (String itemName : itemNameArr) {
			try {
				itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);

				Method getMethod = Model.class.getMethod("get" + itemName);

				Object originalValue = getMethod.invoke(this.originalModel);
				Object value = this.findParentValue(this.project.getParent(), getMethod);

				if (isEmpty(originalValue) && isNotEmpty(value)) {
					this.log.info("   Copy " + itemName + ".");
					Method setMethod = Model.class.getMethod("set" + itemName, value instanceof List ? List.class : (value instanceof Map ? Map.class : value.getClass()));
					setMethod.invoke(this.originalModel, value);
				}
			} catch (Exception e) {
				this.log.warn("   Copy " + itemName + " failed:", e);
			}
		}
	}

	private Object findParentValue(MavenProject parent, Method getMethod) throws InvocationTargetException, IllegalAccessException {
		if (parent == null) {
			return null;
		}
		Object value = getMethod.invoke(parent.getOriginalModel());
		if (isEmpty(value)) {
			return findParentValue(parent.getParent(), getMethod);
		}
		return value;
	}

	protected int getDependenciesSize(DependencyManagement dm) {
		if (dm == null) {
			return 0;
		}
		return getDependenciesSize(dm.getDependencies());
	}

	protected int getDependenciesSize(List<Dependency> dependencies) {
		if (dependencies == null) {
			return 0;
		}
		return dependencies.size();
	}

	protected String dependencyToString(Dependency dependency) {
		if (dependency == null) {
			return "null";
		}

		return "[" + dependency.getGroupId()
				+ ":" + dependency.getArtifactId()
				+ ":" + dependency.getVersion()
				+ (isEmpty(dependency.getType()) || JAR.equalsIgnoreCase(dependency.getType()) ? "" : ":" + dependency.getType())
				+ (isEmpty(dependency.getClassifier()) ? "" : ":" + dependency.getClassifier())
				+ (isEmpty(dependency.getScope()) ? "" : ":" + dependency.getScope())
				+ (isEmpty(dependency.getOptional()) ? "" : ":" + dependency.getOptional())
				+ "]";
	}

	protected String replaceVariable(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}

		while (str.contains("${") && str.contains("}")) {
			int start = str.indexOf("${");
			int end = str.indexOf("}");
			String varName = str.substring(start + 2, end).trim();
			String varValue = this.getProperty(varName);
			varValue = varValue == null ? "" : varValue.trim();
			str = str.substring(0, start) + varValue + str.substring(end + 1);
		}

		return str;
	}

	protected String getProjectProperty(String key) {
		if (key == null || key.isEmpty()) {
			return key;
		}

		switch (key.toLowerCase()) {
			// project
			case "project.groupid":
			case "${project.groupid}":
				return this.project.getGroupId();
			case "project.artifactid":
			case "${project.artifactid}":
				return this.project.getArtifactId();
			case "project.version":
			case "${project.version}":
				return this.project.getVersion();

			// parent
			case "project.parent.groupid":
			case "${project.parent.groupid}":
			case "parent.groupid":
			case "${parent.groupid}":
				return this.project.getParent() != null ? this.project.getParent().getGroupId() : this.project.getGroupId();
			case "project.parent.artifactid":
			case "${project.parent.artifactid}":
			case "parent.artifactid":
			case "${parent.artifactid}":
				return this.project.getParent() != null ? this.project.getParent().getArtifactId() : this.project.getArtifactId();
			case "project.parent.version":
			case "${project.parent.version}":
			case "parent.version":
			case "${parent.version}":
				return this.project.getParent() != null ? this.project.getParent().getVersion() : this.project.getVersion();

			default:
				return key;
		}
	}

	protected String getProperty(String key) {
		if (key == null || key.isEmpty()) {
			return key;
		}

		String value = this.getProjectProperty(key);
		// 如果不相等，说明是获取到了项目属性
		if (!key.equals(value)) {
			return value;
		}

		// properties
		Properties properties = this.model.getProperties();
		if (properties == null || properties.isEmpty()) {
			return null;
		}
		return this.model.getProperties().getProperty(key);
	}

	protected void printLine() {
		this.log.info("-------------------------------------------");
	}


	//region # 对POM中各元素的操作


	//region --- Parent ---

	/**
	 * 移除Parent
	 */
	public void removeParent() {
		if (this.originalModel.getParent() != null && !Boolean.FALSE.equals(this.config.needRemoveParent())) {
			this.log.info("Remove Parent.");
			this.originalModel.setParent(null);
		}
		this.resetArtifactIdentification();
		this.resetNameAndDescription();
	}

	public void removeParentByConfig() {
		if (this.originalModel.getParent() != null && Boolean.TRUE.equals(this.config.needRemoveParent())) {
			this.removeParent();
		}
	}

	/**
	 * 替换Parent的版本号表达式 '${revision}' 为具体的版本号
	 */
	public void replaceParentRevision() {
		// replace parent version
		Parent originalParent = this.originalModel.getParent();
		if (originalParent != null) {
			if (REVISION.equals(originalParent.getVersion())) {
				this.log.info("Set parent version from '" + originalParent.getVersion() + "' to '" + this.modelParent.getVersion() + "'.");
				originalParent.setVersion(this.modelParent.getVersion());
			}
		}

		// remove the property named 'revision', if the value is equals to the version of current project.
		Properties originalProperties = this.originalModel.getProperties();
		if (isNotEmpty(originalProperties) && originalProperties.containsKey(REVISION_KEY)
				&& this.model.getVersion().equals(this.model.getProperties().getProperty(REVISION_KEY))) {
			this.log.info("Remove the special property '<" + REVISION_KEY + ">'.");
			originalProperties.remove(REVISION_KEY);
		}
	}

	public void removeParentRelativePath() {
		if (this.originalModel.getParent() != null && this.originalModel.getParent().getRelativePath() != null) {
			this.log.info("Remove Parent RelativePath.");
			this.originalModel.getParent().setRelativePath(null);
		}
	}

	//endregion ##


	//region -------------------- GroupId、ArtifactId、Version、Packaging --------------------

	public void resetArtifactIdentification() {
		this.removeGroupIdAndVersionIfEqualsToParent();

		if (!this.model.getGroupId().equals(this.originalModel.getGroupId())) {
			this.log.info("Set GroupId from '" + this.originalModel.getGroupId() + "' to '" + this.model.getGroupId() + "'.");
			this.originalModel.setGroupId(this.model.getGroupId());
		}
		if (!this.model.getArtifactId().equals(this.originalModel.getArtifactId())) {
			this.log.info("Set ArtifactId from '" + this.originalModel.getArtifactId() + "' to '" + this.model.getArtifactId() + "'.");
			this.originalModel.setArtifactId(this.model.getArtifactId());
		}
		if (!this.model.getVersion().equals(this.originalModel.getVersion())) {
			this.log.info("Set Version from '" + this.originalModel.getVersion() + "' to '" + this.model.getVersion() + "'.");
			this.originalModel.setVersion(this.model.getVersion());
		}
		if (isNotEmpty(this.model.getPackaging()) && !this.model.getPackaging().equals(this.originalModel.getPackaging()) && !JAR.equalsIgnoreCase(this.model.getPackaging())) {
			this.log.info("Set Packaging from '" + this.originalModel.getPackaging() + "' to '" + this.model.getPackaging() + "'.");
			this.originalModel.setPackaging(this.model.getPackaging());
		}
	}

	public void resetVersion() {
		if (isNotEmpty(this.originalModel.getVersion()) && !this.model.getVersion().equals(this.originalModel.getVersion())) {
			this.log.info("Set Version from '" + this.originalModel.getVersion() + "' to '" + this.model.getVersion() + "'.");
			this.originalModel.setVersion(this.model.getVersion());
		}
	}

	public void removeGroupIdAndVersionIfEqualsToParent() {
		if (this.originalModel.getParent() == null) {
			return;
		}

		Parent parent = this.model.getParent();
		if (parent.getGroupId().equals(this.model.getGroupId()) && isNotEmpty(this.originalModel.getGroupId())) {
			this.log.info("Remove GroupId, because it's equals to the groupId of the parent.");
			this.originalModel.setGroupId(null);
		}
		if (parent.getVersion().equals(this.model.getVersion()) && isNotEmpty(this.originalModel.getVersion())) {
			this.log.info("Remove Version, because it's equals to the version of the parent.");
			this.originalModel.setVersion(null);
		}
	}

	//endregion ##


	//region -------------------- Name、Description --------------------

	public void resetNameAndDescription() {
		if (isNotEmpty(this.model.getName()) && isNotEmpty(this.originalModel.getName()) && !this.model.getName().equals(this.originalModel.getName())) {
			this.log.info("Set Name from '" + this.originalModel.getName() + "' to '" + this.model.getName() + "'.");
			this.originalModel.setName(this.model.getName());
		}
		if (isNotEmpty(this.model.getDescription()) && isNotEmpty(this.originalModel.getDescription()) && !this.model.getDescription().equals(this.originalModel.getDescription())) {
			this.log.info("Set Description from '" + this.originalModel.getDescription() + "' to '" + this.model.getDescription() + "'.");
			this.originalModel.setDescription(this.model.getDescription());
		}
	}

	//endregion ##


	//region -------------------- Organization、Url、Licenses、Developers、Scm、IssueManagement --------------------

	public void copyProjectInfoFromParentForOpenSourceProject() {
		if (this.isCopiedParentItemsForOpenSourceProject || !this.config.isOpenSourceProject() || this.originalModel.getParent() != null) {
			return;
		}
		this.isCopiedParentItemsForOpenSourceProject = true;

		printLine();
		this.log.info("Copy project info from parent for the open source project:");

		String[] itemNameArr = new String[]{
				// 开源必须
				"Url",
				"Licenses",
				"Developers",
				"Scm",

				// 开源非必须，但加着比较好
				"Organization",
				"IssueManagement"
		};
		this.copyParentItems(itemNameArr);
		printLine();
	}

	//endregion ##


	//region -------------------- InceptionYear、Contributors、MailingLists、CiManagement --------------------

	public void copyProjectInfoFromParent() {
		if (this.isCopiedParentItems || !this.config.isOpenSourceProject() || this.originalModel.getParent() != null) {
			return;
		}
		this.isCopiedParentItems = true;

		printLine();
		this.log.info("Copy project info from parent:");

		String[] itemNameArr = new String[]{
				"InceptionYear",
				"Contributors",
				"MailingLists",
				"CiManagement"
		};
		this.copyParentItems(itemNameArr);

		this.copyProjectInfoFromParentForOpenSourceProject();
		printLine();
	}

	//endregion ##


	//region -------------------- DependencyManagement、Dependencies --------------------

	public void removeDependencyManagement() {
		if (this.originalModel.getDependencyManagement() != null) {
			this.resetDependencies();
			this.log.info("Remove DependencyManagement.");
			this.originalModel.setDependencyManagement(null);
		} else {
			this.resetDependencies();
		}
	}

	/**
	 * 目前仅用于BOM模式
	 */
	public void resetDependencyManagement() {
		if (this.model.getDependencyManagement() != null && isNotEmpty(this.model.getDependencyManagement().getDependencies())) {
			if (this.config.isExpandImportDependencyManagement()) {
				int originalSize = this.getDependenciesSize(this.originalModel.getDependencyManagement());
				int size = this.getDependenciesSize(this.model.getDependencyManagement());
				this.log.info("Reset DependencyManagement: " + originalSize + " -> " + size);

				// 复制一份DependencyManagement出来，避免对maven的运行造成影响
				DependencyManagement originalDependencyManagement = new DependencyManagement();
				List<Dependency> dependenciesForDependencyManagement = new ArrayList<>();
				for (Dependency dependency : this.model.getDependencyManagement().getDependencies()) {
					dependenciesForDependencyManagement.add(this.copyDependency(dependency));
				}
				originalDependencyManagement.setDependencies(dependenciesForDependencyManagement);

				this.originalModel.setDependencyManagement(originalDependencyManagement);
			}
		} else {
			this.log.warn("In BOM mode, the <dependencyManagement> cannot be null or empty, otherwise the POM will be meaningless.");
		}
	}

	public void optimizeDependencyManagement() {
		DependencyManagement dm = this.originalModel.getDependencyManagement();
		if (dm == null || isEmpty(dm.getDependencies())) {
			return;
		}

		this.printLine();
		this.log.info("Optimize DependencyManagement: (" + dm.getDependencies().size() + ")");
		this.optimizeDependencies(dm.getDependencies());
		this.printLine();
	}

	public void removeDependencies() {
		if (isNotEmpty(this.originalModel.getDependencies())) {
			this.log.info("Remove Dependencies.");
			this.originalModel.setDependencies(null);
		}
	}

	public void clearDependencyScopeCompileAndOptionalFalse(Dependency dependency) {
		if (dependency.getScope() != null && "compile".equalsIgnoreCase(dependency.getScope())) {
			this.log.info("  Set scope from '" + dependency.getScope() + "' to null: " + dependency.getManagementKey());
			dependency.setScope(null);
		}
		if (dependency.getOptional() != null && !"true".equalsIgnoreCase(dependency.getOptional())) {
			this.log.info("  Set optional from '" + dependency.getOptional() + "' to null: " + dependency.getManagementKey());
			dependency.setOptional(null);
		}
	}

	public void clearDependencyScopeCompileAndOptionalFalse(List<Dependency> dependencies) {
		if (isEmpty(dependencies)) {
			return;
		}

		for (Dependency dependency : dependencies) {
			this.clearDependencyScopeCompileAndOptionalFalse(dependency);
		}
	}

	public void resetDependencies() {
		if (isResetDependencies) {
			return;
		}
		isResetDependencies = true;

		if (isEmpty(this.originalModel.getDependencies())) {
			return;
		}

		int originalSize = getDependenciesSize(this.originalModel.getDependencies());
		printLine();
		this.log.info("Reset dependencies: groupId, version, exclusions (Contains " + this.originalModel.getDependencies().size() + " dependencies)");
		for (int i = 0, n = 0; i < this.model.getDependencies().size(); i++, n++) {
			Dependency dependency = this.model.getDependencies().get(i);
			if (i < originalSize) {
				Dependency originalDependency = this.originalModel.getDependencies().get(n);
				String beforeDependencyStr = dependencyToString(originalDependency);

				// reset groupId and artifactId
				originalDependency.setGroupId(this.replaceVariable(originalDependency.getGroupId()));
				originalDependency.setArtifactId(this.replaceVariable(originalDependency.getArtifactId()));

				// 重置groupId、version和exclusions
				if (dependency.getGroupId().equals(originalDependency.getGroupId())
						&& dependency.getArtifactId().equals(originalDependency.getArtifactId())) {
					//region 判断是否需要移除

					if (!this.config.isKeepProvidedDependencies() && "provided".equalsIgnoreCase(dependency.getScope())) {
						this.removeOneDependencies(dependency, n--, "scope=provided");
						continue;
					}

					if (!this.config.isKeepTestDependencies() && "test".equalsIgnoreCase(dependency.getScope())) {
						this.removeOneDependencies(dependency, n--, "scope=test");
						continue;
					}

					if (!this.config.isKeepOptionalDependencies() && dependency.isOptional()) {
						this.removeOneDependencies(dependency, n--, "optional=true");
						continue;
					}

					if (this.config.isExcludeDependency(dependency)) {
						this.removeOneDependencies(dependency, n--, "isExclude=true");
						continue;
					}

					//endregion

					originalDependency.setType(dependency.getType());
					originalDependency.setVersion(dependency.getVersion());
					originalDependency.setClassifier(dependency.getClassifier());

					originalDependency.setScope("compile".equalsIgnoreCase(dependency.getScope()) ? null : dependency.getScope());
					originalDependency.setOptional(dependency.isOptional() ? "true" : null);

					originalDependency.setSystemPath(dependency.getSystemPath());

					originalDependency.setExclusions(dependency.getExclusions());

					this.log.info("  Reset dependency: " + beforeDependencyStr + " -> " + dependencyToString(originalDependency));
				} else {
					// 基本上是重复引用导致，直接移除当前dependency
					this.log.info("  Remove duple dependency: " + dependencyToString(originalDependency));
					this.originalModel.getDependencies().remove(n--);
					originalSize--;
					i--;
				}
			} else if (!isNeedRemoved(dependency)) {
				Dependency originalDependency = this.copyDependency(dependency); // 复制一份出来再添加
				this.log.info("  Add dependency: " + dependencyToString(originalDependency));
				this.originalModel.getDependencies().add(originalDependency);
			}
		}
		this.log.info("Remaining " + this.originalModel.getDependencies().size() + " dependencies.");
		printLine();
	}

	/**
	 * 为避免修改了model里的依赖数据，影响maven的正常运行。所以复制一份出来。设置到originalModel中。
	 *
	 * @param dependency 需复制的依赖项
	 * @return 复制的依赖项
	 */
	protected Dependency copyDependency(Dependency dependency) {
		Dependency originalDependency = new Dependency();

		originalDependency.setGroupId(dependency.getGroupId());
		originalDependency.setArtifactId(dependency.getArtifactId());
		originalDependency.setVersion(dependency.getVersion());
		originalDependency.setType(dependency.getType());
		originalDependency.setClassifier(dependency.getClassifier());

		originalDependency.setScope("compile".equalsIgnoreCase(dependency.getScope()) ? null : dependency.getScope());
		originalDependency.setOptional(dependency.isOptional() ? "true" : null);

		originalDependency.setSystemPath(dependency.getSystemPath());

		originalDependency.setExclusions(new ArrayList<>(dependency.getExclusions()));

		return originalDependency;
	}

	public void optimizeDependencies() {
		if (isEmpty(this.originalModel.getDependencies())) {
			return;
		}
		this.printLine();
		this.log.info("Optimize Dependencies: (" + this.originalModel.getDependencies().size() + ")");
		this.optimizeDependencies(this.originalModel.getDependencies());
	}

	protected void optimizeDependencies(List<Dependency> dependencies) {
		if (isEmpty(dependencies)) {
			return;
		}

		Function<String, String> fun = this.getReplaceVariableFunction();

		for (Dependency dependency : dependencies) {
			String dependencyBefore = dependencyToString(dependency);

			dependency.setGroupId(fun.apply(dependency.getGroupId()));
			dependency.setArtifactId(fun.apply(dependency.getArtifactId()));
			dependency.setVersion(fun.apply(dependency.getVersion()));

			this.clearDependencyScopeCompileAndOptionalFalse(dependency);

			String dependencyAfter = dependencyToString(dependency);

			if (!dependencyBefore.equals(dependencyAfter)) {
				this.log.info("  optimize dependency: " + dependencyBefore + " -> " + dependencyAfter);
			}
		}
	}

	protected Function<String, String> getReplaceVariableFunction() {
		if (this.originalModel.getParent() != null || isEmpty(this.model.getProperties())) {
			this.log.info(" - Optimize with 'getProjectProperty'");
			return this::getProjectProperty;
		} else {
			this.log.info(" - Optimize with 'replaceVariable'");
			return this::replaceVariable;
		}
	}

	protected boolean isNeedRemoved(Dependency dependency) {
		if (!this.config.isKeepProvidedDependencies() && "provided".equalsIgnoreCase(dependency.getScope())) {
			return true;
		}

		if (!this.config.isKeepTestDependencies() && "test".equalsIgnoreCase(dependency.getScope())) {
			return true;
		}

		if (!this.config.isKeepOptionalDependencies() && dependency.isOptional()) {
			return true;
		}

		if (this.config.isExcludeDependency(dependency)) {
			return true;
		}

		return false;
	}

	protected void removeOneDependencies(Dependency dependency, int n, String cause) {
		this.originalModel.getDependencies().remove(n);
		this.log.info("  Remove dependency by " + cause + ": " + dependencyToString(dependency));
	}

	//endregion ##


	//region -------------------- Properties --------------------

	public void removeProperties() {
		if (isNotEmpty(this.originalModel.getProperties())) {
			this.log.info("Remove Properties.");
			this.originalModel.setProperties(null);
			this.resetDependencies();
		}
	}

	/**
	 * 该功能的应用场景：<br>
	 * 举例1：框架中，添加一个模块，simplifyMode=pom，但是希望设置parent为此模块的子模块中，采用simplifyMode=bom.
	 */
	public void createPropertiesByConfig() {
		try {
			if (this.originalModel.getProperties() == null) {
				return;
			}

			if (isNotEmpty(this.config.getCreateProperties())) {
				this.config.getCreateProperties().forEach((key, value) -> {
					if (isNotEmpty(key) && isNotEmpty(value)) {
						this.log.info("Create Properties: " + key + " = " + value);
						this.originalModel.getProperties().put(key, value);
					}
				});
			}
		} finally {
			if (this.originalModel.getBuild() != null && isNotEmpty(this.originalModel.getBuild().getPlugins())) {
				for (Plugin plugin : this.originalModel.getBuild().getPlugins()) {
					if ("icu.easyj.maven.plugins".equalsIgnoreCase(plugin.getGroupId())
							&& "easyj-maven-plugin".equalsIgnoreCase(plugin.getArtifactId())
							&& plugin.getConfiguration() instanceof Xpp3Dom) {
						this.removeConfiguration(plugin, "createProperties", "removeParent");
						break;
					}
				}
			}
		}
	}

	protected void removeConfiguration(Plugin plugin, String... removeConfigNames) {
		Set<String> removeConfigSet = new HashSet<>(Arrays.asList(removeConfigNames));
		removeConfigSet.remove(null);

		Xpp3Dom configDom = (Xpp3Dom)plugin.getConfiguration();
		for (int i = 0; i < configDom.getChildren().length; i++) {
			Xpp3Dom child = configDom.getChildren()[i];
			if (removeConfigSet.contains(child.getName())) {
				this.log.info("Remove one config '" + child.getName() + "' from the plugin '" + plugin.getId() + "'.");
				configDom.removeChild(i--);
			}
		}
		if (configDom.getChildren().length == 0) {
			this.log.info("Remove Configuration from the plugin '" + plugin.getId() + "'.");
			plugin.setConfiguration(null);
		}
	}

	//endregion


	//region -------------------- Prerequisites、Build、Reporting、Reports --------------------

	public void removePrerequisites() {
		if (this.originalModel.getPrerequisites() != null) {
			this.log.info("Remove Prerequisites.");
			this.originalModel.setPrerequisites(null);
		}
	}

	public void removeBuild() {
		if (this.originalModel.getBuild() != null) {
			this.log.info("Remove Build.");
			this.originalModel.setBuild(null);
		}
	}

	public void removeReporting() {
		if (this.originalModel.getReporting() != null) {
			this.log.info("Remove Reporting.");
			this.originalModel.setReporting(null);
		}
	}

	public void removeReports() {
		if (this.originalModel.getReports() != null) {
			this.originalModel.setReports(null);
		}
	}

	//endregion ##


	//region -------------------- Repositories、PluginRepositories、DistributionManagement --------------------

	public void removeRepositories() {
		if (isNotEmpty(this.originalModel.getRepositories())) {
			this.log.info("Remove Repositories.");
			this.originalModel.setRepositories(null);
		}
	}

	public void removePluginRepositories() {
		if (isNotEmpty(this.originalModel.getPluginRepositories())) {
			this.log.info("Remove PluginRepositories.");
			this.originalModel.setPluginRepositories(null);
		}
	}

	public void removeDistributionManagement() {
		if (this.originalModel.getDistributionManagement() != null) {
			this.log.info("Remove DistributionManagement.");
			this.originalModel.setDistributionManagement(null);
		}
	}

	//endregion ##


	//region -------------------- Profiles --------------------

	public void removeProfiles() {
		if (isNotEmpty(this.originalModel.getProfiles())) {
			this.log.info("Remove Profiles.");
			this.originalModel.setProfiles(null);
		}
	}

	//endregion ##


	//endregion #
}
