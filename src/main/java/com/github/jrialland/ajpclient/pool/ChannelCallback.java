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
package com.github.jrialland.ajpclient.pool;

import io.netty.channel.Channel;

public interface ChannelCallback {

	/**
	 * Called before doWithChannel in order to clean/configure it (like
	 * modifiying pipeline, etc..)
	 */
	void beforeUse(Channel channel);

	/**
	 *
	 * @param channel
	 * @return true when the channel should be reused.
	 */
	boolean __doWithChannel(Channel channel) throws Exception;

	/**
	 * Called before the channel is returned to the pool. one may want to clean
	 * the channel (remove pipeline, etc)
	 *
	 * @param channel
	 */
	void beforeRelease(Channel channel);
}
