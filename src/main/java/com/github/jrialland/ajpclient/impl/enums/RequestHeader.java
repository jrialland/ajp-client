/* Copyright (c) 2014-2016 Julien Rialland <julien.rialland@gmail.com>
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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * common request headers that are recognized by the protocol. these header
 * names are turned into numerical values when sending request
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public enum RequestHeader {

	// @formatter:off
	Accept("accept", 0xA001), AcceptCharset("accept-charset", 0xA002), AcceptEncoding("accept-encoding", 0xA003), AcceptLanguage(
			"accept-language", 0xA004), Authorization("authorization", 0xA005), Connection("connection", 0xA006), ContentType(
			"content-type", 0xA007), ContentLength("content-length", 0xA008), Cookie("cookie", 0xA009), Cookie2("cookie2", 0xA00A), Host(
			"host", 0xA00B), Pragma("pragma", 0xA00C), Referer("referer", 0xA00D), UserAgent("user-agent", 0xA00E);
	// @formatter:on

	private final String name;

	private final int code;

	private RequestHeader(final String name, final int code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}

	private static Map<String, Integer> codes = new TreeMap<String, Integer>();

	static {

		for (final RequestHeader rh : RequestHeader.values()) {
			codes.put(rh.getName(), rh.getCode());
		}
		codes = Collections.unmodifiableMap(codes);
	}

	public static Integer getKeyCode(final String key) {
		return codes.get(key.toLowerCase());
	}
}
