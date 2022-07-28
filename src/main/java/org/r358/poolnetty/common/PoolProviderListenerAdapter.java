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
 * Adapter for the PoolProviderListener.
 */
public class PoolProviderListenerAdapter implements PoolProviderListener
{
    @Override
    public void started(PoolProvider provider)
    {

    }

    @Override
    public void stopped(PoolProvider provider)
    {

    }

    @Override
    public void leaseRequested(PoolProvider provider, int leaseTime, TimeUnit units, Object userObject)
    {

    }

    @Override
    public void leaseGranted(PoolProvider provider, Channel channel, Object userObject)
    {

    }

    @Override
    public void leaseCanceled(PoolProvider provider, Object userObject)
    {

    }

    @Override
    public void leaseYield(PoolProvider provider, Channel channel, Object userObject)
    {

    }

    @Override
    public void leaseExpired(PoolProvider provider, Channel channel, Object userObject)
    {

    }

    @Override
    public void connectionClosed(PoolProvider provider, Channel channel)
    {

    }

    @Override
    public void connectionCreated(PoolProvider provider, Channel channel, boolean immortal)
    {

    }

    @Override
    public void ephemeralReaped(PoolProvider poolProvider, Channel channel)
    {

    }
}
