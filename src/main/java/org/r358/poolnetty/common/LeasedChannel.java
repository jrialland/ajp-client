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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.r358.poolnetty.common.concurrent.ValueEvent;
import org.r358.poolnetty.common.exceptions.PoolProviderException;

import java.net.SocketAddress;

/**
 * A wrapper around a netty channel.
 */
public class LeasedChannel
    implements Channel,
    Leasee
{
    /**
     * Inner channel.
     */
    private final Channel inner;
    /**
     * Pool provider.
     */
    private final PoolProvider poolProvider;

    /**
     * The user object.
     */
    private final Object userObject;

    /**
     * Lease expiration callback.
     */
    private ValueEvent<Leasee> leaseExpirationCallback;

    /**
     * The owning context.
     */
    private final LeasedContext owningContext;

    public LeasedChannel(LeasedContext owningContext, Channel inner, PoolProvider provider, Object userObject)
    {
        this.owningContext = owningContext;
        this.inner = inner;
        this.poolProvider = provider;
        this.userObject = userObject;
        this.owningContext.setExpirationRunnable(new Runnable()
        {
            @Override
            public void run()
            {
                if (leaseExpirationCallback != null)
                {
                    leaseExpirationCallback.on(LeasedChannel.this);
                }
            }
        });
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> attributeKey) {
        return inner.hasAttr(attributeKey);
    }

    @Override
    public ChannelId id() {
        return inner.id();
    }

    @Override
    public long bytesBeforeWritable() {
        return inner.bytesBeforeWritable();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return inner.bytesBeforeUnwritable();
    }

    @Override
    public EventLoop eventLoop()
    {
        return inner.eventLoop();
    }

    @Override
    public Channel parent()
    {
        return inner.parent();
    }

    @Override
    public ChannelConfig config()
    {
        return inner.config();
    }

    @Override
    public boolean isOpen()
    {
        return inner.isOpen();
    }

    @Override
    public boolean isRegistered()
    {
        return inner.isRegistered();
    }

    @Override
    public boolean isActive()
    {
        return inner.isActive();
    }

    @Override
    public ChannelMetadata metadata()
    {
        return inner.metadata();
    }

    @Override
    public SocketAddress localAddress()
    {
        return inner.localAddress();
    }

    @Override
    public SocketAddress remoteAddress()
    {
        return inner.remoteAddress();
    }

    @Override
    public ChannelFuture closeFuture()
    {
        return inner.closeFuture();
    }

    @Override
    public boolean isWritable()
    {
        return inner.isWritable();
    }

    @Override
    public Channel flush()
    {
        return inner.flush();
    }

    @Override
    public Channel read()
    {
        return inner.read();
    }

    @Override
    public Unsafe unsafe()
    {
        return inner.unsafe();
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key)
    {
        return inner.attr(key);
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress)
    {
        return inner.bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress)
    {
        return inner.connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
    {
        return inner.connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect()
    {
        return inner.disconnect();
    }

    @Override
    public ChannelFuture close()
    {
        return inner.close();
    }

    @Override
    public ChannelFuture deregister()
    {
        return inner.deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise)
    {
        return inner.bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise)
    {
        return inner.connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
    {
        return inner.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise)
    {
        return inner.disconnect();
    }

    @Override
    public ChannelFuture close(ChannelPromise promise)
    {
        return inner.close();
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise)
    {
        return inner.deregister(promise);
    }

    @Override
    public ChannelFuture write(Object msg)
    {
        return inner.write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise)
    {
        return inner.write(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)
    {
        return inner.writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg)
    {
        return inner.writeAndFlush(msg);
    }

    @Override
    public ChannelPipeline pipeline()
    {
        return inner.pipeline();
    }

    @Override
    public ByteBufAllocator alloc()
    {
        return inner.alloc();
    }

    @Override
    public ChannelPromise newPromise()
    {
        return inner.newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise()
    {
        return inner.newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture()
    {
        return inner.newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause)
    {
        return inner.newFailedFuture(cause);
    }

    @Override
    public ChannelPromise voidPromise()
    {
        return inner.voidPromise();
    }

    @Override
    public int compareTo(Channel o)
    {
        return inner.compareTo(o);
    }

    @Override
    public void yield()
        throws PoolProviderException
    {
        poolProvider.yield(inner);
    }

    @Override
    public void onLeaseExpire(ValueEvent<Leasee> callThis)
    {
        this.leaseExpirationCallback = callThis;
    }

    /**
     * The actual inner channel this object wraps.
     *
     * @return The inner channel.
     */
    public Channel getInner()
    {
        return inner;
    }

    public Object getUserObject()
    {
        return userObject;
    }

    /**
     * Calling this will cause your lease expiration value event to be called.
     */
    public void fireExpired()
    {
        if (leaseExpirationCallback != null)
        {
            leaseExpirationCallback.on(this);
        }
    }

}
