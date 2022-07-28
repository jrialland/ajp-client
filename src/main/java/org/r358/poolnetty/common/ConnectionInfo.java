/*
 * Copyright (c) 2014 R358 https://github.com/R358
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.r358.poolnetty.common;

import io.netty.channel.ChannelInitializer;

import java.net.SocketAddress;

/**
 * Defines the connection.
 */
public class ConnectionInfo
{
    /**
     * Netty channel initializer instance.
     */
    private final ChannelInitializer channelInitializer;
    /**
     * The remote address.
     */
    private final SocketAddress remoteSocketAddress;

    /**
     * The Local address, can be null.
     */
    private final SocketAddress localSocketAddress;


    /**
     * Create connection info.
     *
     * @param remoteSocketAddress The remote address, where you want to connect to.
     * @param localSocketAddress  Local address or null.
     * @param channelInitializer  The channel netty channel initializer.
     */
    public ConnectionInfo(SocketAddress remoteSocketAddress, SocketAddress localSocketAddress, ChannelInitializer channelInitializer)
    {
        this.channelInitializer = channelInitializer;
        this.remoteSocketAddress = remoteSocketAddress;
        this.localSocketAddress = localSocketAddress;
    }

    /**
     * Return the channel initializer.
     *
     * @return The channel initializer.
     */
    public ChannelInitializer getChannelInitializer()
    {
        return channelInitializer;
    }

    /**
     * Return the remote address.
     *
     * @return The remote address.
     */
    public SocketAddress getRemoteSocketAddress()
    {
        return remoteSocketAddress;
    }

    /**
     * Return the local address.
     *
     * @return The local address.
     */

    public SocketAddress getLocalSocketAddress()
    {
        return localSocketAddress;
    }
}
