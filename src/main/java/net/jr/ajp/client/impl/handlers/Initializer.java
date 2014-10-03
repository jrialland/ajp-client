package net.jr.ajp.client.impl.handlers;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class Initializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new ContainerMessageHandler()).addLast(new AjpApplicationHandler());
	}
}
