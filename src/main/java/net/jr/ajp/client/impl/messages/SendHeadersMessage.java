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
package net.jr.ajp.client.impl.messages;

import java.util.LinkedList;
import java.util.List;

import net.jr.ajp.client.Header;

/**
 * the servlet container sends us the response status line and headers
 * 
 * @author jrialland
 * 
 */
public class SendHeadersMessage extends AjpMessage {

	private final int statusCode;

	private final String statusMessage;

	private final List<Header> headers = new LinkedList<Header>();

	private boolean chunked = false;

	private long contentLength = -1L;

	public SendHeadersMessage(final int statusCode, final String statusMessage) {
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
	}

	public void addHeader(final String headerName, final String value) {
		if (headerName.equals("Transfer-Encoding") && value.equalsIgnoreCase("chuncked")) {
			chunked = true;
		} else if (headerName.equals("Content-Length")) {
			contentLength = Long.parseLong(value);
		}
		headers.add(new Header(headerName, value));
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public boolean isChunked() {
		return chunked;
	}

	public Long getContentLength() {
		return contentLength == -1 ? null : contentLength;
	}
}
