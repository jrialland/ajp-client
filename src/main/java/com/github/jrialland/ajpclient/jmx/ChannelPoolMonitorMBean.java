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
package com.github.jrialland.ajpclient.jmx;

import java.util.Date;

/**
 * interface of the mbean that is exported for each ChannelPool.
 * 
 * @author julien
 *
 */
public interface ChannelPoolMonitorMBean {

	/**
	 * count active connections
	 * 
	 * @return active connections
	 */
	public int getActiveConnections();

	/**
	 * date when the pool has been created
	 * 
	 * @return when the pool has been created
	 */
	public Date getStartTime();

	/**
	 * target hostname
	 * 
	 * @return target hostname
	 */
	public String getHost();

	/**
	 * Target tcp port
	 * 
	 * @return Target tcp port
	 */
	public int getPort();

	/**
	 * Resets all the connections owned by the pool
	 */
	public void resetConnections();
}
