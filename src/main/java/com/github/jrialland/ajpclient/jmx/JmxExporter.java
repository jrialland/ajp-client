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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrialland.ajpclient.pool.ChannelPool;
import com.github.jrialland.ajpclient.pool.Channels;

/**
 * given a ChannelPool, tries to export a monitor for it using jmx
 * 
 * @author julien
 *
 */
public class JmxExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(JmxExporter.class);

	private static final Logger getLog() {
		return LOGGER;
	}

	/**
	 * export a monitor for the pool.
	 * 
	 * @param channelPool
	 *            any registred pool instanciated by
	 *            {@link Channels#getPool(String, int)}
	 */
	public static void exportMonitor(ChannelPool channelPool) {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName objectName = makeObjectName(channelPool.getHost(), channelPool.getPort());
			try {
				server.getMBeanInfo(objectName);
			} catch (InstanceNotFoundException e) {
				StandardMBean mBean = new StandardMBean(channelPool.getMonitor(), ChannelPoolMonitorMBean.class);
				server.registerMBean(mBean, objectName);
				getLog().debug("new mBean registered for channelPool " + channelPool);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static ObjectName makeObjectName(String host, int port) throws MalformedObjectNameException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("host", host);
		properties.put("port", Integer.toString(port));
		return new ObjectName(ChannelPool.class.getPackage().getName() + ".monitor", properties);
	}

	/**
	 * get an mbean instance for a remote jmx-enabled jvm.
	 * 
	 * @param jmxServerUrl
	 *            jmx service url, for example
	 *            <code>service:jmx:rmi:///jndi/rmi://:9999/jmxrmi</code>
	 * @param host
	 *            channel pool's tcp host
	 * @param port
	 *            channel pool's tcp port
	 * @return a proxy for the monitor associated with the pool
	 */
	public static ChannelPoolMonitorMBean getMonitor(String jmxServerUrl, String host, int port) throws IOException {
		ObjectName objectName = null;
		try {
			objectName = makeObjectName(host, port);
		} catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException(e);
		}

		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
		JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

		Set<ObjectInstance> set = mbsc.queryMBeans(objectName, null);
		if (set == null || set.isEmpty()) {
			return null;
		} else {
			return MBeanServerInvocationHandler.newProxyInstance(mbsc, objectName, ChannelPoolMonitorMBean.class, true);
		}
	}
}
