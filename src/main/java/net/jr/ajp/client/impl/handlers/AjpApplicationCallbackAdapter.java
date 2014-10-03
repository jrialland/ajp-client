package net.jr.ajp.client.impl.handlers;

import net.jr.ajp.client.impl.messages.CPongMessage;
import net.jr.ajp.client.impl.messages.EndResponseMessage;
import net.jr.ajp.client.impl.messages.GetBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendHeadersMessage;

public class AjpApplicationCallbackAdapter implements AjpApplicationHandlerCallback {

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
