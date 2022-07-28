
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

import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public interface PoolProviderListener
{
    /**
     * Called when pool has finished starting, after immortal connections are created.
     *
     * @param provider The provider.
     */
    void started(PoolProvider provider);

    /**
     * Called after all connections are closed.
     *
     * @param provider The provider.
     */
    void stopped(PoolProvider provider);

    /**
     * Called when a lease is requested.
     *
     * @param provider   The Provider.
     * @param leaseTime  The time of the lease.
     * @param units      The units.
     * @param userObject The userobject accompanying the lease request.
     */
    void leaseRequested(PoolProvider provider, int leaseTime, TimeUnit units, Object userObject);

    /**
     * Called when a lease is actually granted.
     *
     * @param provider   The provider.
     * @param channel    The channel.
     * @param userObject The userobject accompanying the lease request.
     */
    void leaseGranted(PoolProvider provider, Channel channel, Object userObject);


    /**
     * Called the the lease is canceled.
     *
     * @param provider   The provider.
     * @param userObject The userobject accompanying the lease request.
     */
    void leaseCanceled(PoolProvider provider, Object userObject);


    /**
     * Called when a lease is yielded.
     *
     * @param provider   The provider.
     * @param channel    The channel.
     * @param userObject The userobject accompanying the lease request.
     */
    void leaseYield(PoolProvider provider, Channel channel, Object userObject);

    /**
     * Called when a lease has expired.
     *
     * @param provider   The provider.
     * @param channel    The channel.
     * @param userObject The userobject accompanying the lease request.
     */
    void leaseExpired(PoolProvider provider, Channel channel, Object userObject);

    /**
     * Called when a connection is closed.
     *
     * @param provider The provider.
     * @param channel  The channel.
     */
    void connectionClosed(PoolProvider provider, Channel channel);


    /**
     * Called when a  connection is created.
     *
     * @param provider The provider.
     * @param channel  The context.
     * @param immortal true if immortal.
     */
    void connectionCreated(PoolProvider provider, Channel channel, boolean immortal);

    /**
     * Called when a connection is reaped, and is scheduled for closure.
     *
     * @param poolProvider The provider.
     * @param channel      The Channel.
     */
    void ephemeralReaped(PoolProvider poolProvider, Channel channel);
}
