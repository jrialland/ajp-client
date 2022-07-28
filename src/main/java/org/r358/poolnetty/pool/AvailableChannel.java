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

package org.r358.poolnetty.pool;

import io.netty.channel.Channel;

import java.util.concurrent.ScheduledFuture;

/**
 * A wrapper for the context.
 * Not thread safe.
 */
public class AvailableChannel
{
    private final long closeAfter;
    private final Channel channel;
    private final int lifespan;
    private final boolean immortal;
    private final ScheduledFuture reaper;

    public AvailableChannel(long closeAfter, Channel channel, int lifespan, boolean immortal, ScheduledFuture reaper)
    {
        this.closeAfter = closeAfter;
        this.channel = channel;
        this.lifespan = lifespan;
        this.immortal = immortal;
        this.reaper = reaper;
    }

    public Channel getChannel()
    {
        return channel;
    }


    public void cancelReaper()
    {
        if (reaper != null)
        {
            reaper.cancel(false); // Should not be an issue as it is all running from the same decoupler within the pool.
        }
    }

    /**
     * Has this context expired.
     *
     * @param notAfter Not after.
     * @return
     */
    public boolean expired(long notAfter)
    {
        return !immortal & notAfter < closeAfter;
    }

    public boolean isImmortal()
    {
        return immortal;
    }

    public int getLifespan()
    {
        return lifespan;
    }
}
