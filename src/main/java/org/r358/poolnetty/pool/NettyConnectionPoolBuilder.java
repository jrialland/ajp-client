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
import org.r358.poolnetty.common.*;
import org.r358.poolnetty.common.concurrent.Completion;

/**
 * A connection pool builder.
 * The connection pool is final and this class build it. (listeners can be added an removed at will).
 * <p/>
 * <p>At the very lease you need to provide:</p>
 * <ol>
 * <li>ConnectionInfoProvider</li>
 * <li>BootstrapProvider</li>
 * </ol>
 */
public class NettyConnectionPoolBuilder
{
    protected ConnectionInfoProvider connectionInfoProvider;
    protected ContextExceptionHandler contextExceptionHandler;
    protected LeaseExpiredHandler leaseExpiredHandler;
    protected PreGrantLease preGrantLease;
    protected PreReturnToPool preReturnToPool;
    protected BootstrapProvider bootstrapProvider;
    protected PoolExceptionHandler poolExceptionHandler;
    protected LeaseExpiryReaper leaseExpiryReaper;
    protected PostConnectEstablish postConnectEstablish;
    protected String inboundHandlerName = "_pool";


    protected int immortalCount = 5;
    protected int maxEphemeralCount = 5;
    protected int ephemeralLifespanMillis = 60000;
    protected int reaperIntervalMillis = 15000;


    public NettyConnectionPoolBuilder()
    {

    }

    /**
     * Create defining the immortal count, the maximum ephemeral count an the idle lifespan (milliseconds)
     * or each ephemeral connection.
     *
     * @param immortalCount           The number of immortal connections.
     * @param maxEphemeralCount       The maximum ephemeral count.
     * @param ephemeralLifespanMillis The idle lifespan in milliseconds of each ephemeral connection.
     */
    public NettyConnectionPoolBuilder(int immortalCount, int maxEphemeralCount, int ephemeralLifespanMillis)
    {
        this.immortalCount = immortalCount;
        this.maxEphemeralCount = maxEphemeralCount;
        this.ephemeralLifespanMillis = ephemeralLifespanMillis;
    }

    public NettyConnectionPoolBuilder withImmortalCount(int immortalCount)
    {
        this.immortalCount = immortalCount;
        return this;
    }

    public NettyConnectionPoolBuilder withMaxEphemeralCount(int maxEphemeralCount)
    {
        this.maxEphemeralCount = maxEphemeralCount;
        return this;
    }

    public NettyConnectionPoolBuilder withEphemeralLifespanMillis(int ephemeralLifespanMillis)
    {
        this.ephemeralLifespanMillis = ephemeralLifespanMillis;
        return this;
    }


    public NettyConnectionPoolBuilder withPostConnectEstablish(PostConnectEstablish postConnectEstablish)
    {
        this.postConnectEstablish = postConnectEstablish;
        return this;
    }

    public NettyConnectionPoolBuilder withLeaseExpiryHarvester(LeaseExpiryReaper leaseExpiryReaper)
    {
        this.leaseExpiryReaper = leaseExpiryReaper;
        return this;
    }

    public NettyConnectionPoolBuilder withBootstrapProvider(BootstrapProvider bootstrapProvider)
    {
        this.bootstrapProvider = bootstrapProvider;
        return this;
    }

    public NettyConnectionPoolBuilder withPoolExceptionHandler(PoolExceptionHandler poolExceptionHandler)
    {
        this.poolExceptionHandler = poolExceptionHandler;
        return this;
    }

    public NettyConnectionPoolBuilder withConnectionInfoProvider(ConnectionInfoProvider connectionInfoProvider)
    {
        this.connectionInfoProvider = connectionInfoProvider;
        return this;
    }

    public NettyConnectionPoolBuilder withContextExceptionHandler(ContextExceptionHandler contextExceptionHandler)
    {
        this.contextExceptionHandler = contextExceptionHandler;
        return this;
    }

    public NettyConnectionPoolBuilder withLeaseExpiredHandler(LeaseExpiredHandler leaseExpiredHandler)
    {
        this.leaseExpiredHandler = leaseExpiredHandler;
        return this;
    }

    public NettyConnectionPoolBuilder withPreGrantLease(PreGrantLease preGrantLease)
    {
        this.preGrantLease = preGrantLease;
        return this;
    }

    public NettyConnectionPoolBuilder withPreReturnToPool(PreReturnToPool preReturnToPool)
    {
        this.preReturnToPool = preReturnToPool;
        return this;
    }

    public NettyConnectionPoolBuilder withInboundHandlerName(String inboundHandlerName)
    {
        this.inboundHandlerName = inboundHandlerName;
        return this;
    }

    public NettyConnectionPoolBuilder withReaperIntervalMillis(int reaperInterval)
    {
        this.reaperIntervalMillis = reaperInterval;
        return this;
    }

    public NettyConnectionPool build()
    {
        if (connectionInfoProvider == null)
        {
            throw new IllegalStateException("No connection info provider.");
        }

        if (bootstrapProvider == null)
        {
            throw new IllegalArgumentException("No Bootstrap provider.");
        }


        //
        // Default exception handling is to close the offending context ungracefully.
        //
        if (contextExceptionHandler == null)
        {
            contextExceptionHandler = new ContextExceptionHandler()
            {
                @Override
                public boolean close(Throwable throwable, PoolProvider provider)
                {
                    return true;
                }
            };
        }

        //
        // Default action is to close the expired context.
        // If you don't want to have leases then set the time to 0 when you take out the lease.
        //
        if (leaseExpiredHandler == null)
        {
            leaseExpiredHandler = new LeaseExpiredHandler()
            {
                @Override
                public boolean closeExpiredLease(LeasedContext context, PoolProvider provider)
                {
                    return true;
                }
            };
        }

        //
        // Default action is to continue to grant the lease.
        //
        if (preGrantLease == null)
        {
            preGrantLease = new PreGrantLease()
            {
                @Override
                public boolean continueToGrantLease(Channel channel, PoolProvider provider, Object userObject)
                {
                    return true;
                }
            };
        }

        if (preReturnToPool == null)
        {
            preReturnToPool = new PreReturnToPool()
            {
                @Override
                public boolean returnToPoolOrDisposeNow(Channel context, PoolProvider provider, Object userObject)
                {
                    return true;
                }
            };
        }

        if (poolExceptionHandler == null)
        {
            poolExceptionHandler = new PoolExceptionHandler()
            {
                @Override
                public void handleException(Throwable th)
                {
                    th.printStackTrace();
                }
            };
        }

        if (postConnectEstablish == null)
        {
            postConnectEstablish = new PostConnectEstablish()
            {

                @Override
                public void establish(Channel channel, PoolProvider provider, Completion completion)
                {
                    completion.complete();
                }
            };

        }


        return new NettyConnectionPool(
            connectionInfoProvider,
            contextExceptionHandler,
            leaseExpiredHandler,
            preGrantLease,
            preReturnToPool,
            bootstrapProvider,
            poolExceptionHandler,
            leaseExpiryReaper,
            postConnectEstablish,
            immortalCount,
            maxEphemeralCount,
            ephemeralLifespanMillis,
            inboundHandlerName, reaperIntervalMillis);
    }
}
