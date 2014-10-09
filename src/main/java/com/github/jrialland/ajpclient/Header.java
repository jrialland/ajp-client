/* Copyright (c) 2014 Julien Rialland <julien.rialland@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jrialland.ajpclient;

/**
 * Just store the key/value pair that forms an http header.
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public class Header {

	private final String key;

	private final String value;

	public Header(final String key, final String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return key.hashCode() + value.hashCode();
	}

	@Override
	public String toString() {
		return String.format("%s: %s", getKey(), getValue());
	}

}
