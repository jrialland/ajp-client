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

/**
 * A ConnectionInfoProvider provides information for the pool so that it can create new connections.
 * <p>For Example:</p>
 * <p>If a pool is defined to have a number of fixed connections an implementation of this interface would be called by the pool to create those contexts.</p>
 * <p>As netty context is much more than a simple connection, the complexity of setting up handlers needs to be delegated.</p>
 * <p>It is safe to return the same instance of ConnectionInfo.</p>
 * <p/>
 * <p>Note that the pool will add a transparent ChannelInboundHandler.</p>
 * <p><B>Thread Safety:</B> A PoolProvider guarantees to sequentially call this interface during connection creation, if an implementation is shared between pool providers then no such guarantees are made.</p>
 */
public interface ConnectionInfoProvider
{

    /**
     * Provide the connection info so the PoolProvider can create a connection.
     *
     * @param poolProvider The calling pool provider.
     * @return An instance of ConnectionInfo.
     */
    ConnectionInfo connectionInfo(PoolProvider poolProvider);
}

