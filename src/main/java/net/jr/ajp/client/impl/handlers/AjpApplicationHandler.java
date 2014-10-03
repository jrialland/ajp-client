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
