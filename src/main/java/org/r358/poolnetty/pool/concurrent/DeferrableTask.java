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

package org.r358.poolnetty.pool.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Task that can reschedule its execution.
 * Used primarily in lease granting where the defer() implementation puts the
 * lease request back into a holding deque.
 */
public abstract class DeferrableTask<V> implements Runnable
{
    protected V result = null;
    protected ExecutionException executionException = null;
    protected CountDownLatch latch = new CountDownLatch(1);
    protected boolean firstAttempt = true;

    public abstract void defer()
        throws Exception;

    public abstract boolean runOrDefer()
        throws Exception;

    @Override
    public void run()
    {
        try
        {
            if (runOrDefer())
            {
                defer();
                firstAttempt = false;
            }

        }
        catch (Exception ex)
        {
            executionException = new ExecutionException(ex);
            latch.countDown();
        }
    }

    protected void setResult(V result)
    {
        this.result = result;
        latch.countDown();
    }


    public V get()
        throws InterruptedException, ExecutionException
    {

        latch.await();
        if (executionException != null)
        {
            throw executionException;
        }

        return result;
    }


    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException
    {
        if (!latch.await(timeout, unit))
        {
            throw new TimeoutException("Get timed out.");
        }

        if (executionException != null)
        {
            throw executionException;
        }

        return result;
    }


}
