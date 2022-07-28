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
package com.github.jrialland.ajpclient.util;

import com.github.jrialland.ajpclient.servlet.JavaxServletApiCompat;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class ApiCompatTest {

    @Test
    public void test() {
        javax.servlet.http.HttpServletRequest request = new MockHttpServletRequest();
        jakarta.servlet.http.HttpServletRequest jakartaRequest = JavaxServletApiCompat.INSTANCE.makeProxy(request);
        jakartaRequest.setAttribute("test", true);
    }
}
