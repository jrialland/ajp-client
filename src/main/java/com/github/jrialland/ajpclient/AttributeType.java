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
public enum AttributeType {

  // @formatter:off
	CONTEXT(0x01), // unused
	SERVLET_PATH(0x02), // unused
	REMOTE_USER(0x03),
	AUTH_TYPE(0x04),
	QUERY_STRING(0x05),
	ROUTE(0x06),
	SSL_CERT(0x07),
	SSL_CIPHER(0x08),
	SSL_SESSION(0x09),
	REQ_ATTRIBUTE(0x0A),
  SSL_KEY_SIZE(0x0B),
  SECRET(0x0C),
	STORED_METHOD(0x0D);
	// @formatter:on

	private final int code;

	AttributeType(final int code) {
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
		final AttributeType attr = AttributeType.valueOf(key);
		return attr == null ? null : attr.getCode();
	}
}
