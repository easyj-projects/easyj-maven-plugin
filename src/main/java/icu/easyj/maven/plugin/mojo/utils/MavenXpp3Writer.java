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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.ActivationOS;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Extension;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.model.Site;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.MXSerializer;

/**
 * 为了更加简化POM文件，复制了 {@link org.apache.maven.model.io.xpp3.MavenXpp3Writer} 并修改部分代码.
 *
 * @author wangliang181230
 * @since 1.0.1
 */
public class MavenXpp3Writer {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	private static final String NAMESPACE = null;

	private static final String LINE_SEPARATOR = System.getProperty("os.name").contains("Windows") ? "\r\n" : "\n";


	private String fileComment = null;

	private boolean useTabIndent = false;


	//------------------/
	//- Public Methods -/
	//------------------/

	public void setFileComment(String fileComment) {
		this.fileComment = fileComment;
	}

	public void setUseTabIndent(boolean useTabIndent) {
		this.useTabIndent = useTabIndent;
	}

	public void write(Writer writer, Model model) throws IOException {
		MXSerializer serializer = new MXSerializer();
		serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", this.useTabIndent ? "\t" : "  ");
		serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", LINE_SEPARATOR);

		serializer.setOutput(writer);
		serializer.startDocument(model.getModelEncoding(), null);
		writeModel(model, serializer);
		serializer.endDocument();
	}


	//-------------------/
	//- Private Methods -/
	//-------------------/

	private void writeActivation(Activation activation, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "activation");
		if (activation.isActiveByDefault()) {
			this.write("activeByDefault", String.valueOf(activation.isActiveByDefault()), serializer);
		}
		if (activation.getJdk() != null) {
			this.write("jdk", activation.getJdk(), serializer);
		}
		if (activation.getOs() != null) {
			writeActivationOS(activation.getOs(), serializer);
		}
		if (activation.getProperty() != null) {
			writeActivationProperty(activation.getProperty(), serializer);
		}
		if (activation.getFile() != null) {
			writeActivationFile(activation.getFile(), serializer);
		}
		serializer.endTag(NAMESPACE, "activation");
	}

	private void writeActivationFile(ActivationFile activationFile, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "file");
		if (activationFile.getMissing() != null) {
			this.write("missing", activationFile.getMissing(), serializer);
		}
		if (activationFile.getExists() != null) {
			this.write("exists", activationFile.getExists(), serializer);
		}
		serializer.endTag(NAMESPACE, "file");
	}

	private void writeActivationOS(ActivationOS activationOS, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "os");
		if (activationOS.getName() != null) {
			this.write("name", activationOS.getName(), serializer);
		}
		if (activationOS.getFamily() != null) {
			this.write("family", activationOS.getFamily(), serializer);
		}
		if (activationOS.getArch() != null) {
			this.write("arch", activationOS.getArch(), serializer);
		}
		if (activationOS.getVersion() != null) {
			this.write("version", activationOS.getVersion(), serializer);
		}
		serializer.endTag(NAMESPACE, "os");
	}

	private void writeActivationProperty(ActivationProperty activationProperty, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "property");
		if (activationProperty.getName() != null) {
			this.write("name", activationProperty.getName(), serializer);
		}
		if (activationProperty.getValue() != null) {
			this.write("value", activationProperty.getValue(), serializer);
		}
		serializer.endTag(NAMESPACE, "property");
	}

	private void writeBuild(Build build, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "build");
		if (build.getSourceDirectory() != null) {
			this.write("sourceDirectory", build.getSourceDirectory(), serializer);
		}
		if (build.getScriptSourceDirectory() != null) {
			this.write("scriptSourceDirectory", build.getScriptSourceDirectory(), serializer);
		}
		if (build.getTestSourceDirectory() != null) {
			this.write("testSourceDirectory", build.getTestSourceDirectory(), serializer);
		}
		if (build.getOutputDirectory() != null) {
			this.write("outputDirectory", build.getOutputDirectory(), serializer);
		}
		if (build.getTestOutputDirectory() != null) {
			this.write("testOutputDirectory", build.getTestOutputDirectory(), serializer);
		}
		if ((build.getExtensions() != null) && (build.getExtensions().size() > 0)) {
			this.writeList("extensions", build.getExtensions(), this::writeExtension, serializer);
		}
		if (build.getDefaultGoal() != null) {
			this.write("defaultGoal", build.getDefaultGoal(), serializer);
		}
		if ((build.getResources() != null) && (build.getResources().size() > 0)) {
			this.writeList("resources", "resource", build.getResources(), this::writeResource, serializer);
		}
		if ((build.getTestResources() != null) && (build.getTestResources().size() > 0)) {
			this.writeList("testResources", "testResource", build.getTestResources(), this::writeResource, serializer);
		}
		if (build.getDirectory() != null) {
			this.write("directory", build.getDirectory(), serializer);
		}
		if (build.getFinalName() != null) {
			this.write("finalName", build.getFinalName(), serializer);
		}
		if ((build.getFilters() != null) && (build.getFilters().size() > 0)) {
			this.writeList("filters", "filter", build.getFilters(), serializer);
		}
		if (build.getPluginManagement() != null) {
			writePluginManagement(build.getPluginManagement(), serializer);
		}
		if ((build.getPlugins() != null) && (build.getPlugins().size() > 0)) {
			this.writeList("plugins", build.getPlugins(), this::writePlugin, serializer);
		}
		serializer.endTag(NAMESPACE, "build");
	}

	private void writeBuildBase(BuildBase buildBase, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "build");
		if (buildBase.getDefaultGoal() != null) {
			this.write("defaultGoal", buildBase.getDefaultGoal(), serializer);
		}
		if ((buildBase.getResources() != null) && (buildBase.getResources().size() > 0)) {
			this.writeList("resources", "resource", buildBase.getResources(), this::writeResource, serializer);
		}
		if ((buildBase.getTestResources() != null) && (buildBase.getTestResources().size() > 0)) {
			this.writeList("testResources", "testResource", buildBase.getTestResources(), this::writeResource, serializer);
		}
		if (buildBase.getDirectory() != null) {
			this.write("directory", buildBase.getDirectory(), serializer);
		}
		if (buildBase.getFinalName() != null) {
			this.write("finalName", buildBase.getFinalName(), serializer);
		}
		if ((buildBase.getFilters() != null) && (buildBase.getFilters().size() > 0)) {
			this.writeList("filters", "filter", buildBase.getFilters(), serializer);
		}
		if (buildBase.getPluginManagement() != null) {
			writePluginManagement(buildBase.getPluginManagement(), serializer);
		}
		if ((buildBase.getPlugins() != null) && (buildBase.getPlugins().size() > 0)) {
			this.writeList("plugins", buildBase.getPlugins(), this::writePlugin, serializer);
		}
		serializer.endTag(NAMESPACE, "build");
	}

	private void writeCiManagement(CiManagement ciManagement, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "ciManagement");
		if (ciManagement.getSystem() != null) {
			this.write("system", ciManagement.getSystem(), serializer);
		}
		if (ciManagement.getUrl() != null) {
			this.write("url", ciManagement.getUrl(), serializer);
		}
		if ((ciManagement.getNotifiers() != null) && (ciManagement.getNotifiers().size() > 0)) {
			this.writeList("notifiers", ciManagement.getNotifiers(), this::writeNotifier, serializer);
		}
		serializer.endTag(NAMESPACE, "ciManagement");
	}

	private void writeContributor(Contributor contributor, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "contributor");
		if (contributor.getName() != null) {
			this.write("name", contributor.getName(), serializer);
		}
		if (contributor.getEmail() != null) {
			this.write("email", contributor.getEmail(), serializer);
		}
		if (contributor.getUrl() != null) {
			this.write("url", contributor.getUrl(), serializer);
		}
		if (contributor.getOrganization() != null) {
			this.write("organization", contributor.getOrganization(), serializer);
		}
		if (contributor.getOrganizationUrl() != null) {
			this.write("organizationUrl", contributor.getOrganizationUrl(), serializer);
		}
		if ((contributor.getRoles() != null) && (contributor.getRoles().size() > 0)) {
			this.writeList("roles", "role", contributor.getRoles(), serializer);
		}
		if (contributor.getTimezone() != null) {
			this.write("timezone", contributor.getTimezone(), serializer);
		}
		if ((contributor.getProperties() != null) && (contributor.getProperties().size() > 0)) {
			this.writeMap("properties", contributor.getProperties(), serializer);
		}
		serializer.endTag(NAMESPACE, "contributor");
	}

	private void writeDependency(Dependency dependency, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "dependency");
		if (dependency.getGroupId() != null) {
			this.write("groupId", dependency.getGroupId(), serializer);
		}
		if (dependency.getArtifactId() != null) {
			this.write("artifactId", dependency.getArtifactId(), serializer);
		}
		if (dependency.getVersion() != null) {
			this.write("version", dependency.getVersion(), serializer);
		}
		if ((dependency.getType() != null) && !dependency.getType().equals("jar")) {
			this.write("type", dependency.getType(), serializer);
		}
		if (dependency.getClassifier() != null) {
			this.write("classifier", dependency.getClassifier(), serializer);
		}
		if (dependency.getScope() != null && !dependency.getScope().equals("compile")) {
			this.write("scope", dependency.getScope(), serializer);
		}
		if (dependency.getSystemPath() != null) {
			this.write("systemPath", dependency.getSystemPath(), serializer);
		}
		if ((dependency.getExclusions() != null) && (dependency.getExclusions().size() > 0)) {
			this.writeList("exclusions", dependency.getExclusions(), this::writeExclusion, serializer);
		}
		if (dependency.getOptional() != null) {
			this.write("optional", dependency.getOptional(), serializer);
		}
		serializer.endTag(NAMESPACE, "dependency");
	}

	private void writeDependencyManagement(DependencyManagement dependencyManagement, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "dependencyManagement");
		if ((dependencyManagement.getDependencies() != null) && (dependencyManagement.getDependencies().size() > 0)) {
			this.writeList("dependencies", dependencyManagement.getDependencies(), this::writeDependency, serializer);
		}
		serializer.endTag(NAMESPACE, "dependencyManagement");
	}

	private void writeDeploymentRepository(DeploymentRepository deploymentRepository, String tagName, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (!deploymentRepository.isUniqueVersion()) {
			this.write("uniqueVersion", String.valueOf(deploymentRepository.isUniqueVersion()), serializer);
		}
		if (deploymentRepository.getReleases() != null) {
			writeRepositoryPolicy(deploymentRepository.getReleases(), "releases", serializer);
		}
		if (deploymentRepository.getSnapshots() != null) {
			writeRepositoryPolicy(deploymentRepository.getSnapshots(), "snapshots", serializer);
		}
		if (deploymentRepository.getId() != null) {
			this.write("id", deploymentRepository.getId(), serializer);
		}
		if (deploymentRepository.getName() != null) {
			this.write("name", deploymentRepository.getName(), serializer);
		}
		if (deploymentRepository.getUrl() != null) {
			this.write("url", deploymentRepository.getUrl(), serializer);
		}
		if ((deploymentRepository.getLayout() != null) && !deploymentRepository.getLayout().equals("default")) {
			this.write("layout", deploymentRepository.getLayout(), serializer);
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private void writeDeveloper(Developer developer, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "developer");
		if (developer.getId() != null) {
			this.write("id", developer.getId(), serializer);
		}
		if (developer.getName() != null) {
			this.write("name", developer.getName(), serializer);
		}
		if (developer.getEmail() != null) {
			this.write("email", developer.getEmail(), serializer);
		}
		if (developer.getUrl() != null) {
			this.write("url", developer.getUrl(), serializer);
		}
		if (developer.getOrganization() != null) {
			this.write("organization", developer.getOrganization(), serializer);
		}
		if (developer.getOrganizationUrl() != null) {
			this.write("organizationUrl", developer.getOrganizationUrl(), serializer);
		}
		if ((developer.getRoles() != null) && (developer.getRoles().size() > 0)) {
			this.writeList("roles", "role", developer.getRoles(), serializer);
		}
		if (developer.getTimezone() != null) {
			this.write("timezone", developer.getTimezone(), serializer);
		}
		if ((developer.getProperties() != null) && (developer.getProperties().size() > 0)) {
			this.writeMap("properties", developer.getProperties(), serializer);
		}
		serializer.endTag(NAMESPACE, "developer");
	}

	private void writeDistributionManagement(DistributionManagement distributionManagement, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "distributionManagement");
		if (distributionManagement.getRepository() != null) {
			writeDeploymentRepository(distributionManagement.getRepository(), "repository", serializer);
		}
		if (distributionManagement.getSnapshotRepository() != null) {
			writeDeploymentRepository(distributionManagement.getSnapshotRepository(), "snapshotRepository", serializer);
		}
		if (distributionManagement.getSite() != null) {
			writeSite(distributionManagement.getSite(), serializer);
		}
		if (distributionManagement.getDownloadUrl() != null) {
			this.write("downloadUrl", distributionManagement.getDownloadUrl(), serializer);
		}
		if (distributionManagement.getRelocation() != null) {
			writeRelocation(distributionManagement.getRelocation(), serializer);
		}
		if (distributionManagement.getStatus() != null) {
			this.write("status", distributionManagement.getStatus(), serializer);
		}
		serializer.endTag(NAMESPACE, "distributionManagement");
	}

	private void writeExclusion(Exclusion exclusion, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "exclusion");
		if (exclusion.getGroupId() != null) {
			this.write("groupId", exclusion.getGroupId(), serializer);
		}
		if (exclusion.getArtifactId() != null) {
			this.write("artifactId", exclusion.getArtifactId(), serializer);
		}
		serializer.endTag(NAMESPACE, "exclusion");
	}

	private void writeExtension(Extension extension, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "extension");
		if (extension.getGroupId() != null) {
			this.write("groupId", extension.getGroupId(), serializer);
		}
		if (extension.getArtifactId() != null) {
			this.write("artifactId", extension.getArtifactId(), serializer);
		}
		if (extension.getVersion() != null) {
			this.write("version", extension.getVersion(), serializer);
		}
		serializer.endTag(NAMESPACE, "extension");
	}

	private void writeIssueManagement(IssueManagement issueManagement, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "issueManagement");
		if (issueManagement.getSystem() != null) {
			this.write("system", issueManagement.getSystem(), serializer);
		}
		if (issueManagement.getUrl() != null) {
			this.write("url", issueManagement.getUrl(), serializer);
		}
		serializer.endTag(NAMESPACE, "issueManagement");
	}

	private void writeLicense(License license, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "license");
		if (license.getName() != null) {
			this.write("name", license.getName(), serializer);
		}
		if (license.getUrl() != null) {
			this.write("url", license.getUrl(), serializer);
		}
		if (license.getDistribution() != null) {
			this.write("distribution", license.getDistribution(), serializer);
		}
		if (license.getComments() != null) {
			this.write("comments", license.getComments(), serializer);
		}
		serializer.endTag(NAMESPACE, "license");
	}

	private void writeMailingList(MailingList mailingList, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "mailingList");
		if (mailingList.getName() != null) {
			this.write("name", mailingList.getName(), serializer);
		}
		if (mailingList.getSubscribe() != null) {
			this.write("subscribe", mailingList.getSubscribe(), serializer);
		}
		if (mailingList.getUnsubscribe() != null) {
			this.write("unsubscribe", mailingList.getUnsubscribe(), serializer);
		}
		if (mailingList.getPost() != null) {
			this.write("post", mailingList.getPost(), serializer);
		}
		if (mailingList.getArchive() != null) {
			this.write("archive", mailingList.getArchive(), serializer);
		}
		if ((mailingList.getOtherArchives() != null) && (mailingList.getOtherArchives().size() > 0)) {
			this.writeList("otherArchives", "otherArchive", mailingList.getOtherArchives(), serializer);
		}
		serializer.endTag(NAMESPACE, "mailingList");
	}

	private void writeModel(Model model, MXSerializer serializer) throws IOException {
		if (this.fileComment != null) {
			serializer.text(LINE_SEPARATOR);
			serializer.comment(this.fileComment);
		}
		serializer.text(LINE_SEPARATOR);

		//serializer.setPrefix("", "http://maven.apache.org/POM/4.0.0");
		//serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		serializer.startTag(NAMESPACE, "project");
		//serializer.attribute("", "xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd");
		serializer.getWriter().write(" xmlns=\"http://maven.apache.org/POM/4.0.0\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");

		if (model.getChildProjectUrlInheritAppendPath() != null) {
			serializer.attribute(NAMESPACE, "child.project.url.inherit.append.path", model.getChildProjectUrlInheritAppendPath());
		}
		if (model.getModelVersion() != null) {
			this.write("modelVersion", model.getModelVersion(), serializer);
		}
		if (model.getParent() != null) {
			writeParent(model.getParent(), serializer);
		}
		if (model.getGroupId() != null) {
			this.write("groupId", model.getGroupId(), serializer);
		}
		if (model.getArtifactId() != null) {
			this.write("artifactId", model.getArtifactId(), serializer);
		}
		if (model.getVersion() != null) {
			this.write("version", model.getVersion(), serializer);
		}
		if ((model.getPackaging() != null) && !model.getPackaging().equals("jar")) {
			this.write("packaging", model.getPackaging(), serializer);
		}
		if (model.getName() != null) {
			this.write("name", model.getName(), serializer);
		}
		if (model.getDescription() != null) {
			this.write("description", model.getDescription(), serializer);
		}
		if (model.getUrl() != null) {
			this.write("url", model.getUrl(), serializer);
		}
		if (model.getInceptionYear() != null) {
			this.write("inceptionYear", model.getInceptionYear(), serializer);
		}
		if (model.getOrganization() != null) {
			writeOrganization(model.getOrganization(), serializer);
		}
		if ((model.getLicenses() != null) && (model.getLicenses().size() > 0)) {
			this.writeList("licenses", model.getLicenses(), this::writeLicense, serializer);
		}
		if ((model.getDevelopers() != null) && (model.getDevelopers().size() > 0)) {
			this.writeList("developers", model.getDevelopers(), this::writeDeveloper, serializer);
		}
		if ((model.getContributors() != null) && (model.getContributors().size() > 0)) {
			this.writeList("contributors", model.getContributors(), this::writeContributor, serializer);
		}
		if ((model.getMailingLists() != null) && (model.getMailingLists().size() > 0)) {
			this.writeList("mailingLists", model.getMailingLists(), this::writeMailingList, serializer);
		}
		if (model.getPrerequisites() != null) {
			writePrerequisites(model.getPrerequisites(), serializer);
		}
		if ((model.getModules() != null) && (model.getModules().size() > 0)) {
			this.writeList("modules", "module", model.getModules(), serializer);
		}
		if (model.getScm() != null) {
			writeScm(model.getScm(), serializer);
		}
		if (model.getIssueManagement() != null) {
			writeIssueManagement(model.getIssueManagement(), serializer);
		}
		if (model.getCiManagement() != null) {
			writeCiManagement(model.getCiManagement(), serializer);
		}
		if (model.getDistributionManagement() != null) {
			writeDistributionManagement(model.getDistributionManagement(), serializer);
		}
		if ((model.getProperties() != null) && (model.getProperties().size() > 0)) {
			this.writeMap("properties", model.getProperties(), serializer);
		}
		if (model.getDependencyManagement() != null) {
			writeDependencyManagement(model.getDependencyManagement(), serializer);
		}
		if ((model.getDependencies() != null) && (model.getDependencies().size() > 0)) {
			this.writeList("dependencies", model.getDependencies(), this::writeDependency, serializer);
		}
		if ((model.getRepositories() != null) && (model.getRepositories().size() > 0)) {
			this.writeList("repositories", "repository", model.getRepositories(), this::writeRepository, serializer);
		}
		if ((model.getPluginRepositories() != null) && (model.getPluginRepositories().size() > 0)) {
			this.writeList("pluginRepositories", "pluginRepository", model.getPluginRepositories(), this::writeRepository, serializer);
		}
		if (model.getBuild() != null) {
			writeBuild(model.getBuild(), serializer);
		}
		if (model.getReports() != null) {
			((Xpp3Dom)model.getReports()).writeToSerializer(NAMESPACE, serializer);
		}
		if (model.getReporting() != null) {
			writeReporting(model.getReporting(), serializer);
		}
		if ((model.getProfiles() != null) && (model.getProfiles().size() > 0)) {
			this.writeList("profiles", model.getProfiles(), this::writeProfile, serializer);
		}
		serializer.endTag(NAMESPACE, "project");
	}

	private void writeNotifier(Notifier notifier, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "notifier");
		if ((notifier.getType() != null) && !notifier.getType().equals("mail")) {
			this.write("type", notifier.getType(), serializer);
		}
		if (!notifier.isSendOnError()) {
			this.write("sendOnError", String.valueOf(notifier.isSendOnError()), serializer);
		}
		if (!notifier.isSendOnFailure()) {
			this.write("sendOnFailure", String.valueOf(notifier.isSendOnFailure()), serializer);
		}
		if (!notifier.isSendOnSuccess()) {
			this.write("sendOnSuccess", String.valueOf(notifier.isSendOnSuccess()), serializer);
		}
		if (!notifier.isSendOnWarning()) {
			this.write("sendOnWarning", String.valueOf(notifier.isSendOnWarning()), serializer);
		}
		if (notifier.getAddress() != null) {
			this.write("address", notifier.getAddress(), serializer);
		}
		if ((notifier.getConfiguration() != null) && (notifier.getConfiguration().size() > 0)) {
			this.writeMap("configuration", notifier.getConfiguration(), serializer);
		}
		serializer.endTag(NAMESPACE, "notifier");
	}

	private void writeOrganization(Organization organization, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "organization");
		if (organization.getName() != null) {
			this.write("name", organization.getName(), serializer);
		}
		if (organization.getUrl() != null) {
			this.write("url", organization.getUrl(), serializer);
		}
		serializer.endTag(NAMESPACE, "organization");
	}

	private void writeParent(Parent parent, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "parent");
		if (parent.getGroupId() != null) {
			this.write("groupId", parent.getGroupId(), serializer);
		}
		if (parent.getArtifactId() != null) {
			this.write("artifactId", parent.getArtifactId(), serializer);
		}
		if (parent.getVersion() != null) {
			this.write("version", parent.getVersion(), serializer);
		}
		if (parent.getRelativePath() != null && parent.getRelativePath().length() > 0 && !parent.getRelativePath().equals("../pom.xml")) {
			this.write("relativePath", parent.getRelativePath(), serializer);
		}
		serializer.endTag(NAMESPACE, "parent");
	}

	private void writePlugin(Plugin plugin, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "plugin");
		if ((plugin.getGroupId() != null) && !plugin.getGroupId().equals("org.apache.maven.plugins")) {
			this.write("groupId", plugin.getGroupId(), serializer);
		}
		if (plugin.getArtifactId() != null) {
			this.write("artifactId", plugin.getArtifactId(), serializer);
		}
		if (plugin.getVersion() != null) {
			this.write("version", plugin.getVersion(), serializer);
		}
		if (plugin.getExtensions() != null) {
			this.write("extensions", plugin.getExtensions(), serializer);
		}
		if ((plugin.getExecutions() != null) && (plugin.getExecutions().size() > 0)) {
			this.writeList("executions", plugin.getExecutions(), this::writePluginExecution, serializer);
		}
		if ((plugin.getDependencies() != null) && (plugin.getDependencies().size() > 0)) {
			this.writeList("dependencies", plugin.getDependencies(), this::writeDependency, serializer);
		}
		if (plugin.getGoals() != null) {
			((Xpp3Dom)plugin.getGoals()).writeToSerializer(NAMESPACE, serializer);
		}
		if (!plugin.isInherited()) {
			this.write("inherited", plugin.getInherited(), serializer);
		}
		if (plugin.getConfiguration() != null) {
			((Xpp3Dom)plugin.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, "plugin");
	}

	private void writePluginExecution(PluginExecution pluginExecution, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "execution");
		if ((pluginExecution.getId() != null) && !pluginExecution.getId().equals("default")) {
			this.write("id", pluginExecution.getId(), serializer);
		}
		if (pluginExecution.getPhase() != null) {
			this.write("phase", pluginExecution.getPhase(), serializer);
		}
		if ((pluginExecution.getGoals() != null) && (pluginExecution.getGoals().size() > 0)) {
			this.writeList("goals", "goal", pluginExecution.getGoals(), serializer);
		}
		if (!pluginExecution.isInherited()) {
			this.write("inherited", pluginExecution.getInherited(), serializer);
		}
		if (pluginExecution.getConfiguration() != null) {
			((Xpp3Dom)pluginExecution.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, "execution");
	}

	private void writePluginManagement(PluginManagement pluginManagement, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "pluginManagement");
		if ((pluginManagement.getPlugins() != null) && (pluginManagement.getPlugins().size() > 0)) {
			this.writeList("plugins", pluginManagement.getPlugins(), this::writePlugin, serializer);
		}
		serializer.endTag(NAMESPACE, "pluginManagement");
	}

	private void writePrerequisites(Prerequisites prerequisites, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "prerequisites");
		if ((prerequisites.getMaven() != null) && !prerequisites.getMaven().equals("2.0")) {
			this.write("maven", prerequisites.getMaven(), serializer);
		}
		serializer.endTag(NAMESPACE, "prerequisites");
	}

	private void writeProfile(Profile profile, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "profile");
		if ((profile.getId() != null) && !profile.getId().equals("default")) {
			this.write("id", profile.getId(), serializer);
		}
		if (profile.getActivation() != null) {
			writeActivation(profile.getActivation(), serializer);
		}
		if (profile.getBuild() != null) {
			writeBuildBase(profile.getBuild(), serializer);
		}
		if ((profile.getModules() != null) && (profile.getModules().size() > 0)) {
			this.writeList("modules", "module", profile.getModules(), serializer);
		}
		if (profile.getDistributionManagement() != null) {
			writeDistributionManagement(profile.getDistributionManagement(), serializer);
		}
		if ((profile.getProperties() != null) && (profile.getProperties().size() > 0)) {
			this.writeMap("properties", profile.getProperties(), serializer);
		}
		if (profile.getDependencyManagement() != null) {
			writeDependencyManagement(profile.getDependencyManagement(), serializer);
		}
		if ((profile.getDependencies() != null) && (profile.getDependencies().size() > 0)) {
			this.writeList("dependencies", profile.getDependencies(), this::writeDependency, serializer);
		}
		if ((profile.getRepositories() != null) && (profile.getRepositories().size() > 0)) {
			this.writeList("repositories", "repository", profile.getRepositories(), this::writeRepository, serializer);
		}
		if ((profile.getPluginRepositories() != null) && (profile.getPluginRepositories().size() > 0)) {
			this.writeList("pluginRepositories", "pluginRepository", profile.getPluginRepositories(), this::writeRepository, serializer);
		}
		if (profile.getReports() != null) {
			((Xpp3Dom)profile.getReports()).writeToSerializer(NAMESPACE, serializer);
		}
		if (profile.getReporting() != null) {
			writeReporting(profile.getReporting(), serializer);
		}
		serializer.endTag(NAMESPACE, "profile");
	}

	private void writeRelocation(Relocation relocation, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "relocation");
		if (relocation.getGroupId() != null) {
			this.write("groupId", relocation.getGroupId(), serializer);
		}
		if (relocation.getArtifactId() != null) {
			this.write("artifactId", relocation.getArtifactId(), serializer);
		}
		if (relocation.getVersion() != null) {
			this.write("version", relocation.getVersion(), serializer);
		}
		if (relocation.getMessage() != null) {
			this.write("message", relocation.getMessage(), serializer);
		}
		serializer.endTag(NAMESPACE, "relocation");
	}

	private void writeReportPlugin(ReportPlugin reportPlugin, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "plugin");
		if ((reportPlugin.getGroupId() != null) && !reportPlugin.getGroupId().equals("org.apache.maven.plugins")) {
			this.write("groupId", reportPlugin.getGroupId(), serializer);
		}
		if (reportPlugin.getArtifactId() != null) {
			this.write("artifactId", reportPlugin.getArtifactId(), serializer);
		}
		if (reportPlugin.getVersion() != null) {
			this.write("version", reportPlugin.getVersion(), serializer);
		}
		if ((reportPlugin.getReportSets() != null) && (reportPlugin.getReportSets().size() > 0)) {
			this.writeList("reportSets", reportPlugin.getReportSets(), this::writeReportSet, serializer);
		}
		if (!reportPlugin.isInherited()) {
			this.write("inherited", reportPlugin.getInherited(), serializer);
		}
		if (reportPlugin.getConfiguration() != null) {
			((Xpp3Dom)reportPlugin.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, "plugin");
	}

	private void writeReportSet(ReportSet reportSet, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "reportSet");
		if ((reportSet.getId() != null) && !reportSet.getId().equals("default")) {
			this.write("id", reportSet.getId(), serializer);
		}
		if ((reportSet.getReports() != null) && (reportSet.getReports().size() > 0)) {
			this.writeList("reports", "report", reportSet.getReports(), serializer);
		}
		if (!reportSet.isInherited()) {
			this.write("inherited", reportSet.getInherited(), serializer);
		}
		if (reportSet.getConfiguration() != null) {
			((Xpp3Dom)reportSet.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, "reportSet");
	}

	private void writeReporting(Reporting reporting, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "reporting");
		if (reporting.getExcludeDefaults() != null) {
			this.write("excludeDefaults", reporting.getExcludeDefaults(), serializer);
		}
		if (reporting.getOutputDirectory() != null) {
			this.write("outputDirectory", reporting.getOutputDirectory(), serializer);
		}
		if ((reporting.getPlugins() != null) && (reporting.getPlugins().size() > 0)) {
			this.writeList("plugins", reporting.getPlugins(), this::writeReportPlugin, serializer);
		}
		serializer.endTag(NAMESPACE, "reporting");
	}

	private void writeRepository(Repository repository, String tagName, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (repository.getReleases() != null) {
			writeRepositoryPolicy(repository.getReleases(), "releases", serializer);
		}
		if (repository.getSnapshots() != null) {
			writeRepositoryPolicy(repository.getSnapshots(), "snapshots", serializer);
		}
		if (repository.getId() != null) {
			this.write("id", repository.getId(), serializer);
		}
		if (repository.getName() != null) {
			this.write("name", repository.getName(), serializer);
		}
		if (repository.getUrl() != null) {
			this.write("url", repository.getUrl(), serializer);
		}
		if ((repository.getLayout() != null) && !repository.getLayout().equals("default")) {
			this.write("layout", repository.getLayout(), serializer);
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private void writeRepositoryPolicy(RepositoryPolicy repositoryPolicy, String tagName, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (repositoryPolicy.getEnabled() != null) {
			this.write("enabled", repositoryPolicy.getEnabled(), serializer);
		}
		if (repositoryPolicy.getUpdatePolicy() != null) {
			this.write("updatePolicy", repositoryPolicy.getUpdatePolicy(), serializer);
		}
		if (repositoryPolicy.getChecksumPolicy() != null) {
			this.write("checksumPolicy", repositoryPolicy.getChecksumPolicy(), serializer);
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private void writeResource(Resource resource, String tagName, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (resource.getTargetPath() != null) {
			this.write("targetPath", resource.getTargetPath(), serializer);
		}
		if (resource.getFiltering() != null) {
			this.write("filtering", resource.getFiltering(), serializer);
		}
		if (resource.getDirectory() != null) {
			this.write("directory", resource.getDirectory(), serializer);
		}
		if ((resource.getIncludes() != null) && (resource.getIncludes().size() > 0)) {
			this.writeList("includes", "include", resource.getIncludes(), serializer);
		}
		if ((resource.getExcludes() != null) && (resource.getExcludes().size() > 0)) {
			this.writeList("excludes", "exclude", resource.getExcludes(), serializer);
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private void writeScm(Scm scm, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "scm");
		if (scm.getChildScmConnectionInheritAppendPath() != null) {
			serializer.attribute(NAMESPACE, "child.scm.connection.inherit.append.path", scm.getChildScmConnectionInheritAppendPath());
		}
		if (scm.getChildScmDeveloperConnectionInheritAppendPath() != null) {
			serializer.attribute(NAMESPACE, "child.scm.developerConnection.inherit.append.path", scm.getChildScmDeveloperConnectionInheritAppendPath());
		}
		if (scm.getChildScmUrlInheritAppendPath() != null) {
			serializer.attribute(NAMESPACE, "child.scm.url.inherit.append.path", scm.getChildScmUrlInheritAppendPath());
		}
		if (scm.getConnection() != null) {
			this.write("connection", scm.getConnection(), serializer);
		}
		if (scm.getDeveloperConnection() != null) {
			this.write("developerConnection", scm.getDeveloperConnection(), serializer);
		}
		if ((scm.getTag() != null) && !scm.getTag().equals("HEAD")) {
			this.write("tag", scm.getTag(), serializer);
		}
		if (scm.getUrl() != null) {
			this.write("url", scm.getUrl(), serializer);
		}
		serializer.endTag(NAMESPACE, "scm");
	}

	private void writeSite(Site site, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, "site");
		if (site.getChildSiteUrlInheritAppendPath() != null) {
			serializer.attribute(NAMESPACE, "child.site.url.inherit.append.path", site.getChildSiteUrlInheritAppendPath());
		}
		if (site.getId() != null) {
			this.write("id", site.getId(), serializer);
		}
		if (site.getName() != null) {
			this.write("name", site.getName(), serializer);
		}
		if (site.getUrl() != null) {
			this.write("url", site.getUrl(), serializer);
		}
		serializer.endTag(NAMESPACE, "site");
	}

	private void write(Map.Entry<Object, Object> entry, MXSerializer serializer) throws IOException {
		this.write(entry.getKey(), entry.getValue(), serializer);
	}

	private void write(Object tagNameObj, Object valueObj, MXSerializer serializer) throws IOException {
		String tagName = (String)tagNameObj;
		String value = (String)valueObj;
		this.write(tagName, value, serializer);
	}

	private void write(String tagName, String value, MXSerializer serializer) throws IOException {
		if (value != null && value.length() > 0) {
			serializer.startTag(NAMESPACE, tagName).text(value).endTag(NAMESPACE, tagName);
		} else {
			serializer.startTag(NAMESPACE, tagName).endTag(NAMESPACE, tagName);
		}
	}

	private void writeList(String parentTagName, String tagName, List<String> list, MXSerializer serializer) throws IOException {
		this.writeList(parentTagName, list, (v, s) -> this.write(tagName, v, serializer), serializer);
	}

	private <T> void writeList(String tagName, List<T> list, Consumer<T> consumer, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, tagName);
		for (T item : list) {
			consumer.accept(item, serializer);
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private <T> void writeList(String parentTagName, String tagName, List<T> list, Consumer2<T> consumer, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, parentTagName);
		for (T item : list) {
			consumer.accept(item, tagName, serializer);
		}
		serializer.endTag(NAMESPACE, parentTagName);
	}

	private void writeMap(Map<Object, Object> map, MXSerializer serializer) throws IOException {
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			this.write(entry, serializer);
		}
	}

	private void writeMap(String tagName, Map<Object, Object> map, MXSerializer serializer) throws IOException {
		serializer.startTag(NAMESPACE, tagName);
		this.writeMap(map, serializer);
		serializer.endTag(NAMESPACE, tagName);
	}


	@FunctionalInterface
	private interface Consumer<T> {
		void accept(T t, MXSerializer serializer) throws IOException;
	}


	@FunctionalInterface
	private interface Consumer2<T> {
		void accept(T t, String tagName, MXSerializer serializer) throws IOException;
	}
}
