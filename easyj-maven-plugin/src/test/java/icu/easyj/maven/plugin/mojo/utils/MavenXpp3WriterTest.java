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
package icu.easyj.maven.plugin.mojo.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MavenXpp3Writer}
 *
 * @author wangliang181230
 */
public class MavenXpp3WriterTest {

	protected static final int POM_WRITER_SIZE = 4096;

	@Test
	public void testWrite() throws IOException, IllegalAccessException, InvocationTargetException {
		Model model = new Model();

		Properties properties = new Properties();
		properties.put("aaa", "111");
		properties.put("bbb", "");
		model.setProperties(properties);

		Scm scm = new Scm();
		//scm.setChildScmConnectionInheritAppendPath("aaa");
		boolean hasMethod = true;
		try {
			Scm.class.getMethod("setChildScmConnectionInheritAppendPath", String.class).invoke(scm, "aaa");
		} catch (NoSuchMethodException e) {
			hasMethod = false;
		}
		model.setScm(scm);
		ObjectUtils.invokeMethod(model, "setReports", new Class[]{Object.class}, new Xpp3Dom("reports"));

		MavenXpp3Writer pomWriter = new MavenXpp3Writer(model, "测试fileComment", true);

		StringWriter stringWriter = new StringWriter(POM_WRITER_SIZE);
		pomWriter.write(stringWriter);
		StringBuffer buffer = stringWriter.getBuffer();

		// 预期值
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<!--测试fileComment-->\n" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<scm" + (hasMethod ? " child.scm.connection.inherit.append.path=\"aaa\"" : "") + "/>\n" +
				"\t<properties>\n" +
				"\t\t<aaa>111</aaa>\n" +
				"\t\t<bbb/>\n" +
				"\t</properties>\n" +
				(ObjectUtils.invokeMethod(model, "getReports") != null ? "\t<reports/>\n" : "") +
				"</project>\n";

		// 实际值
		String actual = buffer.toString()
				.replace("\r", "")
				.replaceAll("\n{2,}", IOUtils.LINE_SEPARATOR)
				.replace(" />", "/>");
		// 打印一下实际值
		System.out.println(actual);

		// 比较预期值与实际值
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testReplace() {
		String data = "  <properties>\r\n" +
				"    <a>  \r\n" +
				"  xxx\r\n" +
				"    yyy\r\n" +
				"    </a>\r\n" +
				"        </properties>";
		String data2 = data.replaceAll("(?=(^|  |\t))  (?=(\\s*\\<))", "\t");
		Assertions.assertTrue(data2.length() < data.length());

		String expected = "\t<properties>\r\n" +
				"\t\t<a>  \r\n" +
				"  xxx\r\n" +
				"    yyy\r\n" +
				"\t\t</a>\r\n" +
				"\t\t\t\t</properties>";
		Assertions.assertEquals(expected, data2);
	}
}
