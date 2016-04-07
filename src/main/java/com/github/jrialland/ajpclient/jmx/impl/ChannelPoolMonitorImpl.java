/* Copyright (c) 2014-2016 Julien Rialland <julien.rialland@gmail.com>
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
package com.github.jrialland.ajpclient.jmx.impl;

import io.netty.channel.Channel;

import java.util.Date;

import org.r358.poolnetty.common.PoolProvider;
import org.r358.poolnetty.common.PoolProviderListenerAdapter;

import com.github.jrialland.ajpclient.jmx.ChannelPoolMonitorMBean;
import com.github.jrialland.ajpclient.pool.ChannelPool;

public class ChannelPoolMonitorImpl extends PoolProviderListenerAdapter implements ChannelPoolMonitorMBean {

	private Date startTime = new Date();

	private ChannelPool channelPool;

	public ChannelPoolMonitorImpl(ChannelPool channelPool) {
		this.channelPool = channelPool;
	}

	private long createdConnections = 0;

	private long closedConnections = 0;

	@SuppressWarnings("unused")
	private long leaseGranted = 0;

	@SuppressWarnings("unused")
	private long leaseYield = 0;

	@Override
	public void connectionCreated(PoolProvider provider, Channel channel, boolean immortal) {
		createdConnections++;
	}

	@Override
	public void leaseGranted(PoolProvider provider, Channel channel, Object userObject) {
		leaseGranted++;
	}

	@Override
	public void leaseYield(PoolProvider provider, Channel channel, Object userObject) {
		leaseYield++;
	}

	@Override
	public void connectionClosed(PoolProvider provider, Channel channel) {
		closedConnections++;
	}

	@Override
	public Date getStartTime() {
		return startTime;
	}

	@Override
	public int getActiveConnections() {
		return (int) (createdConnections - closedConnections);
	}

	@Override
	public String getHost() {
		return channelPool.getHost();
	}

	@Override
	public int getPort() {
		return channelPool.getPort();
	}

	@Override
	public void resetConnections() {
		channelPool.reset();
	}
}
