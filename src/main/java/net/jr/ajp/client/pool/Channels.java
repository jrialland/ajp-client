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
package net.jr.ajp.client.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.jr.ajp.client.impl.handlers.AjpMessagesHandler;

public final class Channels {

	private Channels() {

	}

	private static final ThreadFactory THREADFACTORY = new ThreadFactory() {

		private final ThreadFactory wrapped = Executors.defaultThreadFactory();

		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = wrapped.newThread(r);
			t.setDaemon(true);
			return t;
		}
	};

	private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), THREADFACTORY);

	private static final Channels instance = new Channels();

	Map<String, ChannelPool> pools = new TreeMap<String, ChannelPool>();

	private int maxConnectionsPerHost = 15;

	public static ChannelPool getPool(final String host, final int port) {
		final String key = host + ":" + port;
		ChannelPool pool = instance.get(key);
		if (pool == null) {
			pool = new ChannelPool(instance, host, port, instance.maxConnectionsPerHost);
			instance.set(key, pool);
		}
		return pool;
	}

	public static void setMaxConnectionsPerHost(final int maxConnectionsPerHost) {
		instance.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	public static int getMaxConnectionsPerHost() {
		return instance.maxConnectionsPerHost;
	}

	public static void setEventLoopGroup(EventLoopGroup eventLoopGroup) {
		instance.eventLoopGroup = eventLoopGroup;
	}

	public static EventLoopGroup getEventLoopGroup() {
		return instance.eventLoopGroup;
	}

	protected ChannelPool get(final String key) {
		return pools.get(key);
	}

	protected void set(final String key, final ChannelPool cp) {
		final ChannelPool oldCp = pools.put(key, cp);
		if (oldCp != null) {
			oldCp.destroy();
		}
	}

	public static Channel connect(final String host, final int port) {
		return connect(host, port, getEventLoopGroup());
	}

	private static Channel connect(final String host, final int port, final EventLoopGroup eventLoopGroup) {
		final Bootstrap bootstrap = new Bootstrap().group(eventLoopGroup).remoteAddress(host, port).channel(NioSocketChannel.class).handler(new AjpMessagesHandler());
		try {
			final ChannelFuture cf = bootstrap.connect().sync();
			Channel channel = cf.channel();
			if (channel == null) {
				throw new IllegalStateException();
			} else {
				return channel;
			}
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
