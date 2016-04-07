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

import java.io.OutputStream;

/**
 * interface to implement int order to receive response from the servlet
 * container
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public interface ForwardResponse {

	/**
	 * http status line
	 *
	 * @param code
	 * @param message
	 */
	public void setStatus(int code, String message);

	/**
	 * response header.
	 *
	 * @param headerName
	 * @param value
	 */
	public void setHeader(String headerName, String value);

	/**
	 * called when the response body is about to be obtain
	 */
	public void atResponseBodyBegin();

	/**
	 * called the servlet container has finished sending response body
	 *
	 * @param reuse
	 * @throws Exception
	 */
	public void atResponseBodyEnd(boolean reuse) throws Exception;

	/**
	 * gets the outputstream where the response body will be written
	 */
	public OutputStream getOutputStream();
}
