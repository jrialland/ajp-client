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
package net.jr.ajp.client.servlet;

import java.nio.file.Path;
import java.nio.file.Paths;

import net.jr.ajp.client.AbstractTomcatTest;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TestServletProxy extends AbstractTomcatTest {

	private static final Path DIZZY_MP4 = Paths.get("./src/test/resources/dizzy.mp4");

	public TestServletProxy() {
		super(Protocol.Ajp);
		addStaticResource("/dizzy.mp4", DIZZY_MP4);
	}

	@Test
	public void doTestGet() throws Exception {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/dizzy.mp4");
		final MockHttpServletResponse response = new MockHttpServletResponse();
		AjpServletProxy.forHost("localhost", getPort()).forward(request, response);
		if (response.getStatus() != 200) {
			System.out.println(response.getContentAsString());
			Assert.fail(response.getErrorMessage());
		}
	}

	@Test
	@Ignore
	public void testManyTimes() throws Exception {
		for (int i = 0; i < 100; i++) {
			doTestGet();
		}
	}

}
