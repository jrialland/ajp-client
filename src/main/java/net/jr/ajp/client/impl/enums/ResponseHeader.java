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
package net.jr.ajp.client.impl.enums;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * common response headers are associated with numerical codes, too
 * 
 * @author jrialland
 * 
 */
public enum ResponseHeader {

	//@formatter:off
		ContentType("Content-Type",	0xA001),
		ContentLanguage("Content-Language",	0xA002),
		ContentLength("Content-Length",	0xA003),
		Date("Date",	0xA004),
		LastModified("Last-Modified",	0xA005),
		Location("Location",	0xA006),
		SetCookie("Set-Cookie",	0xA007),
		SetCookie2("Set-Cookie2",	0xA008),
		ServletEngine("Servlet-Engine",	0xA009),
		Status("Status",	0xA00A),
		WWWAuthenticate("WWW-Authenticate",	0xA00B);
		//@formatter:on

	private final String key;

	private final int code;

	private ResponseHeader(final String key, final int code) {
		this.key = key;
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public String getKey() {
		return key;
	}

	private static Map<Integer, String> headersByCode = new TreeMap<Integer, String>();

	static {
		for (final ResponseHeader rh : ResponseHeader.values()) {
			headersByCode.put(rh.getCode(), rh.getKey());
		}
		headersByCode = Collections.unmodifiableMap(headersByCode);
	}

	public static String getHeader(final int code) {
		return headersByCode.get(code);
	}

}
