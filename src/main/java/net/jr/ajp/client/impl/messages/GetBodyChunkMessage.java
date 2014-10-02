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
package net.jr.ajp.client.impl.messages;

/**
 * the server wants more data
 * 
 * @author jrialland
 * 
 */
public class GetBodyChunkMessage extends AjpMessage {

	private final int requestedLength;

	public GetBodyChunkMessage(final int requestedLength) {
		this.requestedLength = requestedLength;
	}

	/**
	 * the max amount of data that the server can take in the next message
	 * 
	 * @return
	 */
	public int getRequestedLength() {
		return requestedLength;
	}
}
