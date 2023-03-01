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

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串匹配工具类
 *
 * @author wangliang181230
 * @since 0.4.2
 */
public abstract class MatchUtils {

	/**
	 * 高级匹配，同时支持正则与通配符*
	 *
	 * @param pattern    匹配串
	 * @param str        字符串
	 * @param ignoreCase 忽略大小写
	 * @return 是否匹配：true=匹配 | false=不匹配
	 */
	public static boolean match(String pattern, String str, boolean ignoreCase) {
		if (pattern == null || pattern.isEmpty()) {
			return false;
		}

		// 如果是正则表达式，则使用正则判断
		if (pattern.charAt(0) == '^') {
			Pattern pat = ignoreCase ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) : Pattern.compile(pattern);
			Matcher matcher = pat.matcher(str);
			return matcher.matches();
		}


		char[] patArr = pattern.toCharArray();
		char[] strArr = str.toCharArray();
		int patIdxStart = 0;
		int patIdxEnd = patArr.length - 1;
		int strIdxStart = 0;
		int strIdxEnd = strArr.length - 1;

		boolean containsStar = pattern.contains("*");
		char chPat;
		int patIdxTmp;
		if (!containsStar) {
			if (patIdxEnd != strIdxEnd) {
				return false;
			} else {
				for (patIdxTmp = 0; patIdxTmp <= patIdxEnd; ++patIdxTmp) {
					chPat = patArr[patIdxTmp];
					if (chPat != '?' && !equalsChar(chPat, strArr[patIdxTmp], ignoreCase)) {
						return false;
					}
				}

				return true;
			}
		} else if (patIdxEnd == 0) {
			return true;
		} else {
			while ((chPat = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
				if (chPat != '?' && !equalsChar(chPat, strArr[strIdxStart], ignoreCase)) {
					return false;
				}

				++patIdxStart;
				++strIdxStart;
			}

			if (strIdxStart > strIdxEnd) {
				for (patIdxTmp = patIdxStart; patIdxTmp <= patIdxEnd; ++patIdxTmp) {
					if (patArr[patIdxTmp] != '*') {
						return false;
					}
				}

				return true;
			} else {
				while ((chPat = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
					if (chPat != '?' && !equalsChar(chPat, strArr[strIdxEnd], ignoreCase)) {
						return false;
					}

					--patIdxEnd;
					--strIdxEnd;
				}

				if (strIdxStart > strIdxEnd) {
					for (patIdxTmp = patIdxStart; patIdxTmp <= patIdxEnd; ++patIdxTmp) {
						if (patArr[patIdxTmp] != '*') {
							return false;
						}
					}

					return true;
				} else {
					while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
						patIdxTmp = -1;

						int patLength;
						for (patLength = patIdxStart + 1; patLength <= patIdxEnd; ++patLength) {
							if (patArr[patLength] == '*') {
								patIdxTmp = patLength;
								break;
							}
						}

						if (patIdxTmp == patIdxStart + 1) {
							++patIdxStart;
						} else {
							patLength = patIdxTmp - patIdxStart - 1;
							int strLength = strIdxEnd - strIdxStart + 1;
							int foundIdx = -1;
							int i = 0;

							label427:
							while (i <= strLength - patLength) {
								for (int j = 0; j < patLength; ++j) {
									chPat = patArr[patIdxStart + j + 1];
									if (chPat != '?' && !equalsChar(chPat, strArr[strIdxStart + i + j], ignoreCase)) {
										++i;
										continue label427;
									}
								}

								foundIdx = strIdxStart + i;
								break;
							}

							if (foundIdx == -1) {
								return false;
							}

							patIdxStart = patIdxTmp;
							strIdxStart = foundIdx + patLength;
						}
					}

					for (patIdxTmp = patIdxStart; patIdxTmp <= patIdxEnd; ++patIdxTmp) {
						if (patArr[patIdxTmp] != '*') {
							return false;
						}
					}

					return true;
				}
			}
		}
	}

	// 重载方法
	public static boolean match(String pattern, String str) {
		return match(pattern, str, true); // 忽略大小写
	}

	/**
	 * 匹配文件
	 *
	 * @param patterns 匹配串集合
	 * @param str      目标字符串
	 * @return 是否匹配
	 */
	public static boolean match(Collection<String> patterns, String str) {
		for (String pattern : patterns) {
			if (match(pattern, str)) {
				return true;
			}
		}

		return false;
	}


	private static boolean equalsChar(char c1, char c2, boolean ignoreCase) {
		if (c1 != c2) {
			if (ignoreCase) {
				//return Character.toUpperCase(c1) == Character.toUpperCase(c2);
				// 经测试，以下方式性能更优
				if (c1 > c2) {
					return Character.toUpperCase(c1) == c2;
				} else {
					return c1 == Character.toUpperCase(c2);
				}
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
}
