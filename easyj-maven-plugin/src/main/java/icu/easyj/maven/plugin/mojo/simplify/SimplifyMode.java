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

import javax.annotation.Nullable;

/**
 * 简化模式枚举
 *
 * @author wangliang181230
 * @since 0.4.0
 */
public enum SimplifyMode {

	/**
	 * 空模式，通过自定义配置来简化POM.
	 * 以下两个枚举一样的作用。
	 */
	NONE,
	NOOP,


	//region 构件类型

	/**
	 * 普通的JAR包
	 */
	JAR,

	/**
	 * 普通的WAR包
	 */
	WAR,

	/**
	 * 普通的POM
	 */
	POM,

	/**
	 * Maven插件
	 */
	MAVEN_PLUGIN,

	//endregion


	//region 特殊的构件

	/**
	 * 管理依赖模块（特殊的POM）
	 */
	DEPENDENCIES,

	/**
	 * 组件清单（特殊的DEPENDENCIES，只包含当前项目自己的组件）
	 */
	BOM,

	/**
	 * 启动器（可以是jar，也可以是pom）
	 */
	STARTER,

	/**
	 * 合并的JAR包
	 */
	SHADE,

	//endregion

	;

	/**
	 * 根据字符串，获取枚举
	 *
	 * @param modeStr 模式字符串
	 * @return 返回模式枚举
	 * @throws IllegalArgumentException 参数错误
	 */
	@Nullable
	public static SimplifyMode getByModeStr(String modeStr) throws IllegalArgumentException {
		if (modeStr == null || modeStr.isEmpty()) {
			return null;
		}

		modeStr = modeStr.replace('-', '_').toUpperCase();

		return SimplifyMode.valueOf(modeStr);
	}
}
