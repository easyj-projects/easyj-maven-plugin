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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

/**
 * 字符串工具类
 *
 * @author wangliang181230
 * @since 0.7.2
 */
public abstract class StringUtils {

	public static boolean isEmpty(final String str) {
		return str == null || str.isEmpty();
	}

	public static boolean isNotEmpty(final String str) {
		return !isEmpty(str);
	}

	public static String padLeft(Object obj, int length) {
		String str = obj.toString();

		if (str.length() >= length) {
			return str;
		}

		StringBuilder sb = new StringBuilder(length);
		length -= str.length();
		while (length-- > 0) {
			sb.append(" ");
		}
		sb.append(str);
		return sb.toString();
	}

	//region toSet

	public static Set<String> addToSet(final Set<String> set, String str) {
		assert set != null;

		// maven插件中，使用的字符串两边都不要双引号，方便mvn命令上加参数
		if (str != null) {
			while (str.startsWith("\"")) {
				str = str.substring(1);
			}
			while (str.endsWith("\"")) {
				str = str.substring(0, str.length() - 1);
			}
		}

		if (isNotEmpty(str)) {
			String[] strArr = str.split(",");

			for (String s : strArr) {
				s = s.trim();
				if (s.isEmpty()) {
					continue;
				}

				set.add(s);
			}
		}

		return set;
	}

	@Nonnull
	public static Set<String> toSet(final String str) {
		Set<String> result = new HashSet<>();
		return addToSet(result, str);
	}

	@Nonnull
	public static Set<String> toTreeSet(final String str) {
		Set<String> result = new TreeSet<>(String::compareTo);
		return addToSet(result, str);
	}

	//endregion
}
