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
package com.github.jrialland.ajpclient.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.r358.poolnetty.common.BootstrapProvider;
import org.r358.poolnetty.common.ConnectionInfo;
import org.r358.poolnetty.common.ConnectionInfoProvider;
import org.r358.poolnetty.common.ContextExceptionHandler;
import org.r358.poolnetty.common.LeaseListener;
import org.r358.poolnetty.common.LeasedChannel;
import org.r358.poolnetty.common.PoolProvider;
import org.r358.poolnetty.pool.NettyConnectionPool;
import org.r358.poolnetty.pool.NettyConnectionPoolBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrialland.ajpclient.CPing;
import com.github.jrialland.ajpclient.Forward;
import com.github.jrialland.ajpclient.jmx.ChannelPoolMonitorMBean;
import com.github.jrialland.ajpclient.jmx.impl.ChannelPoolMonitorImpl;

public class ChannelPool {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);

	private static final Logger getLog() {
		return LOGGER;
	}

	String host;

	int port;

	private NettyConnectionPool ncp;

	private final ChannelPoolMonitorImpl monitor;

	protected ChannelPool(final String host, final int port) throws Exception {
		this.host = host;
		this.port = port;
		monitor = new ChannelPoolMonitorImpl(this);
		reset();
	}

	private static NettyConnectionPool createPool(final String host, final int port, final ChannelPoolMonitorImpl monitor) {
		final Bootstrap bootstrap = Channels.newBootStrap(host, port);

		final NettyConnectionPoolBuilder ncb = new NettyConnectionPoolBuilder(0, 200, 1000);

		ncb.withBootstrapProvider(new BootstrapProvider() {

			@Override
			public Bootstrap createBootstrap(final PoolProvider pp) {
				return bootstrap;
			}
		});

		ncb.withConnectionInfoProvider(new ConnectionInfoProvider() {

			@Override
			public ConnectionInfo connectionInfo(final PoolProvider pp) {
				final InetSocketAddress remoteAddr = new InetSocketAddress(host, port);
				return new ConnectionInfo(remoteAddr, null, new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(final Channel ch) throws Exception {
						Channels.initChannel(ch);
					}
				});
			}
		});

		/**
		 * Always close on exception
		 */
		ncb.withContextExceptionHandler(new ContextExceptionHandler() {
			@Override
			public boolean close(final Throwable arg0, final PoolProvider arg1) {
				return true;
			}
		});

		final NettyConnectionPool pool = ncb.build();
		pool.addListener(monitor);
		return pool;
	}

	public void execute(final Forward forward) throws Exception {
		execute(forward.impl());
	}

	public void execute(final CPing cping) throws Exception {
		execute(cping.impl());
	}

	public void execute(final Forward forward, final boolean reuseConnection) throws Exception {
		execute(forward.impl(), reuseConnection);
	}

	public void execute(final CPing cping, final boolean reuseConnection) throws Exception {
		execute(cping.impl(), reuseConnection);
	}

	protected void execute(final ChannelCallback callback) throws Exception {
		execute(callback, true);
	}

	/**
	 * Gets the channel just as
	 * {@link NettyConnectionPool#lease(int, TimeUnit, Object)} does, but better
	 * handle exceptions when we cannot obtain a channel for some reason.
	 *
	 * @return a channel obtained from the pool
	 * @throws Exception
	 */
	protected Channel getChannel() throws Exception {
		final Throwable[] th = new Throwable[1];
		final Future<LeasedChannel> future = ncp.leaseAsync(5, TimeUnit.SECONDS, null, new LeaseListener() {

			@Override
			public void leaseRequest(final boolean success, final LeasedChannel channel, final Throwable t) {
				th[0] = t;
			}
		});
		return future.get(5, TimeUnit.SECONDS);
	}

	/**
	 * Handles channel picking/returning from/to the pool. the 3 methods
	 * {@link ChannelCallback#beforeUse(Channel)},
	 * {@link ChannelCallback#__doWithChannel(Channel)} and
	 * {@link ChannelCallback#beforeRelease(Channel)} are called in this order
	 * on the passed callback instance
	 *
	 * @param callback
	 *            a channelcallback
	 * @throws Exception
	 */
	protected void execute(final ChannelCallback callback, final boolean reuseConnection) throws Exception {
		getLog().debug("getting channel from the connection pool ...");

		final Channel channel = getChannel();

		getLog().debug("... obtained " + channel);

		boolean reuse = false;
		try {
			callback.beforeUse(channel);
			reuse = callback.__doWithChannel(channel) && reuseConnection;
			try {
				callback.beforeRelease(channel);
			} catch (final Exception e) {
				getLog().warn("while releasing channel", e);
				reuse = false;
			}

		} finally {
			if (reuse) {
				getLog().debug("returning channel " + channel + " to the connection pool");
			} else {
				getLog().debug("invalidating channel " + channel);
				channel.close();
			}
			ncp.yield(channel);
		}
	}

	protected void destroy() {
		ncp.stop(true);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public ChannelPoolMonitorMBean getMonitor() {
		return monitor;
	}

	public void reset() {
		if (ncp != null) {
			ncp.stop(true);
		}
		ncp = createPool(host, port, monitor);

		try {
			ncp.start(1000, TimeUnit.MILLISECONDS);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
