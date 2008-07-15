/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.process;

import java.util.concurrent.Callable;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.StopException;

/**
 * Executes given {@link Callable}.
 * 
 * @author Christian Ribeaud
 */
public final class CallableExecutor
{
    private final int maxInvocationsOnFailure;

    private final long millisToSleepOnFailure;

    public CallableExecutor()
    {
        this(Constants.MAXIMUM_INVOCATIONS_ON_FAILURE, Constants.MILLIS_TO_SLEEP_BEFORE_RETRYING);
    }

    public CallableExecutor(final int maxInvocationsOnFailure, final long millisToSleepOnFailure)
    {
        assert millisToSleepOnFailure > -1 : "Negative value";
        assert maxInvocationsOnFailure > -1 : "Negative value";
        this.maxInvocationsOnFailure = maxInvocationsOnFailure;
        this.millisToSleepOnFailure = millisToSleepOnFailure;
    }

    /**
     * Executes given <var>callable</var> until it returns a non-<code>null</code> value (or
     * until <code>maxInvocationsOnFailure</code> is reached).
     */
    public final <T> T executeCallable(final Callable<T> callable) throws StopException
    {
        int counter = 0;
        T result = null;
        try
        {
            do
            {
                StopException.check();
                result = callable.call();
                if (result == null)
                {
                    ++counter;
                    if (counter < maxInvocationsOnFailure && millisToSleepOnFailure > 0)
                    {
                        Thread.sleep(millisToSleepOnFailure);
                    }
                }
            } while (result == null && counter < maxInvocationsOnFailure);
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return result;
    }
}
