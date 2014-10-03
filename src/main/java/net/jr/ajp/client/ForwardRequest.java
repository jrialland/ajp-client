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
package net.jr.ajp.client;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * interface to implement when providing the necessary infos that are needeed to produce a request
 * 
 * @see Forward#asyncForward(ForwardRequest, ForwardResponse)
 * @author jrialland
 * 
 */
public interface ForwardRequest {

	String getMethod();

	String getProtocol();

	boolean isSsl();

	String getRequestUri();

	String getRemoteAddress();

	String getRemoteHost();

	String getServerName();

	int getServerPort();

	Collection<Header> getHeaders();

	String getHeader(String headerName);

	Map<Attribute, String> getAttributes();

	InputStream getRequestBody();
}
