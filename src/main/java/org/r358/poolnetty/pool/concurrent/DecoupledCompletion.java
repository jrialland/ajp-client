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

import org.r358.poolnetty.common.concurrent.Completion;

import java.util.concurrent.Executor;

/**
 * A decoupled completion, extend this to have onComplete() called on the executor of your choice.
 */
public abstract class DecoupledCompletion implements Completion
{

    private final Executor decoupler;

    /**
     * Create defining the executor.
     *
     * @param decoupler
     */
    protected DecoupledCompletion(Executor decoupler)
    {

        this.decoupler = decoupler;
    }

    @Override
    public void complete()
    {
        decoupler.execute(new Runnable()
        {
            @Override
            public void run()
            {
                onComplete();
            }
        });
    }

    protected abstract void onComplete();
}
