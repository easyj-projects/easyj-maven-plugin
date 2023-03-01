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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import icu.easyj.maven.plugin.mojo.utils.IOUtils;
import icu.easyj.maven.plugin.mojo.utils.MavenXpp3Writer;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 抽象的简化POM的 Goal
 *
 * @author wangliang181230
 * @since 0.4.0
 */
public abstract class AbstractSimplifyPomMojo extends AbstractMojo {

	protected static final int POM_WRITER_SIZE = 4096;

	@Parameter(defaultValue = "${project.basedir}")
	private File outputDirectory;

	@Parameter(property = "maven.simplify.simplifiedPomFileName", defaultValue = ".simplified-pom.xml")
	protected String simplifiedPomFileName;

	@Parameter(property = "maven.simplify.fileComment")
	private String fileComment;

	@Parameter(property = "maven.simplify.useTabIndent", defaultValue = "false")
	private boolean useTabIndent;


	protected File getSimplifiedPomFile() {
		return new File(this.outputDirectory, this.simplifiedPomFileName);
	}

	protected void writePom(Model model, File pomFile) throws MojoExecutionException {
		// Create dir
		File parentFile = pomFile.getParentFile();
		if (!parentFile.exists()) {
			boolean success = parentFile.mkdirs();
			if (!success) {
				throw new MojoExecutionException("Failed to create directory " + pomFile.getParent());
			}
		}

		// Handle encoding
		if (model.getModelEncoding() == null) {
			getLog().warn("No encoding specified for " + pomFile + ", using " + StandardCharsets.UTF_8);
			model.setModelEncoding(StandardCharsets.UTF_8.name());
		}

		// Model to String
		MavenXpp3Writer pomWriter = new MavenXpp3Writer(model, this.fileComment, this.useTabIndent);
		String pomFileString;
		try (StringWriter stringWriter = new StringWriter(POM_WRITER_SIZE)) {
			pomWriter.write(stringWriter);
			pomFileString = stringWriter.getBuffer().toString();
		} catch (IOException e) {
			throw new MojoExecutionException("Internal I/O error!", e);
		}

		// 去除 '\r'
		pomFileString = pomFileString.replace("\r", "");
		// 不同的maven版本，换行数量有些微不同，将多个连续的换行替换成单个换行
		pomFileString = pomFileString.replaceAll("\n{2,}", IOUtils.LINE_SEPARATOR);
		// 去除多余的空格
		pomFileString = pomFileString.replace(" />", "/>");

		// Write String to POM file
		this.writeStringToFile(pomFileString, pomFile, model.getModelEncoding());
	}

	private void writeStringToFile(String data, File file, String encoding)
			throws MojoExecutionException {
		byte[] binaryData;
		try {
			binaryData = data.getBytes(encoding);
			if (file.isFile() && file.canRead() && file.length() == binaryData.length) {
				try (InputStream inputStream = Files.newInputStream(file.toPath())) {
					byte[] buffer = new byte[binaryData.length];
					inputStream.read(buffer);
					if (Arrays.equals(buffer, binaryData)) {
						getLog().debug("Arrays.equals( buffer, binaryData ) ");
						return;
					}
					getLog().debug("Not Arrays.equals( buffer, binaryData ) ");
				} catch (IOException e) {
					// ignore those exceptions, we will overwrite the file
					getLog().debug("Issue reading file: " + file.getPath(), e);
				}
			} else {
				getLog().debug("file: " + file + ",file.length(): " + file.length() + ", binaryData.length: " + binaryData.length);
			}
		} catch (IOException e) {
			throw new MojoExecutionException("cannot read String as bytes", e);
		}
		try (OutputStream outStream = Files.newOutputStream(file.toPath())) {
			outStream.write(binaryData);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to write to " + file, e);
		}
	}
}
