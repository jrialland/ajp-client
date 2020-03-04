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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

import java.util.Collection;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrialland.ajpclient.Header;
import com.github.jrialland.ajpclient.impl.handlers.AjpMessagesHandler;
import com.github.jrialland.ajpclient.impl.handlers.AjpMessagesHandlerCallback;
import com.github.jrialland.ajpclient.impl.handlers.OutgoingFramesLogger;
import com.github.jrialland.ajpclient.pool.ChannelCallback;
import com.github.jrialland.ajpclient.pool.Channels;

public abstract class Conversation implements ChannelCallback, AjpMessagesHandlerCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(Conversation.class);

	public static Logger getLog() {
		return LOGGER;
	}

	private Semaphore semaphore;

	private Channel currentChannel;

	@Override
	public void beforeUse(final Channel channel) {
		channel.attr(AjpMessagesHandlerCallback.CHANNEL_ATTR).set(this);
		semaphore = new Semaphore(0);
		currentChannel = channel;
	}

	protected Semaphore getSemaphore() {
		return semaphore;
	}

	protected Channel getCurrentChannel() {
		return currentChannel;
	}

	@Override
	public void beforeRelease(final Channel channel) {
		channel.attr(AjpMessagesHandlerCallback.CHANNEL_ATTR).set(null);
	}

	public boolean execute(final Channel channel) throws Exception {
		final ChannelPipeline pipeline = channel.pipeline();
		if (getLog().isTraceEnabled() && pipeline.get(OutgoingFramesLogger.class) == null) {
			pipeline.addLast(new OutgoingFramesLogger());
		}
		if (pipeline.get(AjpMessagesHandler.class) == null) {
			pipeline.addLast(new AjpMessagesHandler());
		}
		beforeUse(channel);
		final boolean r = __doWithChannel(channel);
		beforeRelease(channel);
		return r;
	}

	public boolean execute(final String host, final int port) throws Exception {
		final Channel channel = Channels.connect(host, port);
		try {
			return execute(channel);
		} finally {
			channel.close();
		}
	}

	@Override
	public boolean __doWithChannel(final Channel channel) throws Exception {
		throw new UnsupportedOperationException("__doWithChannel() is not implemented.");
	}

	@Override
	public void handleCPongMessage() throws Exception {
		throw new UnsupportedOperationException("handleCPongMessage() is not implemented.");

	}

	@Override
	public void handleEndResponseMessage(final boolean reuse) throws Exception {
		throw new UnsupportedOperationException("handleEndResponseMessage() is not implemented.");
	}

	@Override
	public void handleGetBodyChunkMessage(final int requestedLength) throws Exception {
		throw new UnsupportedOperationException("handleGetBodyChunkMessage() is not implemented.");

	}

	@Override
	public void handleSendBodyChunkMessage(final ByteBuf data) throws Exception {
		throw new UnsupportedOperationException("handleSendBodyChunkMessage() is not implemented.");

	}

	@Override
	public void handleSendHeadersMessage(final int statusCode, final String statusMessage, final Collection<Header> headers)
			throws Exception {
		throw new UnsupportedOperationException("handleSendHeadersMessage() is not implemented.");
	}
}
