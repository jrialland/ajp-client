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

import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrialland.ajpclient.CPing;
import com.github.jrialland.ajpclient.Forward;
import com.github.jrialland.ajpclient.impl.CPingImpl;
import com.github.jrialland.ajpclient.impl.logging.Slf4jLogWriter;

public class ChannelPool {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);

	private static final Logger getLog() {
		return LOGGER;
	}

	String host;

	int port;

	private final GenericObjectPool<Channel> objectPool;

	private final PooledObjectFactory<Channel> factory = new BasePooledObjectFactory<Channel>() {

		@Override
		public Channel create() throws Exception {
			final Channel channel = Channels.connect(host, port);
			getLog().debug("obtained " + channel);
			return channel;
		}

		@Override
		public PooledObject<Channel> wrap(final Channel obj) {
			return new DefaultPooledObject<Channel>(obj);
		}

		@Override
		public void destroyObject(final PooledObject<Channel> p) throws Exception {
			p.getObject().close();
		}

		@Override
		public boolean validateObject(final PooledObject<Channel> p) {
			try {
				final Channel channel = p.getObject();
				return channel.isActive() && new CPingImpl(250, TimeUnit.MILLISECONDS).execute(channel);
			} catch (final Exception e) {
				return false;
			}
		}

	};

	protected ChannelPool(final String host, final int port, final int maxConnections) throws Exception {
		this.host = host;
		this.port = port;
		objectPool = new GenericObjectPool<Channel>(factory);
		if (maxConnections < 1) {
			throw new IllegalArgumentException("maxConnections must be > 0");
		}
		objectPool.setMaxTotal(maxConnections);
		objectPool.setMinIdle(1);
		objectPool.setTestWhileIdle(true);
		objectPool.setTestOnBorrow(false);
		objectPool.setTestOnCreate(false);
		objectPool.setTestOnReturn(false);
		objectPool.setTimeBetweenEvictionRunsMillis(20000);
		objectPool.setMinEvictableIdleTimeMillis(30000);

		final AbandonedConfig abandonedConfig = new AbandonedConfig();
		abandonedConfig.setLogAbandoned(true);

		if (getLog().isDebugEnabled()) {
			abandonedConfig.setLogWriter(new Slf4jLogWriter(getLog()));
		}

		abandonedConfig.setRemoveAbandonedOnMaintenance(true);
		abandonedConfig.setUseUsageTracking(false);
		abandonedConfig.setRemoveAbandonedTimeout(30000);

		objectPool.setAbandonedConfig(abandonedConfig);

		objectPool.addObject();
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
		final Channel channel = objectPool.borrowObject();
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

	public GenericObjectPool<Channel> getObjectPool() {
		return objectPool;
	}
}
