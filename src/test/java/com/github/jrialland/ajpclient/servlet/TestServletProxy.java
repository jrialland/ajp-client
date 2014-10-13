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
package com.github.jrialland.ajpclient.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.github.jrialland.ajpclient.AbstractTomcatTest;
import com.github.jrialland.ajpclient.Constants;

public class TestServletProxy extends AbstractTomcatTest {

	private static final Path DIZZY_MP4 = Paths.get("./src/test/resources/dizzy.mp4");

	public TestServletProxy() {
		super(Protocol.Ajp);
		addStaticResource("/dizzy.mp4", DIZZY_MP4);

		addServlet("/test_post", new HttpServlet() {

			private static final long serialVersionUID = 168417L;

			@Override
			protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
				final int nParams = Collections.list(req.getParameterNames()).size();
				resp.getWriter().print(nParams);
			}
		});
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

	private static String slurp(final InputStream is) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buff = new byte[4096];
		int c = 0;
		while ((c = is.read(buff)) > -1) {
			baos.write(buff, 0, c);
		}
		return baos.toString();
	}

	@Test
	public void doTestPost() throws Exception {

		final String cookie = slurp(TestServletProxy.class.getResource("cookie.txt").openStream());

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setRequestURI("/test_post");
		request.addHeader("Host", "test.samplesite.com");

		request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		request.addHeader("Accept-Encoding", "gzip, deflate");
		request.addHeader("Accept-Language", "fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3");
		request.addHeader("Connection", "keep-alive");
		request.addHeader("Cookie", cookie);

		request.addHeader(
				"Referer",
				"https://test.samplesite.com/fr/group/control_panel/manage?p_auth=1bo6fC5N&p_p_id=dbSettingsPortlet_WAR_eloportalservicesportlet&p_p_lifecycle=1&p_p_state=maximized&p_p_mode=view&doAsGroupId=10157&refererPlid=10160&_dbSettingsPortlet_WAR_eloportalservicesportlet_action=showUpdate&_dbSettingsPortlet_WAR_eloportalservicesportlet_client=TMG");
		request.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:32.0) Gecko/20100101 Firefox/32.0");
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setContent("_dbSettingsPortlet_WAR_eloportalservicesportlet_client=xxx&_dbSettingsPortlet_WAR_eloportalservicesportlet_url=jdbc%3Ajtds%3Asqlserver%3A%2F%2Fxxx.xxx.xxx.xxx%2FBD_FR533&_dbSettingsPortlet_WAR_eloportalservicesportlet_user=sa&_dbSettingsPortlet_WAR_eloportalservicesportlet_password=123abcd+&_dbSettingsPortlet_WAR_eloportalservicesportlet_poolmax=5"
				.getBytes());

		final MockHttpServletResponse response = new MockHttpServletResponse();
		AjpServletProxy.forHost("localhost", getPort()).forward(request, response);
		if (response.getStatus() != 200) {
			System.out.println(response.getContentAsString());
			Assert.fail(response.getErrorMessage());
		}

		Assert.assertEquals("5", response.getContentAsString());
	}

	/**
	 * Test that is fails with a request bigger that 8k
	 */
	@Test(expected = ServletException.class)
	public void testTooBigRequest() throws Exception {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/dizzy.mp4");

		// generate a request with an enormous 'Cookie' header
		String cookie = "";
		final int i = 0;
		while (cookie.length() < Constants.MAX_MESSAGE_SIZE) {
			cookie = cookie + "COOKIE" + i + "=foo;";
		}
		request.addHeader("Cookie", cookie);

		final MockHttpServletResponse response = new MockHttpServletResponse();
		AjpServletProxy.forHost("localhost", getPort()).forward(request, response);
	}
}
