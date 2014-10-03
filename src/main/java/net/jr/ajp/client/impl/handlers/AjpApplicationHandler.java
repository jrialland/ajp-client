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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jr.ajp.client.impl.messages.CPongMessage;
import net.jr.ajp.client.impl.messages.EndResponseMessage;
import net.jr.ajp.client.impl.messages.GetBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendHeadersMessage;

public class AjpApplicationHandler extends ChannelInboundHandlerAdapter {

	private AjpApplicationHandlerCallback callback;

	public synchronized void setCallback(final AjpApplicationHandlerCallback callback) {
		this.callback = callback;
	}

	protected synchronized AjpApplicationHandlerCallback getCallback() {
		return callback;
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		final AjpApplicationHandlerCallback fCallback = getCallback();
		if (fCallback != null) {
			if (msg instanceof CPongMessage) {
				fCallback.handleCPongMessage((CPongMessage) msg);
			}

			else if (msg instanceof EndResponseMessage) {
				fCallback.handleEndResponseMessage((EndResponseMessage) msg);
			}

			else if (msg instanceof GetBodyChunkMessage) {
				fCallback.handleGetBodyChunkMessage((GetBodyChunkMessage) msg);
			}

			else if (msg instanceof SendBodyChunkMessage) {
				fCallback.handleSendBodyChunkMessage((SendBodyChunkMessage) msg);
			}

			else if (msg instanceof SendHeadersMessage) {
				fCallback.handleSendHeadersMessage((SendHeadersMessage) msg);
			}

			else {
				throw new IllegalArgumentException("unknown message type " + msg);
			}
		}
	}
}
