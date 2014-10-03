package net.jr.ajp.client;

import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

import net.jr.ajp.client.impl.Conversation;
import net.jr.ajp.client.impl.ForwardImpl;
import net.jr.ajp.client.pool.ChannelCallback;

/**
 * forwards a request to a servlet container
 * 
 * @author jrialland
 * 
 */
public class Forward {

	private final Conversation impl;

	/**
	 * runs the conversation on the given channel
	 * 
	 * @param channel
	 *            socket channel connected to a servlet container
	 * @return true when the container allow use to reuse the same socket for the next conversation
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
	 * @return true when the container allow use to reuse the same socket for the next conversation
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
