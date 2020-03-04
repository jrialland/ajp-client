/* Copyright (c) 2014-2020 Julien Rialland <julien.rialland@gmail.com>
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
package com.github.jrialland.ajpclient.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jrialland.ajpclient.Attribute;
import com.github.jrialland.ajpclient.Constants;
import com.github.jrialland.ajpclient.ForwardRequest;
import com.github.jrialland.ajpclient.ForwardResponse;
import com.github.jrialland.ajpclient.Header;
import com.github.jrialland.ajpclient.impl.enums.RequestHeader;
import com.github.jrialland.ajpclient.impl.enums.RequestMethod;
import com.github.jrialland.ajpclient.pool.Buffers;
import com.github.jrialland.ajpclient.pool.ChannelCallback;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * Forward conversion : the client forwards an http request to server.
 *
 * @see http://tomcat.apache.org/connectors-doc/ajp/ajpv13a.html
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public class ForwardImpl extends Conversation implements ChannelCallback, Constants {

	private final ForwardRequest request;

	private final ForwardResponse response;

	private boolean shouldReuse = false;

	private long timeout;

	private TimeUnit unit = null;

	public ForwardImpl(final ForwardRequest request, final ForwardResponse response) {
		this.request = request;
		this.response = response;
	}

	public ForwardImpl(final ForwardRequest request, final ForwardResponse response, final long timeout, final TimeUnit unit) {
		this(request, response);
		this.timeout = timeout;
		this.unit = unit;
	}

	@Override
	public void beforeRelease(final Channel channel) {
		shouldReuse = false;
		super.beforeRelease(channel);
	}

	@Override
	public boolean __doWithChannel(final Channel channel) throws Exception {
		shouldReuse = false;
		checkRequest(request);
		sendRequest(channel, request);
		if (unit == null) {
			getLog().debug("START ACQUIRE");
			getSemaphore().acquire();
			getLog().debug("END ACQUIRE");
		} else if (!getSemaphore().tryAcquire(timeout, unit)) {
			throw new TimeoutException("time limit exceeded");
		}
		response.atResponseBodyEnd(shouldReuse);
		return shouldReuse;
	}

	protected static void checkRequest(final ForwardRequest request) {

		String contentLength = null;
		String transferEncoding = null;
		for (final Header h : request.getHeaders()) {
			if (h.getKey().equalsIgnoreCase("Content-Length")) {
				contentLength = h.getValue();
			} else if (h.getKey().equalsIgnoreCase("Transfer-Encoding")) {
				transferEncoding = h.getValue();
			}
		}

		if (request.getMethod().equals(RequestMethod.POST)) {
			if (contentLength == null) {
				if (transferEncoding == null || !transferEncoding.equals("chunked")) {
					getLog().warn("POST request without a Content-Length");
				}
			} else if (!contentLength.matches("[0-9]+$")) {
				throw new IllegalArgumentException("Content-Length header is not a valid number");
			}
		}
	}

	protected static void sendRequest(final Channel channel, final ForwardRequest request) throws IOException {

		// start by writing message payload, header will be appended afterwards
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(MAX_MESSAGE_SIZE);
		final DataOutputStream tmp = new DataOutputStream(baos);

		// request type
		tmp.writeByte(PREFIX_FORWARD_REQUEST);

		// payload
		tmp.writeByte(request.getMethod().getCode());
		writeString(request.getProtocol(), tmp);
		writeString(request.getRequestUri(), tmp);
		writeString(request.getRemoteAddress(), tmp);
		writeString(request.getRemoteHost(), tmp);
		writeString(request.getServerName(), tmp);
		tmp.writeShort(request.getServerPort());
		tmp.writeBoolean(request.isSsl());
		tmp.writeShort(request.getHeaders().size());

		// headers
		for (final Header header : request.getHeaders()) {
			final Integer code = RequestHeader.getKeyCode(header.getKey());
			if (code == null) {
				writeString(header.getKey(), tmp);
			} else {
				tmp.writeShort(code);
			}
			writeString(header.getValue(), tmp);
		}

		// attributes
		for (final Attribute attr : request.getAttributes()) {
			tmp.writeByte(attr.getType().getCode());
			for(String value: attr.getValue()) {
				writeString(value, tmp);
			}
		}

		// request terminator
		tmp.write(REQUEST_TERMINATOR);
		tmp.flush();

		// now prepare the whole message
		final byte[] data = baos.toByteArray();

		if (data.length + 4 > MAX_MESSAGE_SIZE) {
			throw new IllegalArgumentException("Message size is larger than " + MAX_MESSAGE_SIZE + " bytes.");
		}

		final ByteBuf buf = Buffers.makeBuffer(4 + data.length);
		buf.writeBytes(CLIENT_MAGIC);
		buf.writeShort(data.length);
		buf.writeBytes(data);

		channel.writeAndFlush(buf);
		getLog().debug("Sent : FORWARDREQUEST (" + PREFIX_FORWARD_REQUEST + "), payload size = " + data.length + " bytes");

		final InputStream requestBody = request.getRequestBody();
		if (requestBody != null) {
			sendChunk(true, requestBody, MAX_SEND_CHUNK_SIZE, channel);
		}
	}

	protected static void sendChunk(final boolean firstChunk, final InputStream in, final int length, final Channel channel) throws IOException {

		final byte[] buf = new byte[MAX_SEND_CHUNK_SIZE + 6];

		// 2 first bytes : magic signature
		buf[0] = CLIENT_MAGIC[0];
		buf[1] = CLIENT_MAGIC[1];

		// compute the actual amount of bytes that we can send
		int actual = 0;
		if (in != null) {
			try {
				actual = Math.max(0, in.read(buf, 6, length));
			} catch (final EOFException e) {
				// 'actual' will be set to zero in this case
			} catch (final IndexOutOfBoundsException e) {
				throw new IOException(String.format("could not read buffer (bufLen=%s, offset=%s length=%s)", buf.length, 6, length));
			}
		}

		// do not send anything when this is the first chunk and there is nothing
		// to send
		if (firstChunk && actual == 0) {
			return;
		}

		// total packet length
		buf[2] = (byte) (actual + 2 >> 8);
		buf[3] = (byte) (actual + 2 & 0xff);

		// length of the data block
		buf[4] = (byte) (actual >> 8);
		buf[5] = (byte) (actual & 0xff);

		// write to channel
		channel.writeAndFlush(Buffers.wrap(buf, 0, actual + 6));
		getLog().debug("Sent : REQUESTBODYCHUNK (n/a), payload size = " + (actual + 2) + " bytes");
	}

	/**
	 * A variable-sized string (length bounded by 2^16). Encoded with the length
	 * packed into two bytes first, followed by the string (including the
	 * terminating '\0'). Note that the encoded length does not include the
	 * trailing '\0' -- it is like strlen. This is a touch confusing on the Java
	 * side, which is littered with odd autoincrement statements to skip over
	 * these terminators. I believe the reason this was done was to allow the C
	 * code to be extra efficient when reading strings which the servlet
	 * container is sending back -- with the terminating \0 character, the C
	 * code can pass around references into a single buffer, without copying. If
	 * the \0 was missing, the C code would have to copy things out in order to
	 * get its notion of a string. Note a size of -1 (65535) indicates a null
	 * string and no data follow the length in this case.
	 *
	 * @param s
	 * @param d
	 * @throws IOException
	 */
	protected static void writeString(final String s, final DataOutputStream d) throws IOException {
		if (s == null) {
			d.writeShort(-1);
		} else {
			d.writeShort(s.length());
			d.write(s.getBytes());
			d.writeByte(0);
		}
	}

	@Override
	public void handleSendHeadersMessage(final int statusCode, final String statusMessage, final Collection<Header> headers) throws Exception {
		response.setStatus(statusCode, statusMessage);
		for (final Header h : headers) {
			response.setHeader(h.getKey(), h.getValue());
		}
		response.atResponseBodyBegin();
	}

	@Override
	public void handleSendBodyChunkMessage(final ByteBuf data) throws Exception {
		data.readBytes(response.getOutputStream(), data.readableBytes());
	}

	@Override
	public void handleGetBodyChunkMessage(final int requestedLength) throws Exception {
		sendChunk(false, request.getRequestBody(), Math.min(MAX_SEND_CHUNK_SIZE, Math.max(0, requestedLength)), getCurrentChannel());
	}

	@Override
	public void handleEndResponseMessage(final boolean reuse) throws Exception {
		shouldReuse = reuse;
		getSemaphore().release();
	}
}
