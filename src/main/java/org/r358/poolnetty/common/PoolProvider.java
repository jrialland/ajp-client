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
import org.r358.poolnetty.common.exceptions.PoolProviderException;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * All pool implementations implement this interface is provides the basic concepts of:
 * <ol>
 * <li><B>Request</B> Lease a resource from the pool.</li>
 * <li><B>Release</B> Yield a resource back to the pool.</li>
 * </ol>
 */
public interface PoolProvider
{

    Future<LeasedChannel> leaseAsync(int time, TimeUnit units, Object userObject);

    /**
     * Request a lease.
     *
     * @param time       The lease time.
     * @param units      Time unists.
     * @param userObject The user object
     * @return A future.
     */
    Future<LeasedChannel> leaseAsync(int time, TimeUnit units, Object userObject, LeaseListener listener);

    /**
     * Blocking Request a ChannelHandlerContext from the pool
     *
     * @return The context.
     * @throws Exception
     */
    LeasedChannel lease(int leaseTime, TimeUnit units, Object userObject)
        throws PoolProviderException;

    /**
     * Release a channel back to the pool
     *
     * @param channel The context to release.
     * @throws PoolProviderException if the context is unknown to the pool or declared lost to the pool because lease time expired.
     */
    void yield(Channel channel)
        throws PoolProviderException;


    /**
     * Start the pool.
     *
     * @param timeToWait Time to wait.
     * @param timeUnit   Time units.
     * @return True if all immortal connections are made in the timeToWait time.
     * @throws Exception rethrows all exceptions.
     */
    boolean start(long timeToWait, TimeUnit timeUnit)
        throws Exception;

    /**
     * Stop the pool. When force == false the pool will only stop when all channels are yielded,
     * when force is true the connections are closed ungracefully and the pool stops.
     *
     * @param force when true caused the pool to shutdown immediately.
     */
    void stop(boolean force);

    /**
     * Execute the runnable on the pools decoupler thread.
     *
     * @param runnable The runnable.
     */
    void execute(Runnable runnable);

    /**
     * Add listener.
     *
     * @param listener The listener to add.
     */
    void addListener(PoolProviderListener listener);

    /**
     * Remove listener.
     *
     * @param listener The listener.
     */
    void removeListener(PoolProviderListener listener);
}
