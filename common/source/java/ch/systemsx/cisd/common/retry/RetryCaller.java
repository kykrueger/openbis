/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.retry;

import org.springframework.remoting.RemoteConnectFailureException;

/**
 * @author pkupczyk
 */
public abstract class RetryCaller<T, E extends Throwable>
{

    private int retryCounter = 1;

    private int retryMaxCounter = 5;

    private int retryWaitingTime = 1000;

    private int retryWaitingTimeFactor = 2;

    protected abstract T call() throws E;

    public T callWithRetry() throws E
    {
        while (true)
        {
            try
            {
                T result = call();
                return result;
            } catch (RemoteConnectFailureException e)
            {
                if (shouldRetry())
                {
                    System.err.println("Call failed - will retry");
                    waitForRetry();
                } else
                {
                    System.err.println("Call failed - will NOT retry");
                    throw e;
                }
            }
        }
    }

    private boolean shouldRetry()
    {
        return retryCounter < retryMaxCounter;
    }

    private void waitForRetry()
    {
        try
        {
            Thread.sleep(retryWaitingTime);
            retryWaitingTime *= retryWaitingTimeFactor;
            retryCounter++;
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    public int getRetryMaxCounter()
    {
        return retryMaxCounter;
    }

    public void setRetryMaxCounter(int retryMaxCounter)
    {
        this.retryMaxCounter = retryMaxCounter;
    }

    public int getRetryWaitingTime()
    {
        return retryWaitingTime;
    }

    public void setRetryWaitingTime(int retryWaitingTime)
    {
        this.retryWaitingTime = retryWaitingTime;
    }

    public int getRetryWaitingTimeFactor()
    {
        return retryWaitingTimeFactor;
    }

    public void setRetryWaitingTimeFactor(int retryWaitingTimeFactor)
    {
        this.retryWaitingTimeFactor = retryWaitingTimeFactor;
    }

}
