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
package com.github.jrialland.ajpclient;

import io.netty.channel.Channel;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.github.jrialland.ajpclient.CPing;
import com.github.jrialland.ajpclient.pool.Channels;

public class TestCPing extends AbstractTomcatTest {

	public TestCPing() {
		super(AbstractTomcatTest.Protocol.Ajp);
	}

	@Test
	public void testCping() throws Exception {

		final boolean pong = new CPing(2, TimeUnit.SECONDS).execute("localhost", getPort());
		Assert.assertTrue(pong);
	}

	public void testUnknownHost() throws Exception {
		try {
		    final Channel channel = Channels.connect("unknownHost", getPort());
		    new CPing(2, TimeUnit.SECONDS).execute(channel);
		    Assert.fail("an exception should have been raised");
		} catch(UnresolvedAddressException|UnknownHostException e) {
		  
		} catch(Exception e) {
		  Assert.fail("bad exception type");
		}
	}

	@Test(expected = ConnectException.class)
	public void testWrongPort() throws Exception {
		final Channel channel = Channels.connect("localhost", getPort() + 1);
		new CPing(2, TimeUnit.SECONDS).execute(channel);
	}

}
