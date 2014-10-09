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
package com.github.jrialland.ajpclient;

import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

import com.github.jrialland.ajpclient.impl.Conversation;
import com.github.jrialland.ajpclient.impl.ForwardImpl;
import com.github.jrialland.ajpclient.pool.ChannelCallback;

/**
 * forwards a request to a servlet container
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public class Forward {

	private final Conversation impl;

	/**
	 * runs the conversation on the given channel
	 *
	 * @param channel
	 *            socket channel connected to a servlet container
	 * @return true when the container allow use to reuse the same socket for
	 *         the next conversation
	 * @throws Exception
	 */
	public boolean execute(final Channel channel) throws Exception {
		return impl.execute(channel);
	}

	/**
	 * @see Forward#execute(Channel)
	 *
	 * @param host
	 *            tcp host
	 * @param port
	 *            tcp port
	 * @return true when the container allow use to reuse the same socket for
	 *         the next conversation
	 * @throws Exception
	 */
	public boolean execute(final String host, final int port) throws Exception {
		return impl.execute(host, port);
	}

	public Forward(final ForwardRequest request, final ForwardResponse response) {
		impl = new ForwardImpl(request, response);
	}

	public Forward(final ForwardRequest request, final ForwardResponse response, final long timeout, final TimeUnit unit) {
		impl = new ForwardImpl(request, response, timeout, unit);
	}

	public ChannelCallback impl() {
		return impl;
	}
}
