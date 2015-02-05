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
 * 
 */
package com.github.jrialland.ajpclient.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrialland.ajpclient.impl.handlers.AjpMessagesHandler;
import com.github.jrialland.ajpclient.impl.handlers.OutgoingFramesLogger;
import com.github.jrialland.ajpclient.jmx.JmxExporter;

public final class Channels {

	private static final Logger LOGGER = LoggerFactory.getLogger(Channels.class);

	public static Logger getLog() {
		return LOGGER;
	}

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

	public static synchronized ChannelPool getPool(final String host, final int port) throws Exception {
		final String key = host + ":" + port;
		ChannelPool pool = instance.get(key);
		if (pool == null) {
			pool = new ChannelPool(host, port);
			instance.set(key, pool);
			JmxExporter.exportMonitor(pool);
			getLog().debug("added " + pool);
		}
		return pool;
	}

	public static ChannelPool getPool(final URI uri) throws Exception {
		if (!uri.getScheme().equals("ajp")) {
			throw new IllegalArgumentException("only ajp:// uris are supported");
		}
		int port = uri.getPort();
		if (port < 0) {
			port = 8009;
		}
		return getPool(uri.getHost(), uri.getPort());
	}

	public static void setEventLoopGroup(final EventLoopGroup eventLoopGroup) {
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

	public static Bootstrap newBootStrap(final String host, final int port) {
		return newBootStrap(host, port, getEventLoopGroup());
	}

	public static Bootstrap newBootStrap(final String host, final int port, final EventLoopGroup eventLoopGroup) {
		return new Bootstrap().group(getEventLoopGroup()).remoteAddress(host, port).channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.AUTO_READ, true);
	}

	public static void initChannel(final Channel channel) {
		if (getLog().isTraceEnabled()) {
			channel.pipeline().addLast(new OutgoingFramesLogger());
		}
		channel.pipeline().addLast(new AjpMessagesHandler());
	}

	private static Channel connect(final String host, final int port, final EventLoopGroup eventLoopGroup) {
		final Bootstrap bootstrap = newBootStrap(host, port, eventLoopGroup);
		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(final Channel ch) throws Exception {
				Channels.initChannel(ch);
			}
		});
		try {
			final ChannelFuture cf = bootstrap.connect().sync();
			final Channel channel = cf.channel();
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
