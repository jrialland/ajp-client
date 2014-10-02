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
package net.jr.ajp.client.impl.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import net.jr.ajp.client.ForwardResponse;

public class MockForwardResponse implements ForwardResponse {

	private int statusCode = 0;

	private String statusMessage = null;

	private Map<String, String> headers = new TreeMap<String, String>();

	private boolean reuse = false, reachedBodyBegin = false, reachedBodyEnd = false;

	private Exception exception = null;

	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Override
	public void setStatus(final int code, final String message) {
		statusCode = code;
		statusMessage = message;
	}

	@Override
	public void setHeader(final String headerName, final String value) {
		headers.put(headerName, value);
	}

	@Override
	public void atResponseBodyBegin() {
		reachedBodyBegin = true;
		out = new ByteArrayOutputStream();
	}

	@Override
	public void atResponseBodyEnd(final boolean reuse) {
		this.reuse = reuse;
		reachedBodyEnd = true;
	}

	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public void onException(final Exception e) throws Exception {
		exception = e;
	}

	public String getResponseBodyAsString() {
		return new String(getResponseBodyAsBytes());
	}

	public byte[] getResponseBodyAsBytes() {
		return out.toByteArray();
	}

	public InputStream getResponseBodyAsStream() {
		return new ByteArrayInputStream(getResponseBodyAsBytes());
	}

	public void setException(final Exception exception) {
		this.exception = exception;
	}

	public void setHeaders(final Map<String, String> headers) {
		this.headers = headers;
	}

	public void setOut(final ByteArrayOutputStream out) {
		this.out = out;
	}

	public void setReachedBodyBegin(final boolean reachedBodyBegin) {
		this.reachedBodyBegin = reachedBodyBegin;
	}

	public void setReachedBodyEnd(final boolean reachedBodyEnd) {
		this.reachedBodyEnd = reachedBodyEnd;
	}

	public void setReuse(final boolean reuse) {
		this.reuse = reuse;
	}

	public Exception getException() {
		return exception;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(final int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(final String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public boolean isReuse() {
		return reuse;
	}

	public boolean isReachedBodyBegin() {
		return reachedBodyBegin;
	}

	public boolean isReachedBodyEnd() {
		return reachedBodyEnd;
	}
}
