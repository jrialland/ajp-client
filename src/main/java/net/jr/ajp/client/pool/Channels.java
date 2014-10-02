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
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.jr.ajp.client.impl.handlers.ContainerMessageHandler;

public class Channels {

	public static final String CONVERSATION_HANDLER_NAME = "ajp-handler";

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

	private static NioEventLoopGroup DEFAULT_EVENTLOOP_GROUP = new NioEventLoopGroup(10, THREADFACTORY);

	private static final Channels instance = new Channels();

	Map<String, ChannelPool> pools = new TreeMap<String, ChannelPool>();

	private int maxConnectionsPerHost = 15;

	protected static ThreadFactory getThreadFactory() {
		return THREADFACTORY;
	}

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
		return connect(host, port, DEFAULT_EVENTLOOP_GROUP);
	}

	public static Channel connect(final String host, final int port, final NioEventLoopGroup eventLoopGroup) {
		final Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		bootstrap.remoteAddress(host, port);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(final Channel ch) throws Exception {
				ch.pipeline().addLast(new ContainerMessageHandler());
				ch.pipeline().addLast(CONVERSATION_HANDLER_NAME, new ChannelInboundHandlerAdapter());
			}
		});
		try {
			final ChannelFuture cf = bootstrap.connect().sync();
			return cf.channel();
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
