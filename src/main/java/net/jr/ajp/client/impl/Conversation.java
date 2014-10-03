package net.jr.ajp.client.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

import java.util.concurrent.Semaphore;

import net.jr.ajp.client.impl.handlers.AjpApplicationHandler;
import net.jr.ajp.client.impl.handlers.AjpApplicationHandlerCallback;
import net.jr.ajp.client.impl.handlers.Initializer;
import net.jr.ajp.client.impl.messages.CPongMessage;
import net.jr.ajp.client.impl.messages.EndResponseMessage;
import net.jr.ajp.client.impl.messages.GetBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendHeadersMessage;
import net.jr.ajp.client.pool.ChannelCallback;
import net.jr.ajp.client.pool.Channels;

public abstract class Conversation implements ChannelCallback, AjpApplicationHandlerCallback {

	private Semaphore semaphore;

	private Channel currentChannel;

	@Override
	public void beforeUse(final Channel channel) {
		channel.pipeline().get(AjpApplicationHandler.class).setCallback(this);
		semaphore = new Semaphore(0);
		currentChannel = channel;
	}

	protected void reset() {

	}

	protected Semaphore getSemaphore() {
		return semaphore;
	}

	protected Channel getCurrentChannel() {
		return currentChannel;
	}

	@Override
	public void beforeRelease(final Channel channel) {
		channel.pipeline().get(AjpApplicationHandler.class).setCallback(null);
	}

	@Override
	public boolean __doWithChannel(final Channel channel) throws Exception {
		throw new UnsupportedOperationException("doWithChannel() is not implemented.");

	}

	@Override
	public void handleCPongMessage(final CPongMessage cPongMessage) throws Exception {
		throw new UnsupportedOperationException("handleCPongMessage() is not implemented.");

	}

	@Override
	public void handleEndResponseMessage(final EndResponseMessage endResponseMessage) throws Exception {
		throw new UnsupportedOperationException("handleEndResponseMessage() is not implemented.");

	}

	@Override
	public void handleGetBodyChunkMessage(final GetBodyChunkMessage getBodyChunkMessage) throws Exception {
		throw new UnsupportedOperationException("handleGetBodyChunkMessage() is not implemented.");

	}

	@Override
	public void handleSendBodyChunkMessage(final SendBodyChunkMessage sendBodyChunkMessage) throws Exception {
		throw new UnsupportedOperationException("handleSendBodyChunkMessage() is not implemented.");

	}

	@Override
	public void handleSendHeadersMessage(final SendHeadersMessage sendHeadersMessage) throws Exception {
		throw new UnsupportedOperationException("handleSendHeadersMessage() is not implemented.");
	}

	public boolean execute(final Channel channel) throws Exception {

		final ChannelPipeline pipeline = channel.pipeline();
		if (pipeline.get(AjpApplicationHandler.class) == null) {
			pipeline.addFirst(new Initializer());
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
}
