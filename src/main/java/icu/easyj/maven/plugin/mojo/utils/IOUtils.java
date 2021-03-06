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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * IO工具类
 *
 * @author wangliang181230
 * @since 0.7.2
 */
public abstract class IOUtils {

	//public static final String LINE_SEPARATOR = System.getProperty("os.name").contains("Windows") ? "\r\n" : "\n";
	public static final String LINE_SEPARATOR = "\n";


	/**
	 * 复制文件
	 *
	 * @param sourceFile 源文件
	 * @param targetFile 目标文件
	 * @throws IOException IO异常
	 */
	public static void copy(File sourceFile, File targetFile) throws IOException {
		try (FileInputStream in = new FileInputStream(sourceFile);
			 FileOutputStream out = new FileOutputStream(targetFile)) {
			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			out.write(buffer);
			out.flush();
		}
		try {
			targetFile.setLastModified(getFileLastModified(sourceFile));
		} catch (SecurityException ignore) {
		}
	}

	/**
	 * 创建文件
	 *
	 * @param newFile 文件
	 * @param text    文件内容
	 * @throws IOException IO异常
	 */
	public static void createFile(File newFile, String text) throws IOException {
		try (FileOutputStream out = new FileOutputStream(newFile)) {
			out.write(text.getBytes());
			out.flush();
		}
	}

	/**
	 * 读取文件最近一次修改时间。
	 *
	 * @param file 文件
	 * @return lastModified 最近一次修改时间
	 */
	public static long getFileLastModified(File file) {
		if (file.getName().endsWith(".jar")) {
			// 如果是jar文件，则读取 /META-INF/MANIFEST.MF 文件的lastModified。
			// 因为jar文件是和该文件一起生成的，所以这个文件的lastModified才是jar文件的实际最后生成时间
			try (JarFile jarFile = new JarFile(file)) {
				JarEntry jarEntry = jarFile.getJarEntry(JarFile.MANIFEST_NAME);
				if (jarEntry != null) {
					return jarEntry.getTime();
				}
			} catch (IOException ignore) {
				// do nothing
			}
		}

		return file.lastModified();
	}
}
