/*
 * Copyright 2021-2024 the original author or authors.
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
package icu.easyj.maven.plugin.mojo.file;

/**
 * 文件信息
 */
public class FileInfo {

	/**
	 * 文件相对路径
	 */
	private String path;

	/**
	 * 操作类型
	 */
	private String action;

	/**
	 * 文件内容
	 */
	private String content;

	/**
	 * 当前配置是否生效
	 */
	private Boolean enabled;


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}


	@Override
	public String toString() {
		return "FileInfo{" +
				"path='" + path + '\'' +
				", action='" + action + '\'' +
				", content='" + content + '\'' +
				", enabled=" + enabled +
				'}';
	}
}
