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

package org.r358.poolnetty.pool.reaper;

import org.r358.poolnetty.common.LeaseExpiryReaper;
import org.r358.poolnetty.common.LeasedContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Very simple reaper that considers the whole lease list in one pass, this won't scale well.
 */
public class FullPassSimpleLeaseReaper
    implements LeaseExpiryReaper
{
    @Override
    public List<LeasedContext> reapHarvest(List<LeasedContext> currentLeases)
    {
        long zeit = System.currentTimeMillis();
        List<LeasedContext> toBeExpired = null;
        for (LeasedContext lc : currentLeases)
        {
            if (lc.expiredLease(zeit))
            {
                if (toBeExpired == null)
                {
                    toBeExpired = new ArrayList<>();
                }

                toBeExpired.add(lc);

            }
        }

        return toBeExpired;
    }
}
