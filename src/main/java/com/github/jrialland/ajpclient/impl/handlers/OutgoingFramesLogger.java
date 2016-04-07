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
package com.github.jrialland.ajpclient.impl.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * for debug purposes, logs in hexadecimal all the frames that we emit.
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public class OutgoingFramesLogger extends ChannelOutboundHandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutgoingFramesLogger.class);

	private static final Logger getLog() {
		return LOGGER;
	}

	@Override
	public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {

		if (msg == null) {
			getLog().debug(">>> (null)");
		} else if (msg instanceof ByteBuf) {

			final ByteBuf buf = (ByteBuf) msg;
			buf.markReaderIndex();

			final byte[] data = new byte[buf.readableBytes()];
			buf.readBytes(data);
			getLog().debug(">>> " + bytesToHex(data));

			buf.resetReaderIndex();

		} else {
			getLog().debug(">>> " + msg.toString());
		}

		// pass to the next handler in the chain
		super.write(ctx, msg, promise);
	}

	private static final String bytesToHex(final byte[] data) {
		return DatatypeConverter.printHexBinary(data);
	}

}
