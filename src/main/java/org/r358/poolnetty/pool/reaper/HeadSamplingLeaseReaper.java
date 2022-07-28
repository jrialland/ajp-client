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
 * As leases get older they will migrate to the head of the current leases list as other leases are yielded.
 * This reaper starts checking at the head of the list and stops when the first
 * unexpired lease is found.
 */
public class HeadSamplingLeaseReaper implements LeaseExpiryReaper
{
    @Override
    public List<LeasedContext> reapHarvest(List<LeasedContext> currentLeases)
    {

        ArrayList<LeasedContext> out = new ArrayList<>();

        long zeit = System.currentTimeMillis();

        for (LeasedContext lc : currentLeases)
        {
            if (lc.expiredLease(zeit))
            {
                out.add(lc);
                continue;
            }
            break;
        }


        return out;
    }
}
