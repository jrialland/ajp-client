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
 * 
 */
package com.github.jrialland.ajpclient.impl;

import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

import com.github.jrialland.ajpclient.Constants;
import com.github.jrialland.ajpclient.pool.Buffers;
import com.github.jrialland.ajpclient.pool.ChannelCallback;

/**
 * Cping conversation : the client sends a cping message, and the server shall
 * respond with a cpong.
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public class CPingImpl extends Conversation implements ChannelCallback, Constants {

	private final long timeout;

	private final TimeUnit unit;

	public CPingImpl(final long timeout, final TimeUnit unit) {
		this.timeout = timeout;
		this.unit = unit;
	}

	@Override
	public void handleCPongMessage() {
		getSemaphore().release();
	}

	@Override
	public boolean __doWithChannel(final Channel channel) throws Exception {
		// send the cping message
		channel.writeAndFlush(Buffers.wrap(CPING_MESSAGE, 0, CPING_MESSAGE.length));
		getLog().debug("Sent : CPING (" + PREFIX_CPING + "), payload size = 1 bytes");

		// wait for the cpong message to unblock us
		return getSemaphore().tryAcquire(timeout, unit);
	}
}
