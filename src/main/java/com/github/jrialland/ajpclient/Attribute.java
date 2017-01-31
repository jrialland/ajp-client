/* Copyright (c) 2014-2017 Julien Rialland <julien.rialland@gmail.com>
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
 * 
 */
package com.github.jrialland.ajpclient;

/**
 * ajp13 forward request attributes. these are optional key/values that can be
 * sent with forward requests.
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public enum Attribute {

	// @formatter:off
	context(0x01), // unused
	servlet_path(0x02), // unused
	remote_user(0x03), auth_type(0x04), query_string(0x05), route(0x06), ssl_cert(0x07), ssl_cipher(0x08), ssl_session(0x09), req_attribute(
			0x0A), ssl_key_size(0x0B), secret(0x0C), stored_method(0x0D);
	// @formatter:on

	private final int code;

	Attribute(final int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	};

	/**
	 *
	 * @param key
	 *            attribute key
	 * @return the corresponding code.
	 */
	public static Integer getCode(final String key) {
		final Attribute attr = Attribute.valueOf(key);
		return attr == null ? null : attr.getCode();
	}
}
