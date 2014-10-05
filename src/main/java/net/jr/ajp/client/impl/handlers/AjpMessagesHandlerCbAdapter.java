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
package net.jr.ajp.client.impl.handlers;

import net.jr.ajp.client.impl.messages.CPongMessage;
import net.jr.ajp.client.impl.messages.EndResponseMessage;
import net.jr.ajp.client.impl.messages.GetBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendHeadersMessage;

public class AjpMessagesHandlerCbAdapter implements AjpMessagesHandlerCallback {

	@Override
	public void handleCPongMessage(final CPongMessage cPongMessage) throws Exception {
		// no-op
	}

	@Override
	public void handleEndResponseMessage(final EndResponseMessage endResponseMessage) throws Exception {
		// no-op
	}

	@Override
	public void handleGetBodyChunkMessage(final GetBodyChunkMessage getBodyChunkMessage) throws Exception {
		// no-op
	}

	@Override
	public void handleSendBodyChunkMessage(final SendBodyChunkMessage sendBodyChunkMessage) throws Exception {
		// no-op
	}

	@Override
	public void handleSendHeadersMessage(final SendHeadersMessage sendHeadersMessage) throws Exception {
		// no-op
	}
}
