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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.r358.poolnetty.common.*;
import org.r358.poolnetty.common.exceptions.PoolProviderException;
import org.r358.poolnetty.pool.concurrent.DecoupledCompletion;
import org.r358.poolnetty.pool.concurrent.DeferrableTask;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty Connection Pool.
 * <p>Notes about threading:</p>
 * <p>
 * <ol>
 * <li>Uses single threaded executor and all operations on fields in this class are done on that executor.</li>
 * <li>The addition and removal of listeners exists outside the executor and uses a CopyOnWriteArraySet.</li>
 * </ol>
 * </p>
 */
public class NettyConnectionPool
    implements PoolProvider
{

    /**
     * Task decoupler.
     */
    private final ScheduledExecutorService decoupler = Executors.newSingleThreadScheduledExecutor();

    protected final ConnectionInfoProvider connectionInfoProvider;
    protected final ContextExceptionHandler contextExceptionHandler;
    protected final LeaseExpiredHandler leaseExpiredHandler;
    protected final PreGrantLease preGrantLease;
    protected final PreReturnToPool preReturnToPool;
    protected final BootstrapProvider bootstrapProvider;
    protected final PoolExceptionHandler poolExceptionHandler;
    protected final LeaseExpiryReaper leaseExpiryReaper;
    protected final PostConnectEstablish postConnectEstablish;

    protected final int immortalCount;
    protected final int maxEphemeralCount;
    protected final int ephemeralLifespanMillis;
    protected final String inboundHandlerName;
    protected final int reaperIntervalMillis;


    /**
     * Maps the channel to is current carrier.
     */
    protected final Map<Channel, Object> contextToCarrier = new HashMap<>();

    /**
     * List of leased contexts.
     */
    protected final List<LeasedContext> leasedContexts = new ArrayList<>();
    /**
     * Set of leased contexts (fasting searching in yield operation).
     */
    protected final Set<LeasedContext> leasedContextSet = new HashSet<>();

    /**
     * Listeners.
     */
    protected final CopyOnWriteArraySet<PoolProviderListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * List of lease required tasks that could not be full filled without blocking the decoupler.
     */
    protected final Deque<ObtainLease> leasesRequired = new ArrayDeque<>();

    /**
     * List of contexts that are immortal and do not age out.
     */
    protected final List<AvailableChannel> immortalContexts = new ArrayList<>();

    /**
     * List of contexts that are ephemeral.
     */
    protected final List<AvailableChannel> ephemeralContexts = new ArrayList<>();

    /**
     * List of connections that are still being opened and created.
     */
    protected final List<OpenConnection> connectionsInProgress = new ArrayList<>();


    /**
     * Stop leases from being granted.
     */
    protected boolean noNewLeases = false;

    /**
     * Lease counter, used to give unique id to LeaseChannels. (See note on threading.)
     */
    protected long leaseIdCounter = 0;


    protected NettyConnectionPool(
        ConnectionInfoProvider connectionInfoProvider,
        ContextExceptionHandler contextExceptionHandler,
        LeaseExpiredHandler leaseExpiredHandler,
        PreGrantLease preGrantLease,
        PreReturnToPool preReturnToPool,
        BootstrapProvider bootstrapProvider,
        PoolExceptionHandler poolExceptionHandler,
        LeaseExpiryReaper leaseExpiryReaper,
        PostConnectEstablish postConnectEstablish,
        int immortalCount,
        int maxEphemeralCount,
        int ephemeralLifespanMillis, String inboundHandlerName, int reaperIntervalMillis)
    {
        this.connectionInfoProvider = connectionInfoProvider;
        this.contextExceptionHandler = contextExceptionHandler;
        this.leaseExpiredHandler = leaseExpiredHandler;
        this.preGrantLease = preGrantLease;
        this.preReturnToPool = preReturnToPool;
        this.bootstrapProvider = bootstrapProvider;
        this.poolExceptionHandler = poolExceptionHandler;
        this.leaseExpiryReaper = leaseExpiryReaper;
        this.postConnectEstablish = postConnectEstablish;
        this.immortalCount = immortalCount;
        this.maxEphemeralCount = maxEphemeralCount;
        this.ephemeralLifespanMillis = ephemeralLifespanMillis;
        this.inboundHandlerName = inboundHandlerName;
        this.reaperIntervalMillis = reaperIntervalMillis;

    }

    @Override
    public Future<LeasedChannel> leaseAsync(int time, TimeUnit units, Object userObject)
    {
        return leaseAsync(time, units, userObject, null);
    }

    @Override
    public Future<LeasedChannel> leaseAsync(int time, TimeUnit units, Object userObject, LeaseListener listener)
    {
        final ObtainLease ol = new ObtainLease(time, units, userObject);

        final LeaseFuture future = ol.getLeaseFuture(listener);

        fireLeaseRequested(time, units, userObject);

        NettyConnectionPool.this.execute(ol);

        return future;
    }

    @Override
    public LeasedChannel lease(final int time, final TimeUnit units, final Object userObject)
        throws PoolProviderException
    {

        final ObtainLease ol = new ObtainLease(time, units, userObject);

        fireLeaseRequested(time, units, userObject);
        NettyConnectionPool.this.execute(ol);

        try
        {
            return new LeasedChannel(ol.get(), ol.get().getChannel(), NettyConnectionPool.this, userObject);
            //return new LeasedChannel(ch, this, userObject);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new PoolProviderException("Interrupted: " + e.getMessage(), e);
        }
        catch (ExecutionException e)
        {
            throw new PoolProviderException("Execution Failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void yield(Channel ch)
        throws PoolProviderException
    {
        final Channel channel;
        if (ch instanceof LeasedChannel)
        {
            channel = ((LeasedChannel)ch).getInner();
        }
        else
        {
            channel = ch;
        }


        NettyConnectionPool.this.execute(new Runnable()
        {
            @Override
            public void run()
            {


                Object carrier = contextToCarrier.get(channel);


                if (carrier instanceof LeasedContext)
                {
                    leasedContextSet.remove(carrier);
                    leasedContexts.remove(carrier);

                    AvailableChannel ac = null;

                    if (((LeasedContext)carrier).isImmortal())
                    {
                        ac = new AvailableChannel(-1, ((LeasedContext)carrier).getChannel(), -1, true, null);
                        immortalContexts.add(ac);
                    }
                    else
                    {
                        int lifespan = ((LeasedContext)carrier).getChannelLifespan();
                        EphemeralReaper reaper = new EphemeralReaper(lifespan);
                        ac = new AvailableChannel(System.currentTimeMillis() + lifespan, ((LeasedContext)carrier).getChannel(), lifespan, false, decoupler.schedule(reaper, lifespan, TimeUnit.MILLISECONDS));
                        reaper.context = ac;

                        ephemeralContexts.add(ac);
                    }

                    contextToCarrier.put(channel, ac);

                    if (noNewLeases && leasedContexts.isEmpty())
                    {
                        NettyConnectionPool.this.execute(new ShutdownTask());
                    }
                    else
                    {
                        pollNextRequestOntoDecoupler();
                    }

                    fireLeaseYield(NettyConnectionPool.this, channel, ((LeasedContext)carrier).getUserObject());
                }
                else if (carrier instanceof AvailableChannel)
                {
                    poolExceptionHandler.handleException(new PoolProviderException("Context is not out on lease."));
                }
                else
                {
                    poolExceptionHandler.handleException(new PoolProviderException("Unknown channel, has the lease expired?"));
                }
            }
        });
    }

    @Override
    public boolean start(long timeToWait, TimeUnit timeUnit)
        throws Exception
    {

        CountDownLatch countDownLatch = new CountDownLatch(immortalCount);

        for (int t = 0; t < immortalCount; t++)
        {
            NettyConnectionPool.this.execute(new OpenConnection(false, null, countDownLatch));
        }


        NettyConnectionPool.this.execute(new Runnable()
        {
            @Override
            public void run()
            {
                setupHarvester();
                fireStarted();
            }
        });

        return countDownLatch.await(timeToWait, timeUnit);
    }

    /**
     * stop the pool.
     *
     * @param force true to force shutdown immediately.
     */
    @Override
    public void stop(final boolean force)
    {

        NettyConnectionPool.this.execute(new Runnable()
        {
            @Override
            public void run()
            {
                noNewLeases = true;

                if (force || leasedContexts.isEmpty())
                {
                    new ShutdownTask().run();
                }


            }
        });

    }

    @Override
    public synchronized void execute(Runnable runnable)
    {
        if (decoupler.isShutdown() || decoupler.isTerminated())
        {
            return;
        }

        decoupler.execute(runnable);
    }

    @Override
    public void addListener(PoolProviderListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void removeListener(PoolProviderListener listener)
    {
        listeners.remove(listener);
    }


    /**
     * Set up the expiry harvester.
     */
    private void setupHarvester()
    {
        //
        // A basic recurring process to trigger harvesting of expired leases.
        //
        decoupler.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                List<LeasedContext> toBeExpired = leaseExpiryReaper.reapHarvest(leasedContexts);

                //
                // Go through to be expired list, check they are actually leased and apply the leaseExpiryHandler to each.
                //
                if (toBeExpired != null && !toBeExpired.isEmpty())
                {
                    for (LeasedContext lc : toBeExpired)
                    {
                        if (leasedContextSet.contains(lc))
                        {
                            //
                            // Notify lease expired.
                            //
                            fireLeaseExpired(NettyConnectionPool.this, lc.getChannel(), lc.getUserObject());

                            lc.fireExpired();


                            if (leaseExpiredHandler.closeExpiredLease(lc, NettyConnectionPool.this))
                            {


                                //
                                // Force closure of the context if handler directs it.
                                //

                                leasedContexts.remove(lc);
                                leasedContextSet.remove(lc);
                                NettyConnectionPool.this.execute(new CloseContext(lc.getChannel()));

                                //
                                // As we are closing an immortal connection we need to add a new one to replace it.
                                //
                                if (lc.isImmortal())
                                {
                                    NettyConnectionPool.this.execute(new OpenConnection(false, null));
                                }

                            }
                        }
                        else
                        {
                            poolExceptionHandler.handleException(new IllegalStateException("LeasedContext from harvester not found."));
                        }

                    }
                }

            }
        }, reaperIntervalMillis, reaperIntervalMillis, TimeUnit.MILLISECONDS);
    }


    /**
     * Apply pre-lease to a list of free contexts and grab the first one that passes.
     *
     * @param contextList The list of AvailableContexts.
     * @return An AvailableChannel object or null if none were found.
     */
    private AvailableChannel applyPreLease(List<AvailableChannel> contextList, Object userObject)
    {

        if (!contextList.isEmpty())
        {
            int c = contextList.size();

            while (--c >= 0)
            {
                AvailableChannel ac = contextList.remove(0);
                if (preGrantLease.continueToGrantLease(ac.getChannel(), NettyConnectionPool.this, userObject))
                {
                    return ac;
                }
                else
                {
                    contextList.add(ac);
                    // Append what we cannot lease to the end with the assumption that the
                    // next in the list will able to be leased.
                }

            }
        }

        return null;
    }


    protected void fireStarted()
    {
        for (PoolProviderListener l : listeners)
        {
            l.started(this);
        }
    }

    protected void fireStopped()
    {
        for (PoolProviderListener l : listeners)
        {
            l.stopped(this);
        }
    }

    protected void fireLeaseRequested(int leaseTime, TimeUnit units, Object userObject)
    {
        for (PoolProviderListener l : listeners)
        {
            l.leaseRequested(this, leaseTime, units, userObject);
        }
    }

    protected void fireLeaseGranted(PoolProvider provider, Channel channel, Object userObject)
    {
        for (PoolProviderListener l : listeners)
        {
            l.leaseGranted(this, channel, userObject);
        }
    }

    protected void fireLeaseCanceled(Object userObject)
    {
        for (PoolProviderListener l : listeners)
        {
            l.leaseCanceled(this, userObject);
        }
    }

    protected void fireLeaseYield(PoolProvider provider, Channel channel, Object userObject)
    {
        for (PoolProviderListener l : listeners)
        {
            l.leaseYield(this, channel, userObject);
        }
    }

    protected void fireLeaseExpired(PoolProvider provider, Channel channel, Object userObject)
    {
        for (PoolProviderListener l : listeners)
        {
            l.leaseExpired(this, channel, userObject);
        }
    }

    protected void fireConnectionClosed(Channel ctx)
    {
        for (PoolProviderListener l : listeners)
        {
            l.connectionClosed(this, ctx);
        }
    }

    protected void fireConnectionCreated(Channel ctx, boolean immortal)
    {
        for (PoolProviderListener l : listeners)
        {
            l.connectionCreated(this, ctx, immortal);
        }
    }

    private void fireEphemeralReaped(Channel ctx)
    {
        for (PoolProviderListener l : listeners)
        {
            l.ephemeralReaped(this, ctx);
        }
    }


    /**
     * Traverse the list of ephemeral channels and reap any that have expired.
     */
    private class EphemeralReaper
        implements Runnable
    {

        private final int lifespan;

        private AvailableChannel context;

        private EphemeralReaper(int lifespan)
        {
            this.lifespan = lifespan;
        }

        @Override
        public void run()
        {
            if (context != null)
            {
                ephemeralContexts.remove(context);

                fireEphemeralReaped(context.getChannel());
                NettyConnectionPool.this.execute(new CloseContext(context.getChannel()));
            }
        }
    }


    /**
     * Open a connection.
     */
    private class OpenConnection
        implements Runnable
    {


        private final boolean ephemeral;
        private final ObtainLease obtainLease;
        private final CountDownLatch startUpLatch;

        private OpenConnection(boolean ephemeral, ObtainLease obtainLease)
        {
            this.obtainLease = obtainLease;
            this.ephemeral = ephemeral;
            startUpLatch = null;
        }

        private OpenConnection(boolean ephemeral, ObtainLease obtainLease, CountDownLatch startUpLatch)
        {
            this.ephemeral = ephemeral;
            this.obtainLease = obtainLease;
            this.startUpLatch = startUpLatch;
        }

        @Override
        public void run()
        {
            Bootstrap bs = bootstrapProvider.createBootstrap(NettyConnectionPool.this);
            final ConnectionInfo ci = connectionInfoProvider.connectionInfo(NettyConnectionPool.this);

            final ChannelInitializer initializer = ci.getChannelInitializer();
            bs.handler(initializer);

            try
            {
                bs.connect(ci.getRemoteSocketAddress(), ci.getLocalSocketAddress()).sync().addListener(new ChannelFutureListener()
                {
                    @Override
                    public void operationComplete(final ChannelFuture future)
                        throws Exception
                    {


                        //
                        // Put result back on the decoupler.
                        //
                        NettyConnectionPool.this.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {


                                if (future.isDone() && future.isSuccess())
                                {
                                    //
                                    //TODO check if there is a way to listen without adding a handler..
                                    //
                                    future.channel().pipeline().addLast(inboundHandlerName, new SimpleChannelInboundHandler()
                                    {

                                        @Override
                                        public void channelWritabilityChanged(ChannelHandlerContext ctx)
                                            throws Exception
                                        {
                                            super.channelWritabilityChanged(ctx);
                                            NettyConnectionPool.this.execute(new WritabilityChanged(ctx.channel()));
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                            throws Exception
                                        {
                                            super.exceptionCaught(ctx, cause);
                                            if (contextExceptionHandler.close(cause, NettyConnectionPool.this))
                                            {
                                                NettyConnectionPool.this.execute(new CloseContext(ctx.channel()));
                                            }
                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx)
                                            throws Exception
                                        {
                                            super.channelInactive(ctx);
                                            NettyConnectionPool.this.execute(new ChannelInactive(ctx.channel()));
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Object msg)
                                            throws Exception
                                        {
                                            ctx.fireChannelRead(msg);
                                        }
                                    });

                                    final Channel ctc = future.channel();


                                    //
                                    // Do post connect establish phase.
                                    //


                                    postConnectEstablish.establish(ctc, NettyConnectionPool.this, new DecoupledCompletion(decoupler)
                                    {
                                        @Override
                                        protected void onComplete()
                                        {

                                            AvailableChannel ac = null;
                                            if (ephemeral)
                                            {
                                                EphemeralReaper reaper = new EphemeralReaper(ephemeralLifespanMillis);
                                                ac = new AvailableChannel(
                                                    System.currentTimeMillis() + ephemeralLifespanMillis,
                                                    ctc,
                                                    ephemeralLifespanMillis,
                                                    true,
                                                    decoupler.schedule(reaper, ephemeralLifespanMillis, TimeUnit.MILLISECONDS));

                                                reaper.context = ac;

                                                ephemeralContexts.add(ac);
                                                fireConnectionCreated(ctc, true);
                                            }
                                            else
                                            {
                                                ac = new AvailableChannel(-1, ctc, -1, false, null);

                                                immortalContexts.add(ac);
                                                fireConnectionCreated(ctc, false);
                                            }
                                            contextToCarrier.put(ctc, ac);

                                            //
                                            // If there are requests pending that are off the decoupler then
                                            // put the first one in the deque back on the decoupler before exiting.
                                            //

                                            connectionsInProgress.remove(OpenConnection.this);


                                            if (obtainLease != null)
                                            {
                                                obtainLease.run();
                                            }
                                            else
                                            {
                                                pollNextRequestOntoDecoupler();
                                            }

                                            if (startUpLatch != null)
                                            {
                                                startUpLatch.countDown();
                                            }

                                        }
                                    });


                                }
                                else
                                {
                                    //
                                    // Connection opening failed.
                                    //
                                    connectionsInProgress.remove(OpenConnection.this);
                                    if (obtainLease != null)
                                    {
                                        leasesRequired.add(obtainLease); // Put it back on the request list.
                                    }
                                    if (startUpLatch != null)
                                    {
                                        startUpLatch.countDown();
                                    }
                                }


                            }
                        });

                    }
                });


//

            }
            catch (Exception iex)
            {
                //TODO decide if we interrupt the decoupler.. probably not.
                poolExceptionHandler.handleException(iex);
            }
        }
    }

    /**
     * Obtain a lease deferrable task.
     */
    private class ObtainLease
        extends DeferrableTask<LeasedContext>
    {
        private final long leaseTime;
        private final TimeUnit units;
        private final Object userObject;
        private LeaseFuture leaseFuture;

        public ObtainLease(long time, TimeUnit units, Object userObject)
        {
            this.leaseTime = time;
            this.units = units;
            this.userObject = userObject;

        }


        @Override
        public void defer()
            throws Exception
        {
            leasesRequired.addLast(this); // Others in queue, place at end.
        }

        @Override
        public boolean runOrDefer()
            throws Exception
        {

            if (leaseFuture != null && leaseFuture.isCancelled())
            {
                fireLeaseCanceled(userObject);
                return false;
            }


            if (noNewLeases)
            {
                IllegalArgumentException ilex = new IllegalArgumentException("Pool is shutting down.");

                if (leaseFuture != null)
                {
                    leaseFuture.setError(ilex);
                }
                throw ilex;
            }


            //
            // Check this is not stepping in front of older requests.
            //
            if (firstAttempt && !leasesRequired.isEmpty())
            {
                return true;             // Defer this till later.
            }

            //
            // Can we satisfy this immediately
            //

            AvailableChannel ac = applyPreLease(immortalContexts, userObject);   // From immortals.

            if (ac == null)
            {
                ac = applyPreLease(ephemeralContexts, userObject); // From ephemeral.
            }


            if (ac != null)
            {
                if (leaseFuture != null)
                {
                    leaseFuture.blockLeaseCancel();
                }
                ac.cancelReaper();


                //
                // Has it expired.
                //
                if (ac.expired(System.currentTimeMillis()))
                {
                    ac.getChannel().close();
                    NettyConnectionPool.this.execute(new CloseContext(ac.getChannel()));
                }
                else
                {
                    LeasedContext lc = new LeasedContext(
                        leaseIdCounter++,
                        System.currentTimeMillis() + units.toMillis(leaseTime),
                        ac.getChannel(),
                        !ac.isImmortal(),
                        userObject, ac.getLifespan()
                    );

                    leasedContexts.add(lc);
                    leasedContextSet.add(lc);
                    contextToCarrier.put(lc.getChannel(), lc);


                    //
                    // We got a lease the next one might too.
                    //
                    pollNextRequestOntoDecoupler();


                    setResult(lc);
                    fireLeaseGranted(NettyConnectionPool.this, lc.getChannel(), userObject);

                    //
                    // Fire the future.
                    //
                    if (leaseFuture != null)
                    {
                        leaseFuture.setValue(new LeasedChannel(lc, lc.getChannel(), NettyConnectionPool.this, userObject));
                    }

                    return false; // Lease granted.
                }
            }

            //
            // Can we request creation of another another connection.
            //
            if (ephemeralContexts.size() < maxEphemeralCount - connectionsInProgress.size())
            {
                OpenConnection oc = new OpenConnection(true, this);
                connectionsInProgress.add(oc);
                NettyConnectionPool.this.execute(oc);
                return false; // At this point the
            }


            //
            // Handle cancel.
            //
            if (leaseFuture != null && leaseFuture.isCancelled())
            {
                fireLeaseCanceled(userObject);

                //
                // Schedule the immediate yielding of the channel of the canceled lease.
                //
                NettyConnectionPool.this.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            NettyConnectionPool.this.yield(leaseFuture.channel);

                        }
                        catch (PoolProviderException e)
                        {
                            poolExceptionHandler.handleException(e);
                        }
                    }
                });

                return false;
            }


            return true;
        }


        protected LeaseFuture getLeaseFuture(LeaseListener listener)
        {
            leaseFuture = new LeaseFuture(this, listener);
            return leaseFuture;
        }
    }


    private void pollNextRequestOntoDecoupler()
    {
        if (leasesRequired.isEmpty())
        {
            return;
        }

        NettyConnectionPool.this.execute(leasesRequired.pollFirst());
    }


    /**
     * Called when writability changes.
     */
    private class WritabilityChanged
        implements Runnable
    {

        private final Channel channel;

        public WritabilityChanged(Channel ctx)
        {
            this.channel = ctx;
        }


        @Override
        public void run()
        {
            //TODO This could be elaborated upon.


            if (!channel.isActive() || !channel.isOpen() || channel.isWritable())
            {
                NettyConnectionPool.this.execute(new CloseContext(channel));
            }
        }
    }


    /**
     * Close a channel task
     */
    private class CloseContext
        implements Runnable
    {
        private final Channel ctx;

        public CloseContext(Channel ctx)
        {

            this.ctx = ctx;
        }

        @Override
        public void run()
        {
            Object o = contextToCarrier.remove(ctx);
            if (o instanceof AvailableChannel)
            {
                immortalContexts.remove(o);
                ephemeralContexts.remove(o);
            }
            else
            {
                leasedContexts.remove(o);
                leasedContextSet.remove(o);
            }


            if (ctx.isOpen())
            {
                ctx.close().syncUninterruptibly().addListener(new ChannelFutureListener()
                {
                    @Override
                    public void operationComplete(ChannelFuture future)
                        throws Exception
                    {
                        fireConnectionClosed(ctx);
                    }
                });
            }
            else
            {
                fireConnectionClosed(ctx);
            }


        }
    }


    /**
     * Called when the channel becomes inactive..
     */
    private class ChannelInactive
        implements Runnable
    {
        private final Channel ctx;

        public ChannelInactive(Channel ctx)
        {
            this.ctx = ctx;
        }

        @Override
        public void run()
        {
            NettyConnectionPool.this.execute(new CloseContext(ctx));
        }
    }

    private class ShutdownTask
        implements Runnable
    {
        @Override
        public void run()
        {

            for (LeasedContext lc : leasedContexts)
            {
                lc.getChannel().close();
            }

            for (AvailableChannel ac : immortalContexts)
            {

                try
                {
                    ac.getChannel().close();
                    fireConnectionClosed(ac.getChannel());
                }
                catch (Exception ex)
                {
                    poolExceptionHandler.handleException(ex);
                }
            }

            for (AvailableChannel ac : ephemeralContexts)
            {

                try
                {
                    ac.getChannel().close();
                    fireConnectionClosed(ac.getChannel());
                }
                catch (Exception ex)
                {
                    poolExceptionHandler.handleException(ex);
                }
            }

            decoupler.shutdownNow();
            fireStopped();

        }
    }

    /**
     * A future for lease requests.
     */
    private class LeaseFuture
        implements Future<LeasedChannel>
    {

        private final AtomicBoolean cancel = new AtomicBoolean(false);
        private final CountDownLatch latch = new CountDownLatch(1);
        private final ObtainLease obtainLease;
        private final LeaseListener leaseListener;
        private final AtomicBoolean blockLeaseCancel = new AtomicBoolean(false);

        private boolean success = false;
        private Throwable throwable = null;
        private LeasedChannel channel = null;


        private LeaseFuture(ObtainLease obtainLease, LeaseListener leaseListener)
        {
            this.obtainLease = obtainLease;
            this.leaseListener = leaseListener;
        }

        @Override
        public synchronized boolean cancel(boolean mayInterruptIfRunning)
        {
            if (blockLeaseCancel.get())
            {
                throw new IllegalStateException("Lease has been granted.");
            }

            cancel.set(true);
            if (leaseListener != null)
            {
                leaseListener.leaseRequest(false, channel, new PoolProviderException("Lease request canceled."));
            }


            NettyConnectionPool.this.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    if (leasesRequired.remove(obtainLease))
                    {
                        fireLeaseCanceled(obtainLease.userObject);
                    }

                }
            });

            return true;
        }

        protected void setValue(LeasedChannel lc)
        {
            success = true;
            channel = lc;
            latch.countDown();

            if (leaseListener != null)
            {
                leaseListener.leaseRequest(true, channel, throwable);
            }
        }

        protected void setError(Throwable throwable)
        {
            success = false;
            this.throwable = throwable;
            latch.countDown();

            if (leaseListener != null)
            {
                leaseListener.leaseRequest(false, null, throwable);
            }

        }


        @Override
        public boolean isCancelled()
        {
            return cancel.get();
        }

        @Override
        public boolean isDone()
        {
            return latch.getCount() == 0;
        }

        @Override
        public LeasedChannel get()
            throws InterruptedException, ExecutionException
        {
            latch.await();
            if (success)
            {
                return channel;
            }

            throw new ExecutionException(throwable);
        }

        @Override
        public LeasedChannel get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException
        {

            if (latch.await(timeout, unit))
            {
                if (success)
                {
                    return channel;
                }

                throw new ExecutionException(throwable);
            }

            throw new TimeoutException("Lease request timed out.");
        }

        public void blockLeaseCancel()
        {
            blockLeaseCancel.set(true);
        }
    }
}
