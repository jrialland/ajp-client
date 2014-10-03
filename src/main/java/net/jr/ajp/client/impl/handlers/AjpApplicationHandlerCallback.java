package net.jr.ajp.client.impl.handlers;

import net.jr.ajp.client.impl.messages.CPongMessage;
import net.jr.ajp.client.impl.messages.EndResponseMessage;
import net.jr.ajp.client.impl.messages.GetBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendBodyChunkMessage;
import net.jr.ajp.client.impl.messages.SendHeadersMessage;

public interface AjpApplicationHandlerCallback {

	void handleCPongMessage(CPongMessage cPongMessage) throws Exception;

	void handleEndResponseMessage(EndResponseMessage endResponseMessage) throws Exception;

	void handleGetBodyChunkMessage(GetBodyChunkMessage getBodyChunkMessage) throws Exception;

	void handleSendBodyChunkMessage(SendBodyChunkMessage sendBodyChunkMessage) throws Exception;

	void handleSendHeadersMessage(SendHeadersMessage sendHeadersMessage) throws Exception;

}
