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
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link IOUtils} 测试类
 *
 * @author wangliang181230
 */
public class IOUtilsTest {

	@Test
	public void testGetFileLastModified() {
		File file = new File(this.getClass().getClassLoader().getResource("easyj.jar").getFile());
		long lastModified = IOUtils.getFileLastModified(file);
		Assertions.assertEquals(1658369334000L, lastModified);
	}

	@Test
	public void testIsSpringBootDependenciesStarterJar() {
		File file = new File(this.getClass().getClassLoader().getResource("easyj-test.jar").getFile());
		boolean result = IOUtils.isSpringBootDependenciesStarterJar(file);
		Assertions.assertTrue(result);

		file = new File(this.getClass().getClassLoader().getResource("easyj.jar").getFile());
		result = IOUtils.isSpringBootDependenciesStarterJar(file);
		Assertions.assertFalse(result);
	}
}
