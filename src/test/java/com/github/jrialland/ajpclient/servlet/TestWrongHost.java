/* Copyright (c) 2014-2022 Julien Rialland <julien.rialland@gmail.com>
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
package com.github.jrialland.ajpclient.servlet;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

        Assertions.assertTrue(System.currentTimeMillis() - start < 8000);

        Assertions.assertEquals(HttpServletResponse.SC_BAD_GATEWAY, status);
    }

}
