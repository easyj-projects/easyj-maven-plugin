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
package icu.easyj.maven.plugin.mojo.packagezip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icu.easyj.maven.plugin.mojo.utils.ObjectUtils;
import icu.easyj.maven.plugin.mojo.utils.ZipUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 将指定目录打包成zip 的 Goal
 *
 * @author wangliang181230
 * @since 0.6.9
 */
@Mojo(name = "package-zip", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class PackageZipMojo extends AbstractMojo {

	@Parameter
	private List<String> paths;

	@Parameter
	private String outputFilePathname;

	@Parameter(defaultValue = "true")
	private boolean keepDirStructure;

	@Parameter
	private String pathInZip;


	@Override
	public void execute() throws MojoExecutionException {
		if (ObjectUtils.isEmpty(paths)) {
			getLog().warn("'paths' must not be empty.");
			return;
		}
		if (ObjectUtils.isEmpty(outputFilePathname)) {
			getLog().warn("'outputFilePathname' must not be empty.");
			return;
		}

		List<File> files = new ArrayList<>();
		for (String directory : paths) {
			if (directory != null && !directory.trim().isEmpty()) {
				File file = new File(directory.trim());
				if (file.exists()) {
					files.add(file);
				} else {
					getLog().info("The file is not exists: " + directory);
				}
			}
		}

		if (files.isEmpty()) {
			getLog().info("No file exists, skip this goal.");
			return;
		}

		// 打印日志
		getLog().info("The files: (" + files.size() + ")");
		for (File file : files) {
			getLog().info(" - " + file.getPath());
		}

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outputFilePathname);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Create 'fos' failed", e);
		}

		getLog().info("");
		getLog().info("The output file: " + outputFilePathname);

		try {
			ZipUtils.toZip(files, fos, this.keepDirStructure, this.pathInZip);
		} catch (IOException e) {
			throw new RuntimeException("Zip files failed", e);
		}
	}
}
