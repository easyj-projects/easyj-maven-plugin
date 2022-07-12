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
import java.io.StringWriter;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MavenXpp3Writer}
 *
 * @author wangliang181230
 */
@Disabled("手动测试")
public class MavenXpp3WriterTest {

	protected static final int POM_WRITER_SIZE = 4096;

	@Test
	public void testWrite() throws IOException {
		Model model = new Model();

		Properties properties = new Properties();
		properties.put("aaa", "bbb");
		model.setProperties(properties);

		MavenXpp3Writer pomWriter = new MavenXpp3Writer();
		pomWriter.setFileComment("测试fileComment");
		pomWriter.setUseTabIndent(true);

		StringWriter stringWriter = new StringWriter(POM_WRITER_SIZE);
		pomWriter.write(stringWriter, model);
		StringBuffer buffer = stringWriter.getBuffer();
		System.out.println(buffer);
	}

	@Test
	public void testReplace() {
		String data = "  <properties>\r\n" +
				"    <a>  \r\n" +
				"  asdflkjlsadkfjlsdkafj\r\n" +
				"    lsakdjflkasdjflksjadf\r\n" +
				"    </a>\r\n" +
				"        </properties>";
		String data2 = data.replaceAll("(?=(^|  |\t))  (?=(\\s*\\<))", "\t");
		System.out.println(data2);
	}
}
