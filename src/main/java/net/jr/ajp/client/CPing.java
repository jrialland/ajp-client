package net.jr.ajp.client;

import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

import net.jr.ajp.client.impl.CPingImpl;
import net.jr.ajp.client.impl.Conversation;
import net.jr.ajp.client.pool.ChannelCallback;

/**
 * Cping conversation : the client sends a cping message, and the server shall respond with a cpong.
 * 
 * 
 * 
 * @author jrialland
 * 
 */
public class CPing {

	private final Conversation impl;

	/**
	 * runs the conversation on the given channel
	 * 
	 * @param channel
	 *            socket channel connected to a servlet container
	 * @return true when the container has answered a CPONG
	 * @throws Exception
	 */
	public boolean execute(final Channel channel) throws Exception {
		return impl.execute(channel);
	}

	/**
	 * @see CPing#execute(Channel)
	 * 
	 * @param host
	 *            tcp host
	 * @param port
	 *            tcp port
	 * @return true when the container has answered a CPONG
	 * @throws Exception
	 */
	public boolean execute(final String host, final int port) throws Exception {
		return impl.execute(host, port);
	}

	CPing(final long timeout, final TimeUnit unit) {
		impl = new CPingImpl(timeout, unit);
	}

	public ChannelCallback impl() {
		return impl;
	}
}
