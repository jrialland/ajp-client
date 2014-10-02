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

import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.TimeUnit;

import net.jr.ajp.client.CPing;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelPool {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);

	private static final Logger getLog() {
		return LOGGER;
	}

	String host;

	int port;

	private final NioEventLoopGroup eventLoopGroup;

	private final GenericObjectPool<Channel> objectPool;

	private final PooledObjectFactory<Channel> factory = new BasePooledObjectFactory<Channel>() {

		@Override
		public Channel create() throws Exception {
			getLog().debug("connecting to " + host + ":" + port);
			final Channel ch = Channels.connect(host, port, eventLoopGroup);
			return ch;
		}

		@Override
		public PooledObject<Channel> wrap(final Channel obj) {
			return new DefaultPooledObject<Channel>(obj);
		}

		@Override
		public void destroyObject(final PooledObject<Channel> p) throws Exception {
			final Channel channel = p.getObject();
			if (channel.isActive()) {
				channel.close();
			}
			getLog().debug("destroyed connection to " + host + ":" + port);
		}

		@Override
		public boolean validateObject(final PooledObject<Channel> p) {
			try {
				return new CPing(2, TimeUnit.SECONDS).doWithChannel(p.getObject());
			} catch (final Exception e) {
				return false;
			}
		}

	};

	protected ChannelPool(final Channels cp, final String host, final int port, final int maxConnections) {
		this.host = host;
		this.port = port;
		eventLoopGroup = new NioEventLoopGroup(maxConnections, Channels.getThreadFactory());
		objectPool = new GenericObjectPool<Channel>(factory);
		if (maxConnections < 1) {
			throw new IllegalArgumentException("maxConnections must be > 0");
		}
		objectPool.setMaxTotal(maxConnections);
		objectPool.setMinIdle(1);
		objectPool.setTestWhileIdle(true);
		objectPool.setTestOnBorrow(false);
		objectPool.setTestOnCreate(true);
		objectPool.setTestOnReturn(false);
		objectPool.setTimeBetweenEvictionRunsMillis(20000);
	}

	public void execute(final ChannelCallback callback) throws Exception {
		getLog().debug("getting channel from the connection pool ...");
		final Channel channel = objectPool.borrowObject();
		getLog().debug("... obtained " + channel);

		boolean reuse = false;
		try {
			reuse = callback.doWithChannel(channel);
		} finally {
			if (reuse) {
				getLog().debug("returning channel " + channel + " to the connection pool");
				objectPool.returnObject(channel);
			} else {
				getLog().debug("invalidating channel " + channel);
				objectPool.invalidateObject(channel);
			}
		}
	}

	protected void destroy() {
		objectPool.clear();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
