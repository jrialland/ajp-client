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
package net.jr.ajp.client.impl.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.jr.ajp.client.Constants;
import net.jr.ajp.client.impl.enums.ResponseHeader;
import net.jr.ajp.client.impl.messages.CPongMessage;
import net.jr.ajp.client.impl.messages.EndResponseMessage;
import net.jr.ajp.client.impl.messages.GetBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendHeadersMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * reads a channel, and interprets incoming messages as ajp13 messages
 * 
 * @author jrialland
 * 
 */
public class AjpMessagesHandler extends ReplayingDecoder<Void> implements Constants {

	private static final Logger LOGGER = LoggerFactory.getLogger(AjpMessagesHandler.class);

	private static final Logger getLog() {
		return LOGGER;
	}

	private Long expectedBytes = null;

	private AjpMessagesHandlerCallback callback;

	public void setCallback(AjpMessagesHandlerCallback callback) {
		this.callback = callback;
	}

	private static enum MessageType {
		SendBodyChunk(PREFIX_SEND_BODY_CHUNK), SendHeaders(PREFIX_SEND_HEADERS), EndResponse(PREFIX_END_RESPONSE), GetBodyChunk(PREFIX_GET_BODY_CHUNK), CPong(PREFIX_CPONG);

		private final int code;

		private static Map<Integer, MessageType> byCodes = new TreeMap<Integer, MessageType>();

		static {
			for (final MessageType m : values()) {
				byCodes.put(m.code, m);
			}
		}

		private static MessageType forPrefix(final int code) {
			return byCodes.get(code);
		}

		MessageType(final int code) {
			this.code = code;
		}
	};

	@Override
	protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> _out) throws Exception {

		// read magic bytes
		for (final byte element : CONTAINER_MAGIC) {
			final byte b = in.readByte();
			if (b != element) {
				final String hex = "0" + Integer.toHexString(b);
				getLog().warn("skipping unexpected byte 0x" + hex.substring(hex.length() - 2));
				return;
			}
		}

		// read data length
		final int length = in.readUnsignedShort();

		// read message type prefix
		final int prefix = in.readUnsignedByte();
		final MessageType msgType = MessageType.forPrefix(prefix);
		if (msgType == null) {
			throw new IllegalStateException("unknown message prefix code : " + prefix);
		} else if (getLog().isDebugEnabled()) {
			final String type = MessageType.forPrefix(prefix).name().toUpperCase();
			getLog().debug(String.format("Received : %s (%s), payload size = %s bytes", type, prefix, length));
		}

		// CPONG
		if (prefix == PREFIX_CPONG) {
			callback.handleCPongMessage(new CPongMessage());
			return;
		}

		// SEND_HEADERS
		else if (prefix == PREFIX_SEND_HEADERS) {
			final SendHeadersMessage sbc = readHeaders(in);
			// store response status and content length;
			expectedBytes = sbc.getContentLength();
			callback.handleSendHeadersMessage(sbc);
			return;
		}

		// SEND_BODY_CHUNK
		else if (prefix == PREFIX_SEND_BODY_CHUNK) {
			final int chunkLength = in.readUnsignedShort();
			if (chunkLength > 0) {
				callback.handleSendBodyChunkMessage(new SendBodyChunkMessage(in.readBytes(chunkLength)));

				// update expected bytes counter

				if (expectedBytes != null) {
					expectedBytes -= chunkLength;
				}
			}

			// consume an extra byte, as it seems that there is always a useless
			// 0x00 following data in these packets
			in.readByte();
			return;
		}

		// END_RESPONSE
		else if (prefix == PREFIX_END_RESPONSE) {
			final boolean reuse = in.readBoolean();
			final EndResponseMessage err = new EndResponseMessage(reuse);
			callback.handleEndResponseMessage(err);
			return;
		}

		// GET_BODY_CHUNK
		else if (prefix == PREFIX_GET_BODY_CHUNK) {
			final int requestedLength = in.readUnsignedShort();
			final GetBodyChunkMessage gbr = new GetBodyChunkMessage(requestedLength);
			callback.handleGetBodyChunkMessage(gbr);
			return;
		}
	}

	protected SendHeadersMessage readHeaders(final ByteBuf in) {
		final int statusCode = in.readUnsignedShort();
		final String statusMessage = readString(in);
		final int numHeaders = in.readUnsignedShort();

		if (getLog().isDebugEnabled()) {
			getLog().debug(" | HTTP/1.1 " + statusCode + " " + statusMessage);
		}

		final SendHeadersMessage sbc = new SendHeadersMessage(statusCode, statusMessage);

		for (int i = 0; i < numHeaders; i++) {
			in.markReaderIndex();
			final int code = in.readUnsignedShort();
			String headerName = ResponseHeader.getHeader(code);
			if (headerName == null) {
				in.resetReaderIndex();
				headerName = readString(in);
			}
			final String value = readString(in);
			if (getLog().isDebugEnabled()) {
				getLog().debug(" | " + headerName + ": " + value);
			}
			sbc.addHeader(headerName, value);
		}

		return sbc;
	}

	protected String readString(final ByteBuf in) {
		in.markReaderIndex();
		final int b0 = in.readUnsignedByte();
		if (b0 == 0xff) {
			return null;
		}
		in.resetReaderIndex();
		final int length = in.readUnsignedShort();
		final byte[] data = new byte[length];
		in.readBytes(data);

		// skip trailing \0
		in.readByte();

		return new String(data);
	}
}
