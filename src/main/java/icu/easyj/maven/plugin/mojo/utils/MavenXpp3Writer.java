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

import java.io.Writer;

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

	public void write(Writer writer, Model model)
			throws java.io.IOException {
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

	private void writeActivation(Activation activation, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "activation");
		if (activation.isActiveByDefault()) {
			serializer.startTag(NAMESPACE, "activeByDefault").text(String.valueOf(activation.isActiveByDefault())).endTag(NAMESPACE, "activeByDefault");
		}
		if (activation.getJdk() != null) {
			serializer.startTag(NAMESPACE, "jdk").text(activation.getJdk()).endTag(NAMESPACE, "jdk");
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

	private void writeActivationFile(ActivationFile activationFile, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "file");
		if (activationFile.getMissing() != null) {
			serializer.startTag(NAMESPACE, "missing").text(activationFile.getMissing()).endTag(NAMESPACE, "missing");
		}
		if (activationFile.getExists() != null) {
			serializer.startTag(NAMESPACE, "exists").text(activationFile.getExists()).endTag(NAMESPACE, "exists");
		}
		serializer.endTag(NAMESPACE, "file");
	}

	private void writeActivationOS(ActivationOS activationOS, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "os");
		if (activationOS.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(activationOS.getName()).endTag(NAMESPACE, "name");
		}
		if (activationOS.getFamily() != null) {
			serializer.startTag(NAMESPACE, "family").text(activationOS.getFamily()).endTag(NAMESPACE, "family");
		}
		if (activationOS.getArch() != null) {
			serializer.startTag(NAMESPACE, "arch").text(activationOS.getArch()).endTag(NAMESPACE, "arch");
		}
		if (activationOS.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(activationOS.getVersion()).endTag(NAMESPACE, "version");
		}
		serializer.endTag(NAMESPACE, "os");
	}

	private void writeActivationProperty(ActivationProperty activationProperty, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "property");
		if (activationProperty.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(activationProperty.getName()).endTag(NAMESPACE, "name");
		}
		if (activationProperty.getValue() != null) {
			serializer.startTag(NAMESPACE, "value").text(activationProperty.getValue()).endTag(NAMESPACE, "value");
		}
		serializer.endTag(NAMESPACE, "property");
	}

	private void writeBuild(Build build, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "build");
		if (build.getSourceDirectory() != null) {
			serializer.startTag(NAMESPACE, "sourceDirectory").text(build.getSourceDirectory()).endTag(NAMESPACE, "sourceDirectory");
		}
		if (build.getScriptSourceDirectory() != null) {
			serializer.startTag(NAMESPACE, "scriptSourceDirectory").text(build.getScriptSourceDirectory()).endTag(NAMESPACE, "scriptSourceDirectory");
		}
		if (build.getTestSourceDirectory() != null) {
			serializer.startTag(NAMESPACE, "testSourceDirectory").text(build.getTestSourceDirectory()).endTag(NAMESPACE, "testSourceDirectory");
		}
		if (build.getOutputDirectory() != null) {
			serializer.startTag(NAMESPACE, "outputDirectory").text(build.getOutputDirectory()).endTag(NAMESPACE, "outputDirectory");
		}
		if (build.getTestOutputDirectory() != null) {
			serializer.startTag(NAMESPACE, "testOutputDirectory").text(build.getTestOutputDirectory()).endTag(NAMESPACE, "testOutputDirectory");
		}
		if ((build.getExtensions() != null) && (build.getExtensions().size() > 0)) {
			serializer.startTag(NAMESPACE, "extensions");
			for (Extension o : build.getExtensions()) {
				writeExtension(o, serializer);
			}
			serializer.endTag(NAMESPACE, "extensions");
		}
		if (build.getDefaultGoal() != null) {
			serializer.startTag(NAMESPACE, "defaultGoal").text(build.getDefaultGoal()).endTag(NAMESPACE, "defaultGoal");
		}
		if ((build.getResources() != null) && (build.getResources().size() > 0)) {
			serializer.startTag(NAMESPACE, "resources");
			for (Resource o : build.getResources()) {
				writeResource(o, "resource", serializer);
			}
			serializer.endTag(NAMESPACE, "resources");
		}
		if ((build.getTestResources() != null) && (build.getTestResources().size() > 0)) {
			serializer.startTag(NAMESPACE, "testResources");
			for (Resource o : build.getTestResources()) {
				writeResource(o, "testResource", serializer);
			}
			serializer.endTag(NAMESPACE, "testResources");
		}
		if (build.getDirectory() != null) {
			serializer.startTag(NAMESPACE, "directory").text(build.getDirectory()).endTag(NAMESPACE, "directory");
		}
		if (build.getFinalName() != null) {
			serializer.startTag(NAMESPACE, "finalName").text(build.getFinalName()).endTag(NAMESPACE, "finalName");
		}
		if ((build.getFilters() != null) && (build.getFilters().size() > 0)) {
			serializer.startTag(NAMESPACE, "filters");
			for (String filter : build.getFilters()) {
				serializer.startTag(NAMESPACE, "filter").text(filter).endTag(NAMESPACE, "filter");
			}
			serializer.endTag(NAMESPACE, "filters");
		}
		if (build.getPluginManagement() != null) {
			writePluginManagement(build.getPluginManagement(), serializer);
		}
		if ((build.getPlugins() != null) && (build.getPlugins().size() > 0)) {
			serializer.startTag(NAMESPACE, "plugins");
			for (Plugin o : build.getPlugins()) {
				writePlugin(o, serializer);
			}
			serializer.endTag(NAMESPACE, "plugins");
		}
		serializer.endTag(NAMESPACE, "build");
	}

	private void writeBuildBase(BuildBase buildBase, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "build");
		if (buildBase.getDefaultGoal() != null) {
			serializer.startTag(NAMESPACE, "defaultGoal").text(buildBase.getDefaultGoal()).endTag(NAMESPACE, "defaultGoal");
		}
		if ((buildBase.getResources() != null) && (buildBase.getResources().size() > 0)) {
			serializer.startTag(NAMESPACE, "resources");
			for (Resource o : buildBase.getResources()) {
				writeResource(o, "resource", serializer);
			}
			serializer.endTag(NAMESPACE, "resources");
		}
		if ((buildBase.getTestResources() != null) && (buildBase.getTestResources().size() > 0)) {
			serializer.startTag(NAMESPACE, "testResources");
			for (Resource o : buildBase.getTestResources()) {
				writeResource(o, "testResource", serializer);
			}
			serializer.endTag(NAMESPACE, "testResources");
		}
		if (buildBase.getDirectory() != null) {
			serializer.startTag(NAMESPACE, "directory").text(buildBase.getDirectory()).endTag(NAMESPACE, "directory");
		}
		if (buildBase.getFinalName() != null) {
			serializer.startTag(NAMESPACE, "finalName").text(buildBase.getFinalName()).endTag(NAMESPACE, "finalName");
		}
		if ((buildBase.getFilters() != null) && (buildBase.getFilters().size() > 0)) {
			serializer.startTag(NAMESPACE, "filters");
			for (String filter : buildBase.getFilters()) {
				serializer.startTag(NAMESPACE, "filter").text(filter).endTag(NAMESPACE, "filter");
			}
			serializer.endTag(NAMESPACE, "filters");
		}
		if (buildBase.getPluginManagement() != null) {
			writePluginManagement(buildBase.getPluginManagement(), serializer);
		}
		if ((buildBase.getPlugins() != null) && (buildBase.getPlugins().size() > 0)) {
			serializer.startTag(NAMESPACE, "plugins");
			for (Plugin o : buildBase.getPlugins()) {
				writePlugin(o, serializer);
			}
			serializer.endTag(NAMESPACE, "plugins");
		}
		serializer.endTag(NAMESPACE, "build");
	}

	private void writeCiManagement(CiManagement ciManagement, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "ciManagement");
		if (ciManagement.getSystem() != null) {
			serializer.startTag(NAMESPACE, "system").text(ciManagement.getSystem()).endTag(NAMESPACE, "system");
		}
		if (ciManagement.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(ciManagement.getUrl()).endTag(NAMESPACE, "url");
		}
		if ((ciManagement.getNotifiers() != null) && (ciManagement.getNotifiers().size() > 0)) {
			serializer.startTag(NAMESPACE, "notifiers");
			for (Notifier o : ciManagement.getNotifiers()) {
				writeNotifier(o, serializer);
			}
			serializer.endTag(NAMESPACE, "notifiers");
		}
		serializer.endTag(NAMESPACE, "ciManagement");
	}

	private void writeContributor(Contributor contributor, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "contributor");
		if (contributor.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(contributor.getName()).endTag(NAMESPACE, "name");
		}
		if (contributor.getEmail() != null) {
			serializer.startTag(NAMESPACE, "email").text(contributor.getEmail()).endTag(NAMESPACE, "email");
		}
		if (contributor.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(contributor.getUrl()).endTag(NAMESPACE, "url");
		}
		if (contributor.getOrganization() != null) {
			serializer.startTag(NAMESPACE, "organization").text(contributor.getOrganization()).endTag(NAMESPACE, "organization");
		}
		if (contributor.getOrganizationUrl() != null) {
			serializer.startTag(NAMESPACE, "organizationUrl").text(contributor.getOrganizationUrl()).endTag(NAMESPACE, "organizationUrl");
		}
		if ((contributor.getRoles() != null) && (contributor.getRoles().size() > 0)) {
			serializer.startTag(NAMESPACE, "roles");
			for (String role : contributor.getRoles()) {
				serializer.startTag(NAMESPACE, "role").text(role).endTag(NAMESPACE, "role");
			}
			serializer.endTag(NAMESPACE, "roles");
		}
		if (contributor.getTimezone() != null) {
			serializer.startTag(NAMESPACE, "timezone").text(contributor.getTimezone()).endTag(NAMESPACE, "timezone");
		}
		if ((contributor.getProperties() != null) && (contributor.getProperties().size() > 0)) {
			serializer.startTag(NAMESPACE, "properties");
			for (Object o : contributor.getProperties().keySet()) {
				String key = (String)o;
				String value = (String)contributor.getProperties().get(key);
				serializer.startTag(NAMESPACE, key).text(value).endTag(NAMESPACE, key);
			}
			serializer.endTag(NAMESPACE, "properties");
		}
		serializer.endTag(NAMESPACE, "contributor");
	}

	private void writeDependency(Dependency dependency, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "dependency");
		if (dependency.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(dependency.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (dependency.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(dependency.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		if (dependency.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(dependency.getVersion()).endTag(NAMESPACE, "version");
		}
		if ((dependency.getType() != null) && !dependency.getType().equals("jar")) {
			serializer.startTag(NAMESPACE, "type").text(dependency.getType()).endTag(NAMESPACE, "type");
		}
		if (dependency.getClassifier() != null) {
			serializer.startTag(NAMESPACE, "classifier").text(dependency.getClassifier()).endTag(NAMESPACE, "classifier");
		}
		if (dependency.getScope() != null) {
			serializer.startTag(NAMESPACE, "scope").text(dependency.getScope()).endTag(NAMESPACE, "scope");
		}
		if (dependency.getSystemPath() != null) {
			serializer.startTag(NAMESPACE, "systemPath").text(dependency.getSystemPath()).endTag(NAMESPACE, "systemPath");
		}
		if ((dependency.getExclusions() != null) && (dependency.getExclusions().size() > 0)) {
			serializer.startTag(NAMESPACE, "exclusions");
			for (Exclusion o : dependency.getExclusions()) {
				writeExclusion(o, serializer);
			}
			serializer.endTag(NAMESPACE, "exclusions");
		}
		if (dependency.getOptional() != null) {
			serializer.startTag(NAMESPACE, "optional").text(dependency.getOptional()).endTag(NAMESPACE, "optional");
		}
		serializer.endTag(NAMESPACE, "dependency");
	}

	private void writeDependencyManagement(DependencyManagement dependencyManagement, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "dependencyManagement");
		if ((dependencyManagement.getDependencies() != null) && (dependencyManagement.getDependencies().size() > 0)) {
			serializer.startTag(NAMESPACE, "dependencies");
			for (Dependency o : dependencyManagement.getDependencies()) {
				writeDependency(o, serializer);
			}
			serializer.endTag(NAMESPACE, "dependencies");
		}
		serializer.endTag(NAMESPACE, "dependencyManagement");
	}

	private void writeDeploymentRepository(DeploymentRepository deploymentRepository, String tagName, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (!deploymentRepository.isUniqueVersion()) {
			serializer.startTag(NAMESPACE, "uniqueVersion").text(String.valueOf(deploymentRepository.isUniqueVersion())).endTag(NAMESPACE, "uniqueVersion");
		}
		if (deploymentRepository.getReleases() != null) {
			writeRepositoryPolicy(deploymentRepository.getReleases(), "releases", serializer);
		}
		if (deploymentRepository.getSnapshots() != null) {
			writeRepositoryPolicy(deploymentRepository.getSnapshots(), "snapshots", serializer);
		}
		if (deploymentRepository.getId() != null) {
			serializer.startTag(NAMESPACE, "id").text(deploymentRepository.getId()).endTag(NAMESPACE, "id");
		}
		if (deploymentRepository.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(deploymentRepository.getName()).endTag(NAMESPACE, "name");
		}
		if (deploymentRepository.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(deploymentRepository.getUrl()).endTag(NAMESPACE, "url");
		}
		if ((deploymentRepository.getLayout() != null) && !deploymentRepository.getLayout().equals("default")) {
			serializer.startTag(NAMESPACE, "layout").text(deploymentRepository.getLayout()).endTag(NAMESPACE, "layout");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private void writeDeveloper(Developer developer, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "developer");
		if (developer.getId() != null) {
			serializer.startTag(NAMESPACE, "id").text(developer.getId()).endTag(NAMESPACE, "id");
		}
		if (developer.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(developer.getName()).endTag(NAMESPACE, "name");
		}
		if (developer.getEmail() != null) {
			serializer.startTag(NAMESPACE, "email").text(developer.getEmail()).endTag(NAMESPACE, "email");
		}
		if (developer.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(developer.getUrl()).endTag(NAMESPACE, "url");
		}
		if (developer.getOrganization() != null) {
			serializer.startTag(NAMESPACE, "organization").text(developer.getOrganization()).endTag(NAMESPACE, "organization");
		}
		if (developer.getOrganizationUrl() != null) {
			serializer.startTag(NAMESPACE, "organizationUrl").text(developer.getOrganizationUrl()).endTag(NAMESPACE, "organizationUrl");
		}
		if ((developer.getRoles() != null) && (developer.getRoles().size() > 0)) {
			serializer.startTag(NAMESPACE, "roles");
			for (String role : developer.getRoles()) {
				serializer.startTag(NAMESPACE, "role").text(role).endTag(NAMESPACE, "role");
			}
			serializer.endTag(NAMESPACE, "roles");
		}
		if (developer.getTimezone() != null) {
			serializer.startTag(NAMESPACE, "timezone").text(developer.getTimezone()).endTag(NAMESPACE, "timezone");
		}
		if ((developer.getProperties() != null) && (developer.getProperties().size() > 0)) {
			serializer.startTag(NAMESPACE, "properties");
			for (Object o : developer.getProperties().keySet()) {
				String key = (String)o;
				String value = (String)developer.getProperties().get(key);
				serializer.startTag(NAMESPACE, key).text(value).endTag(NAMESPACE, key);
			}
			serializer.endTag(NAMESPACE, "properties");
		}
		serializer.endTag(NAMESPACE, "developer");
	}

	private void writeDistributionManagement(DistributionManagement distributionManagement, MXSerializer serializer)
			throws java.io.IOException {
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
			serializer.startTag(NAMESPACE, "downloadUrl").text(distributionManagement.getDownloadUrl()).endTag(NAMESPACE, "downloadUrl");
		}
		if (distributionManagement.getRelocation() != null) {
			writeRelocation(distributionManagement.getRelocation(), serializer);
		}
		if (distributionManagement.getStatus() != null) {
			serializer.startTag(NAMESPACE, "status").text(distributionManagement.getStatus()).endTag(NAMESPACE, "status");
		}
		serializer.endTag(NAMESPACE, "distributionManagement");
	}

	private void writeExclusion(Exclusion exclusion, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "exclusion");
		if (exclusion.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(exclusion.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (exclusion.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(exclusion.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		serializer.endTag(NAMESPACE, "exclusion");
	}

	private void writeExtension(Extension extension, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "extension");
		if (extension.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(extension.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (extension.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(extension.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		if (extension.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(extension.getVersion()).endTag(NAMESPACE, "version");
		}
		serializer.endTag(NAMESPACE, "extension");
	}

	private void writeIssueManagement(IssueManagement issueManagement, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "issueManagement");
		if (issueManagement.getSystem() != null) {
			serializer.startTag(NAMESPACE, "system").text(issueManagement.getSystem()).endTag(NAMESPACE, "system");
		}
		if (issueManagement.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(issueManagement.getUrl()).endTag(NAMESPACE, "url");
		}
		serializer.endTag(NAMESPACE, "issueManagement");
	}

	private void writeLicense(License license, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "license");
		if (license.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(license.getName()).endTag(NAMESPACE, "name");
		}
		if (license.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(license.getUrl()).endTag(NAMESPACE, "url");
		}
		if (license.getDistribution() != null) {
			serializer.startTag(NAMESPACE, "distribution").text(license.getDistribution()).endTag(NAMESPACE, "distribution");
		}
		if (license.getComments() != null) {
			serializer.startTag(NAMESPACE, "comments").text(license.getComments()).endTag(NAMESPACE, "comments");
		}
		serializer.endTag(NAMESPACE, "license");
	}

	private void writeMailingList(MailingList mailingList, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "mailingList");
		if (mailingList.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(mailingList.getName()).endTag(NAMESPACE, "name");
		}
		if (mailingList.getSubscribe() != null) {
			serializer.startTag(NAMESPACE, "subscribe").text(mailingList.getSubscribe()).endTag(NAMESPACE, "subscribe");
		}
		if (mailingList.getUnsubscribe() != null) {
			serializer.startTag(NAMESPACE, "unsubscribe").text(mailingList.getUnsubscribe()).endTag(NAMESPACE, "unsubscribe");
		}
		if (mailingList.getPost() != null) {
			serializer.startTag(NAMESPACE, "post").text(mailingList.getPost()).endTag(NAMESPACE, "post");
		}
		if (mailingList.getArchive() != null) {
			serializer.startTag(NAMESPACE, "archive").text(mailingList.getArchive()).endTag(NAMESPACE, "archive");
		}
		if ((mailingList.getOtherArchives() != null) && (mailingList.getOtherArchives().size() > 0)) {
			serializer.startTag(NAMESPACE, "otherArchives");
			for (String otherArchive : mailingList.getOtherArchives()) {
				serializer.startTag(NAMESPACE, "otherArchive").text(otherArchive).endTag(NAMESPACE, "otherArchive");
			}
			serializer.endTag(NAMESPACE, "otherArchives");
		}
		serializer.endTag(NAMESPACE, "mailingList");
	}

	private void writeModel(Model model, MXSerializer serializer) throws java.io.IOException {
		if (this.fileComment != null) {
			serializer.text(LINE_SEPARATOR);
			serializer.comment(this.fileComment);
		}
		serializer.text(LINE_SEPARATOR);

		//serializer.setPrefix("", "http://maven.apache.org/POM/4.0.0");
		//serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		serializer.startTag(NAMESPACE, "project");
		//serializer.attribute("", "xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd");
		serializer.getWriter().write(" xmlns=\"http://maven.apache.org/POM/4.0.0\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");

		if (model.getChildProjectUrlInheritAppendPath() != null) {
			serializer.attribute(NAMESPACE, "child.project.url.inherit.append.path", model.getChildProjectUrlInheritAppendPath());
		}
		if (model.getModelVersion() != null) {
			serializer.startTag(NAMESPACE, "modelVersion").text(model.getModelVersion()).endTag(NAMESPACE, "modelVersion");
		}
		if (model.getParent() != null) {
			writeParent(model.getParent(), serializer);
		}
		if (model.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(model.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (model.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(model.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		if (model.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(model.getVersion()).endTag(NAMESPACE, "version");
		}
		if ((model.getPackaging() != null) && !model.getPackaging().equals("jar")) {
			serializer.startTag(NAMESPACE, "packaging").text(model.getPackaging()).endTag(NAMESPACE, "packaging");
		}
		if (model.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(model.getName()).endTag(NAMESPACE, "name");
		}
		if (model.getDescription() != null) {
			serializer.startTag(NAMESPACE, "description").text(model.getDescription()).endTag(NAMESPACE, "description");
		}
		if (model.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(model.getUrl()).endTag(NAMESPACE, "url");
		}
		if (model.getInceptionYear() != null) {
			serializer.startTag(NAMESPACE, "inceptionYear").text(model.getInceptionYear()).endTag(NAMESPACE, "inceptionYear");
		}
		if (model.getOrganization() != null) {
			writeOrganization(model.getOrganization(), serializer);
		}
		if ((model.getLicenses() != null) && (model.getLicenses().size() > 0)) {
			serializer.startTag(NAMESPACE, "licenses");
			for (License o : model.getLicenses()) {
				writeLicense(o, serializer);
			}
			serializer.endTag(NAMESPACE, "licenses");
		}
		if ((model.getDevelopers() != null) && (model.getDevelopers().size() > 0)) {
			serializer.startTag(NAMESPACE, "developers");
			for (Developer o : model.getDevelopers()) {
				writeDeveloper(o, serializer);
			}
			serializer.endTag(NAMESPACE, "developers");
		}
		if ((model.getContributors() != null) && (model.getContributors().size() > 0)) {
			serializer.startTag(NAMESPACE, "contributors");
			for (Contributor o : model.getContributors()) {
				writeContributor(o, serializer);
			}
			serializer.endTag(NAMESPACE, "contributors");
		}
		if ((model.getMailingLists() != null) && (model.getMailingLists().size() > 0)) {
			serializer.startTag(NAMESPACE, "mailingLists");
			for (MailingList o : model.getMailingLists()) {
				writeMailingList(o, serializer);
			}
			serializer.endTag(NAMESPACE, "mailingLists");
		}
		if (model.getPrerequisites() != null) {
			writePrerequisites(model.getPrerequisites(), serializer);
		}
		if ((model.getModules() != null) && (model.getModules().size() > 0)) {
			serializer.startTag(NAMESPACE, "modules");
			for (String module : model.getModules()) {
				serializer.startTag(NAMESPACE, "module").text(module).endTag(NAMESPACE, "module");
			}
			serializer.endTag(NAMESPACE, "modules");
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
			serializer.startTag(NAMESPACE, "properties");
			for (Object o : model.getProperties().keySet()) {
				String key = (String)o;
				String value = (String)model.getProperties().get(key);
				serializer.startTag(NAMESPACE, key).text(value).endTag(NAMESPACE, key);
			}
			serializer.endTag(NAMESPACE, "properties");
		}
		if (model.getDependencyManagement() != null) {
			writeDependencyManagement(model.getDependencyManagement(), serializer);
		}
		if ((model.getDependencies() != null) && (model.getDependencies().size() > 0)) {
			serializer.startTag(NAMESPACE, "dependencies");
			for (Dependency o : model.getDependencies()) {
				writeDependency(o, serializer);
			}
			serializer.endTag(NAMESPACE, "dependencies");
		}
		if ((model.getRepositories() != null) && (model.getRepositories().size() > 0)) {
			serializer.startTag(NAMESPACE, "repositories");
			for (Repository o : model.getRepositories()) {
				writeRepository(o, "repository", serializer);
			}
			serializer.endTag(NAMESPACE, "repositories");
		}
		if ((model.getPluginRepositories() != null) && (model.getPluginRepositories().size() > 0)) {
			serializer.startTag(NAMESPACE, "pluginRepositories");
			for (Repository o : model.getPluginRepositories()) {
				writeRepository(o, "pluginRepository", serializer);
			}
			serializer.endTag(NAMESPACE, "pluginRepositories");
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
			serializer.startTag(NAMESPACE, "profiles");
			for (Profile o : model.getProfiles()) {
				writeProfile(o, serializer);
			}
			serializer.endTag(NAMESPACE, "profiles");
		}
		serializer.endTag(NAMESPACE, "project");
	}

	private void writeNotifier(Notifier notifier, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "notifier");
		if ((notifier.getType() != null) && !notifier.getType().equals("mail")) {
			serializer.startTag(NAMESPACE, "type").text(notifier.getType()).endTag(NAMESPACE, "type");
		}
		if (!notifier.isSendOnError()) {
			serializer.startTag(NAMESPACE, "sendOnError").text(String.valueOf(notifier.isSendOnError())).endTag(NAMESPACE, "sendOnError");
		}
		if (!notifier.isSendOnFailure()) {
			serializer.startTag(NAMESPACE, "sendOnFailure").text(String.valueOf(notifier.isSendOnFailure())).endTag(NAMESPACE, "sendOnFailure");
		}
		if (!notifier.isSendOnSuccess()) {
			serializer.startTag(NAMESPACE, "sendOnSuccess").text(String.valueOf(notifier.isSendOnSuccess())).endTag(NAMESPACE, "sendOnSuccess");
		}
		if (!notifier.isSendOnWarning()) {
			serializer.startTag(NAMESPACE, "sendOnWarning").text(String.valueOf(notifier.isSendOnWarning())).endTag(NAMESPACE, "sendOnWarning");
		}
		if (notifier.getAddress() != null) {
			serializer.startTag(NAMESPACE, "address").text(notifier.getAddress()).endTag(NAMESPACE, "address");
		}
		if ((notifier.getConfiguration() != null) && (notifier.getConfiguration().size() > 0)) {
			serializer.startTag(NAMESPACE, "configuration");
			for (Object o : notifier.getConfiguration().keySet()) {
				String key = (String)o;
				String value = (String)notifier.getConfiguration().get(key);
				serializer.startTag(NAMESPACE, key).text(value).endTag(NAMESPACE, key);
			}
			serializer.endTag(NAMESPACE, "configuration");
		}
		serializer.endTag(NAMESPACE, "notifier");
	}

	private void writeOrganization(Organization organization, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "organization");
		if (organization.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(organization.getName()).endTag(NAMESPACE, "name");
		}
		if (organization.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(organization.getUrl()).endTag(NAMESPACE, "url");
		}
		serializer.endTag(NAMESPACE, "organization");
	}

	private void writeParent(Parent parent, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "parent");
		if (parent.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(parent.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (parent.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(parent.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		if (parent.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(parent.getVersion()).endTag(NAMESPACE, "version");
		}
		if ((parent.getRelativePath() != null) && !parent.getRelativePath().equals("../pom.xml")) {
			serializer.startTag(NAMESPACE, "relativePath").text(parent.getRelativePath()).endTag(NAMESPACE, "relativePath");
		}
		serializer.endTag(NAMESPACE, "parent");
	}

	private void writePlugin(Plugin plugin, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "plugin");
		if ((plugin.getGroupId() != null) && !plugin.getGroupId().equals("org.apache.maven.plugins")) {
			serializer.startTag(NAMESPACE, "groupId").text(plugin.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (plugin.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(plugin.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		if (plugin.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(plugin.getVersion()).endTag(NAMESPACE, "version");
		}
		if (plugin.getExtensions() != null) {
			serializer.startTag(NAMESPACE, "extensions").text(plugin.getExtensions()).endTag(NAMESPACE, "extensions");
		}
		if ((plugin.getExecutions() != null) && (plugin.getExecutions().size() > 0)) {
			serializer.startTag(NAMESPACE, "executions");
			for (PluginExecution o : plugin.getExecutions()) {
				writePluginExecution(o, serializer);
			}
			serializer.endTag(NAMESPACE, "executions");
		}
		if ((plugin.getDependencies() != null) && (plugin.getDependencies().size() > 0)) {
			serializer.startTag(NAMESPACE, "dependencies");
			for (Dependency o : plugin.getDependencies()) {
				writeDependency(o, serializer);
			}
			serializer.endTag(NAMESPACE, "dependencies");
		}
		if (plugin.getGoals() != null) {
			((Xpp3Dom)plugin.getGoals()).writeToSerializer(NAMESPACE, serializer);
		}
		if (plugin.getInherited() != null) {
			serializer.startTag(NAMESPACE, "inherited").text(plugin.getInherited()).endTag(NAMESPACE, "inherited");
		}
		if (plugin.getConfiguration() != null) {
			((Xpp3Dom)plugin.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, "plugin");
	}

	private void writePluginExecution(PluginExecution pluginExecution, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "execution");
		if ((pluginExecution.getId() != null) && !pluginExecution.getId().equals("default")) {
			serializer.startTag(NAMESPACE, "id").text(pluginExecution.getId()).endTag(NAMESPACE, "id");
		}
		if (pluginExecution.getPhase() != null) {
			serializer.startTag(NAMESPACE, "phase").text(pluginExecution.getPhase()).endTag(NAMESPACE, "phase");
		}
		if ((pluginExecution.getGoals() != null) && (pluginExecution.getGoals().size() > 0)) {
			serializer.startTag(NAMESPACE, "goals");
			for (String goal : pluginExecution.getGoals()) {
				serializer.startTag(NAMESPACE, "goal").text(goal).endTag(NAMESPACE, "goal");
			}
			serializer.endTag(NAMESPACE, "goals");
		}
		if (pluginExecution.getInherited() != null) {
			serializer.startTag(NAMESPACE, "inherited").text(pluginExecution.getInherited()).endTag(NAMESPACE, "inherited");
		}
		if (pluginExecution.getConfiguration() != null) {
			((Xpp3Dom)pluginExecution.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, "execution");
	}

	private void writePluginManagement(PluginManagement pluginManagement, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "pluginManagement");
		if ((pluginManagement.getPlugins() != null) && (pluginManagement.getPlugins().size() > 0)) {
			serializer.startTag(NAMESPACE, "plugins");
			for (Plugin o : pluginManagement.getPlugins()) {
				writePlugin(o, serializer);
			}
			serializer.endTag(NAMESPACE, "plugins");
		}
		serializer.endTag(NAMESPACE, "pluginManagement");
	}

	private void writePrerequisites(Prerequisites prerequisites, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "prerequisites");
		if ((prerequisites.getMaven() != null) && !prerequisites.getMaven().equals("2.0")) {
			serializer.startTag(NAMESPACE, "maven").text(prerequisites.getMaven()).endTag(NAMESPACE, "maven");
		}
		serializer.endTag(NAMESPACE, "prerequisites");
	}

	private void writeProfile(Profile profile, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "profile");
		if ((profile.getId() != null) && !profile.getId().equals("default")) {
			serializer.startTag(NAMESPACE, "id").text(profile.getId()).endTag(NAMESPACE, "id");
		}
		if (profile.getActivation() != null) {
			writeActivation((Activation)profile.getActivation(), serializer);
		}
		if (profile.getBuild() != null) {
			writeBuildBase((BuildBase)profile.getBuild(), serializer);
		}
		if ((profile.getModules() != null) && (profile.getModules().size() > 0)) {
			serializer.startTag(NAMESPACE, "modules");
			for (String module : profile.getModules()) {
				serializer.startTag(NAMESPACE, "module").text(module).endTag(NAMESPACE, "module");
			}
			serializer.endTag(NAMESPACE, "modules");
		}
		if (profile.getDistributionManagement() != null) {
			writeDistributionManagement((DistributionManagement)profile.getDistributionManagement(), serializer);
		}
		if ((profile.getProperties() != null) && (profile.getProperties().size() > 0)) {
			serializer.startTag(NAMESPACE, "properties");
			for (Object o : profile.getProperties().keySet()) {
				String key = (String)o;
				String value = (String)profile.getProperties().get(key);
				serializer.startTag(NAMESPACE, key).text(value).endTag(NAMESPACE, key);
			}
			serializer.endTag(NAMESPACE, "properties");
		}
		if (profile.getDependencyManagement() != null) {
			writeDependencyManagement((DependencyManagement)profile.getDependencyManagement(), serializer);
		}
		if ((profile.getDependencies() != null) && (profile.getDependencies().size() > 0)) {
			serializer.startTag(NAMESPACE, "dependencies");
			for (Dependency o : profile.getDependencies()) {
				writeDependency(o, serializer);
			}
			serializer.endTag(NAMESPACE, "dependencies");
		}
		if ((profile.getRepositories() != null) && (profile.getRepositories().size() > 0)) {
			serializer.startTag(NAMESPACE, "repositories");
			for (Repository o : profile.getRepositories()) {
				writeRepository(o, "repository", serializer);
			}
			serializer.endTag(NAMESPACE, "repositories");
		}
		if ((profile.getPluginRepositories() != null) && (profile.getPluginRepositories().size() > 0)) {
			serializer.startTag(NAMESPACE, "pluginRepositories");
			for (Repository o : profile.getPluginRepositories()) {
				writeRepository(o, "pluginRepository", serializer);
			}
			serializer.endTag(NAMESPACE, "pluginRepositories");
		}
		if (profile.getReports() != null) {
			((Xpp3Dom)profile.getReports()).writeToSerializer(NAMESPACE, serializer);
		}
		if (profile.getReporting() != null) {
			writeReporting((Reporting)profile.getReporting(), serializer);
		}
		serializer.endTag(NAMESPACE, "profile");
	}

	private void writeRelocation(Relocation relocation, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "relocation");
		if (relocation.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(relocation.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (relocation.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(relocation.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		if (relocation.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(relocation.getVersion()).endTag(NAMESPACE, "version");
		}
		if (relocation.getMessage() != null) {
			serializer.startTag(NAMESPACE, "message").text(relocation.getMessage()).endTag(NAMESPACE, "message");
		}
		serializer.endTag(NAMESPACE, "relocation");
	}

	private void writeReportPlugin(ReportPlugin reportPlugin, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "plugin");
		if ((reportPlugin.getGroupId() != null) && !reportPlugin.getGroupId().equals("org.apache.maven.plugins")) {
			serializer.startTag(NAMESPACE, "groupId").text(reportPlugin.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (reportPlugin.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(reportPlugin.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		if (reportPlugin.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(reportPlugin.getVersion()).endTag(NAMESPACE, "version");
		}
		if ((reportPlugin.getReportSets() != null) && (reportPlugin.getReportSets().size() > 0)) {
			serializer.startTag(NAMESPACE, "reportSets");
			for (ReportSet o : reportPlugin.getReportSets()) {
				writeReportSet(o, serializer);
			}
			serializer.endTag(NAMESPACE, "reportSets");
		}
		if (reportPlugin.getInherited() != null) {
			serializer.startTag(NAMESPACE, "inherited").text(reportPlugin.getInherited()).endTag(NAMESPACE, "inherited");
		}
		if (reportPlugin.getConfiguration() != null) {
			((Xpp3Dom)reportPlugin.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, "plugin");
	}

	private void writeReportSet(ReportSet reportSet, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "reportSet");
		if ((reportSet.getId() != null) && !reportSet.getId().equals("default")) {
			serializer.startTag(NAMESPACE, "id").text(reportSet.getId()).endTag(NAMESPACE, "id");
		}
		if ((reportSet.getReports() != null) && (reportSet.getReports().size() > 0)) {
			serializer.startTag(NAMESPACE, "reports");
			for (String report : reportSet.getReports()) {
				serializer.startTag(NAMESPACE, "report").text(report).endTag(NAMESPACE, "report");
			}
			serializer.endTag(NAMESPACE, "reports");
		}
		if (reportSet.getInherited() != null) {
			serializer.startTag(NAMESPACE, "inherited").text(reportSet.getInherited()).endTag(NAMESPACE, "inherited");
		}
		if (reportSet.getConfiguration() != null) {
			((Xpp3Dom)reportSet.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, "reportSet");
	}

	private void writeReporting(Reporting reporting, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "reporting");
		if (reporting.getExcludeDefaults() != null) {
			serializer.startTag(NAMESPACE, "excludeDefaults").text(reporting.getExcludeDefaults()).endTag(NAMESPACE, "excludeDefaults");
		}
		if (reporting.getOutputDirectory() != null) {
			serializer.startTag(NAMESPACE, "outputDirectory").text(reporting.getOutputDirectory()).endTag(NAMESPACE, "outputDirectory");
		}
		if ((reporting.getPlugins() != null) && (reporting.getPlugins().size() > 0)) {
			serializer.startTag(NAMESPACE, "plugins");
			for (ReportPlugin o : reporting.getPlugins()) {
				writeReportPlugin(o, serializer);
			}
			serializer.endTag(NAMESPACE, "plugins");
		}
		serializer.endTag(NAMESPACE, "reporting");
	}

	private void writeRepository(Repository repository, String tagName, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (repository.getReleases() != null) {
			writeRepositoryPolicy(repository.getReleases(), "releases", serializer);
		}
		if (repository.getSnapshots() != null) {
			writeRepositoryPolicy(repository.getSnapshots(), "snapshots", serializer);
		}
		if (repository.getId() != null) {
			serializer.startTag(NAMESPACE, "id").text(repository.getId()).endTag(NAMESPACE, "id");
		}
		if (repository.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(repository.getName()).endTag(NAMESPACE, "name");
		}
		if (repository.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(repository.getUrl()).endTag(NAMESPACE, "url");
		}
		if ((repository.getLayout() != null) && !repository.getLayout().equals("default")) {
			serializer.startTag(NAMESPACE, "layout").text(repository.getLayout()).endTag(NAMESPACE, "layout");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private void writeRepositoryPolicy(RepositoryPolicy repositoryPolicy, String tagName, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (repositoryPolicy.getEnabled() != null) {
			serializer.startTag(NAMESPACE, "enabled").text(repositoryPolicy.getEnabled()).endTag(NAMESPACE, "enabled");
		}
		if (repositoryPolicy.getUpdatePolicy() != null) {
			serializer.startTag(NAMESPACE, "updatePolicy").text(repositoryPolicy.getUpdatePolicy()).endTag(NAMESPACE, "updatePolicy");
		}
		if (repositoryPolicy.getChecksumPolicy() != null) {
			serializer.startTag(NAMESPACE, "checksumPolicy").text(repositoryPolicy.getChecksumPolicy()).endTag(NAMESPACE, "checksumPolicy");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private void writeResource(Resource resource, String tagName, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (resource.getTargetPath() != null) {
			serializer.startTag(NAMESPACE, "targetPath").text(resource.getTargetPath()).endTag(NAMESPACE, "targetPath");
		}
		if (resource.getFiltering() != null) {
			serializer.startTag(NAMESPACE, "filtering").text(resource.getFiltering()).endTag(NAMESPACE, "filtering");
		}
		if (resource.getDirectory() != null) {
			serializer.startTag(NAMESPACE, "directory").text(resource.getDirectory()).endTag(NAMESPACE, "directory");
		}
		if ((resource.getIncludes() != null) && (resource.getIncludes().size() > 0)) {
			serializer.startTag(NAMESPACE, "includes");
			for (String include : resource.getIncludes()) {
				serializer.startTag(NAMESPACE, "include").text(include).endTag(NAMESPACE, "include");
			}
			serializer.endTag(NAMESPACE, "includes");
		}
		if ((resource.getExcludes() != null) && (resource.getExcludes().size() > 0)) {
			serializer.startTag(NAMESPACE, "excludes");
			for (String exclude : resource.getExcludes()) {
				serializer.startTag(NAMESPACE, "exclude").text(exclude).endTag(NAMESPACE, "exclude");
			}
			serializer.endTag(NAMESPACE, "excludes");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private void writeScm(Scm scm, MXSerializer serializer)
			throws java.io.IOException {
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
			serializer.startTag(NAMESPACE, "connection").text(scm.getConnection()).endTag(NAMESPACE, "connection");
		}
		if (scm.getDeveloperConnection() != null) {
			serializer.startTag(NAMESPACE, "developerConnection").text(scm.getDeveloperConnection()).endTag(NAMESPACE, "developerConnection");
		}
		if ((scm.getTag() != null) && !scm.getTag().equals("HEAD")) {
			serializer.startTag(NAMESPACE, "tag").text(scm.getTag()).endTag(NAMESPACE, "tag");
		}
		if (scm.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(scm.getUrl()).endTag(NAMESPACE, "url");
		}
		serializer.endTag(NAMESPACE, "scm");
	}

	private void writeSite(Site site, MXSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, "site");
		if (site.getChildSiteUrlInheritAppendPath() != null) {
			serializer.attribute(NAMESPACE, "child.site.url.inherit.append.path", site.getChildSiteUrlInheritAppendPath());
		}
		if (site.getId() != null) {
			serializer.startTag(NAMESPACE, "id").text(site.getId()).endTag(NAMESPACE, "id");
		}
		if (site.getName() != null) {
			serializer.startTag(NAMESPACE, "name").text(site.getName()).endTag(NAMESPACE, "name");
		}
		if (site.getUrl() != null) {
			serializer.startTag(NAMESPACE, "url").text(site.getUrl()).endTag(NAMESPACE, "url");
		}
		serializer.endTag(NAMESPACE, "site");
	}
}
