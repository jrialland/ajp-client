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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.jr.ajp.client.Attribute;
import net.jr.ajp.client.ForwardRequest;
import net.jr.ajp.client.Header;

public class MockForwardRequest implements ForwardRequest {

	private String method = "GET";

	private String protocol = "HTTP/1.1";

	private String requestUri = "/";

	private String remoteAddress = "127.0.0.1";

	private String remoteHost = "localhost";

	private String serverName = "localhost";

	boolean ssl = false;

	private int serverPort = 80;

	List<Header> headers = new ArrayList<Header>();

	private Map<Attribute, String> attributes = new TreeMap<Attribute, String>();

	private InputStream requestBody;

	@Override
	public String getMethod() {
		return method;
	}

	public void setMethod(final String method) {
		this.method = method;
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(final String protocol) {
		this.protocol = protocol;
	}

	@Override
	public String getRequestUri() {
		return requestUri;
	}

	public void setRequestUri(final String requestUri) {
		this.requestUri = requestUri;
	}

	@Override
	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(final String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	public void setServerName(final String serverName) {
		this.serverName = serverName;
	}

	@Override
	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(final int serverPort) {
		this.serverPort = serverPort;
	}

	@Override
	public Collection<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(final List<Header> headers) {
		this.headers = headers;
	}

	@Override
	public Map<Attribute, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(final Map<Attribute, String> attributes) {
		this.attributes = attributes;
	}

	@Override
	public InputStream getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(final InputStream requestBody) {
		this.requestBody = requestBody;
	}

	public void setRequestBody(final byte[] data) {
		requestBody = new ByteArrayInputStream(data);
		addHeader("Content-Length", Long.toString(data.length));
	}

	public void setRequestBody(final String str) {
		setRequestBody(str.getBytes());
	}

	@Override
	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(final String remoteHost) {
		this.remoteHost = remoteHost;
	}

	@Override
	public String getHeader(String header) {
		header = header.toLowerCase().trim();
		for (final Header h : headers) {
			if (h.getKey().equalsIgnoreCase(header)) {
				return h.getValue();
			}
		}
		return null;
	}

	public void addHeader(final String headerName, final String value) {
		getHeaders().add(new Header(headerName, value));
	}

	public void setSsl(final boolean ssl) {
		this.ssl = ssl;
	}

	@Override
	public boolean isSsl() {
		// TODO Auto-generated method stub
		return false;
	}
}
