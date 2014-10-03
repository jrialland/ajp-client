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
package net.jr.ajp.client.impl;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

import net.jr.ajp.client.Constants;
import net.jr.ajp.client.impl.messages.CPongMessage;
import net.jr.ajp.client.pool.ChannelCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cping conversation : the client sends a cping message, and the server shall respond with a cpong.
 * 
 * @author jrialland
 * 
 */
public class CPingImpl extends Conversation implements ChannelCallback, Constants {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPingImpl.class);

	private static final Logger getLog() {
		return LOGGER;
	}

	private final long timeout;

	private final TimeUnit unit;

	public CPingImpl(final long timeout, final TimeUnit unit) {
		this.timeout = timeout;
		this.unit = unit;
	}

	@Override
	public void handleCPongMessage(final CPongMessage cPongMessage) {
		getSemaphore().release();
	}

	@Override
	public boolean __doWithChannel(final Channel channel) throws Exception {

		// send the cping message
		channel.writeAndFlush(Unpooled.wrappedBuffer(CPING_MESSAGE));
		getLog().debug("Sent : CPING (" + PREFIX_CPING + "), payload size = 1 bytes");

		// wait for the cpong message to unblock us
		return getSemaphore().tryAcquire(timeout, unit);
	}
}
