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
package com.github.jrialland.ajpclient.impl.enums;

/**
 * numeric codes for http request method.
 */
public enum RequestMethod {

	// @formatter:off
	OPTIONS(1), GET(2), HEAD(3), POST(4), PUT(5), DELETE(6), TRACE(7), PROPFIND(8), PROPPATCH(9), MKCOL(10), COPY(11), MOVE(12), LOCK(13), UNLOCK(
			14), ACL(15), REPORT(16), VERSION_CONTROL(17), CHECKIN(18), CHECKOUT(19), UNCHECKOUT(20), SEARCH(21), MKWORKSPACE(22), UPDATE(
					23), LABEL(24), MERGE(25), BASELINE_CONTROL(26), MKACTIVITY(27), JK_STORED(0xff);
	// @formatter:on

	private final int code;

	RequestMethod(final int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static final RequestMethod forMethod(final String method) {
		try {
			final RequestMethod m = RequestMethod.valueOf(method.replace('-', '_'));
			return m;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
}
