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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * 对象工具类
 *
 * @author wangliang181230
 * @since 0.4.2
 */
public abstract class ObjectUtils {

	/**
	 * 判断对象是否为 null 或 空
	 *
	 * @param obj 对象
	 * @return true=空 | false=不空
	 */
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		}

		if (obj instanceof String) {
			String str = obj.toString();
			return str.isEmpty();
		} else if (obj instanceof Collection) {
			Collection list = (Collection)obj;
			return list.isEmpty();
		} else if (obj instanceof Map) {
			Map map = (Map)obj;
			return map.isEmpty();
		} else if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0;
		}

		return false;
	}

	/**
	 * 判断对象是否不为 null 或 空
	 *
	 * @param obj 对象
	 * @return true=不空 | false=空
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	/**
	 * 判断值是否在目标值数组中
	 *
	 * @param value        值
	 * @param targetValues 目标值数组
	 * @param <T>          值类型
	 * @return true=存在 | false=不存在
	 */
	@SafeVarargs
	public static <T> boolean in(T value, T... targetValues) {
		for (T targetValue : targetValues) {
			if (Objects.equals(value, targetValue)) {
				return true;
			}
		}

		return false;
	}

	//region @since 1.0.3：兼容 低版本maven 或 高版本maven

	@Nullable
	public static <T> T invokeMethod(Object obj, String methodName) {
		Method method;
		try {
			method = obj.getClass().getMethod(methodName);
		} catch (NoSuchMethodException e) {
			return null;
		}

		try {
			return (T)method.invoke(obj);
		} catch (Exception e) {
			throw new RuntimeException("Invoke method '" + method.getName() + "' failed", e);
		}
	}

	@Nullable
	public static <T> T invokeMethod(Object obj, String methodName, Class<?>[] classes, Object... args) {
		Method method;
		try {
			method = obj.getClass().getMethod(methodName, classes);
		} catch (NoSuchMethodException e) {
			return null;
		}

		try {
			return (T)method.invoke(obj, args);
		} catch (Exception e) {
			throw new RuntimeException("Invoke method '" + method.getName() + "' failed", e);
		}
	}
}
