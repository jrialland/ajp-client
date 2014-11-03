package com.github.jrialland.ajpclient.servlet;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TestWrongHost {

	/**
	 * tries to make a request to an unknown host, verifies that the request
	 * fails (the library does not hangs) and that we have a 502 error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWrongTargetHost() throws Exception {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/dizzy.mp4");
		final MockHttpServletResponse response = new MockHttpServletResponse();

		final Future<Integer> statusFuture = Executors.newSingleThreadExecutor().submit(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				AjpServletProxy.forHost("unknownhost.inexistentdomain.com", 8415).forward(request, response);
				return response.getStatus();
			}
		});

		final long start = System.currentTimeMillis();

		// should finish in less that seconds
		final int status = statusFuture.get(10, TimeUnit.SECONDS);

		Assert.assertTrue(System.currentTimeMillis() - start < 8000);

		Assert.assertEquals(HttpServletResponse.SC_BAD_GATEWAY, status);
	}

}
