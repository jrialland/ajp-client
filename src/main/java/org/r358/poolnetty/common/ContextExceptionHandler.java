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
 * <p>>Called when a netty channel handler context has thrown an exception, implementations of this interface must determine if the connection pool is to dispose of the channel by closing it if possible.</p>
 * <p>Generally if the context is emitting exceptions its probably broken, or the input is not be handled correctly, keeping a failing context in the pool may open up other subtle issues.</p>
 */
public interface ContextExceptionHandler
{
    /**
     * Close because of throwable.
     *
     * @param throwable The throwable.
     * @param provider  The pool provider.
     * @return true to close, false to leave in pool.
     */
    boolean close(Throwable throwable, PoolProvider provider);


}
