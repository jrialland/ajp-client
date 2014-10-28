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
package com.github.jrialland.ajpclient.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrialland.ajpclient.Attribute;
import com.github.jrialland.ajpclient.Forward;
import com.github.jrialland.ajpclient.ForwardRequest;
import com.github.jrialland.ajpclient.ForwardResponse;
import com.github.jrialland.ajpclient.Header;
import com.github.jrialland.ajpclient.impl.enums.RequestMethod;
import com.github.jrialland.ajpclient.pool.ChannelPool;
import com.github.jrialland.ajpclient.pool.Channels;

public class AjpServletProxy {

	private static final Logger LOGGER = LoggerFactory.getLogger(AjpServletProxy.class);

	public static Logger getLog() {
		return LOGGER;
	}

	private static class RequestWrapper implements ForwardRequest {

		private RequestMethod requestMethod;

		private final HttpServletRequest request;

		private final URL requestUrl;

		private final InputStream in;

		private final List<Header> headers = new LinkedList<Header>();

		private final Map<Attribute, String> attributes = new TreeMap<Attribute, String>();

		public RequestWrapper(final HttpServletRequest request) throws IOException {

			this.request = request;
			requestUrl = new URL(request.getRequestURL().toString());
			requestMethod = RequestMethod.forMethod(request.getMethod());
			if (requestMethod == null) {
				requestMethod = RequestMethod.JK_STORED;
				attributes.put(Attribute.stored_method, request.getMethod());
			}

			boolean hasContentLength = false;
			for (final String headerName : Collections.list(request.getHeaderNames())) {
				if (headerName.equals("Content-Length")) {
					hasContentLength = true;
				}
				for (final String value : Collections.list(request.getHeaders(headerName))) {
					headers.add(new Header(headerName, value));
				}
			}

			if (!hasContentLength && request.getContentLength() > -1) {
				headers.add(new Header("Content-Length", Long.toString(request.getContentLength())));
			}

			if (request.getQueryString() != null) {
				attributes.put(Attribute.query_string, request.getQueryString());
			}

			if (request.getRemoteUser() != null) {
				attributes.put(Attribute.remote_user, request.getRemoteUser());
			}

			attributes.put(Attribute.servlet_path, request.getServletPath());

			in = new BufferedInputStream(request.getInputStream());
		}

		@Override
		public RequestMethod getMethod() {
			return requestMethod;
		}

		@Override
		public String getProtocol() {
			return request.getProtocol();
		}

		@Override
		public String getRequestUri() {
			return request.getRequestURI();
		}

		@Override
		public String getRemoteAddress() {
			return request.getRemoteAddr();
		}

		@Override
		public String getRemoteHost() {
			return request.getRemoteHost();
		}

		@Override
		public String getServerName() {
			return requestUrl.getHost();
		}

		@Override
		public int getServerPort() {
			return requestUrl.getPort();
		}

		@Override
		public Collection<Header> getHeaders() {
			return headers;
		}

		@Override
		public Map<Attribute, String> getAttributes() {
			return attributes;
		}

		@Override
		public InputStream getRequestBody() {
			return in;
		}

		@Override
		public boolean isSsl() {
			return request.isSecure();
		}
	}

	private static class ResponseWrapper implements ForwardResponse {

		private final HttpServletResponse response;

		private final ServletOutputStream out;

		public ResponseWrapper(final HttpServletResponse response) throws IOException {
			this.response = response;
			out = response.getOutputStream();
		}

		@Override
		public void atResponseBodyBegin() {
			// does nothing
		}

		@Override
		public void atResponseBodyEnd(final boolean reuse) throws IOException {
			out.flush();
		}

		@Override
		public OutputStream getOutputStream() {
			return out;
		}

		@Override
		public void setHeader(final String headerName, final String value) {
			response.setHeader(headerName, value);
		}

		@Override
		@SuppressWarnings("deprecation")
		public void setStatus(final int code, final String message) {
			response.setStatus(code, message);
		}
	}

	private final ChannelPool channelPool;

	private AjpServletProxy(final ChannelPool channelPool) {
		this.channelPool = channelPool;
	}

	public static AjpServletProxy forHost(final String host, final int port) throws ServletException {
		try {
			final ChannelPool channelPool = Channels.getPool(host, port);
			return new AjpServletProxy(channelPool);
		} catch (final Exception e) {
			throw new ServletException(e);
		}
	}

	public void forward(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		forward(request, response, true);
	}

	public void forward(final HttpServletRequest request, final HttpServletResponse response, final long timeout, final TimeUnit unit,
			final boolean reuseConnection) throws IOException, ServletException {
		final RequestWrapper ajpRequest = new RequestWrapper(request);
		final ResponseWrapper ajpResponse = new ResponseWrapper(response);
		try {
			channelPool.execute(new Forward(ajpRequest, ajpResponse, timeout, unit), reuseConnection);
		} catch (final IOException ioException) {
			throw ioException;
		} catch (final Exception e) {
			throw new ServletException(e);
		}
	}

	public void forward(final HttpServletRequest request, final HttpServletResponse response, final boolean reuseConnection)
			throws IOException, ServletException {
		forward(request, response, -1L, null, reuseConnection);
	}

}
