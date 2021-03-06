/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common;

import java.util.Properties;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * A class that provides timing parameters for operations that can timeout or otherwise fail.
 * 
 * @author Bernd Rinn
 */
public class TimingParameters
{

    private final static TimingParameters standardParameters =
            new TimingParameters(Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT,
                    Constants.MAXIMUM_RETRY_COUNT, Constants.MILLIS_TO_SLEEP_BEFORE_RETRYING);

    private final static TimingParameters defaultParameters =
            new TimingParameters(standardParameters);

    private final static TimingParameters noTimeoutNoRetriesParameters =
            new TimingParameters(Long.MAX_VALUE, 0, 0L);

    /**
     * Returns the standard timing parameters (may not be overwritten).
     */
    public static final TimingParameters getStandardParameters()
    {
        return standardParameters;
    }

    /**
     * Returns the default timing parameters.
     */
    public static final TimingParameters getDefaultParameters()
    {
        return defaultParameters;
    }

    /**
     * Returns timing parameters that corresponds to 'no timeout', 'no retries'.
     */
    public static final TimingParameters getNoTimeoutNoRetriesParameters()
    {
        return noTimeoutNoRetriesParameters;
    }

    public static final String TIMEOUT_PROPERTY = "timeout";

    public static final String MAX_RETRY_PROPERTY = "max-retries";

    public static final String FAILURE_INTERVAL = "failure-interval";

    private volatile long timeoutMillis;

    private volatile int maxRetriesOnFailure;

    private volatile long intervalToWaitAfterFailureMillis;

    public static TimingParameters create(final Properties properties)
    {
        return new TimingParameters(properties);
    }

    /**
     * Returns <code>true</code> if the <var>properties</var> contain timing parameters and
     * <code>false</code> otherwise.
     */
    public static boolean hasTimingParameters(Properties properties)
    {
        return properties.containsKey(TIMEOUT_PROPERTY)
                || properties.containsKey(MAX_RETRY_PROPERTY)
                || properties.containsKey(FAILURE_INTERVAL);
    }

    /**
     * Sets the new default.
     */
    public static void setDefault(final long timeoutMillis, final int maxRetriesOnFailure,
            final long intervalToWaitAfterFailureMillis)
    {
        defaultParameters.timeoutMillis = timeoutMillis;
        defaultParameters.maxRetriesOnFailure = maxRetriesOnFailure;
        defaultParameters.intervalToWaitAfterFailureMillis = intervalToWaitAfterFailureMillis;
    }

    /**
     * Sets the new default.
     */
    public static void setDefault(final TimingParameters timingParameters)
    {
        defaultParameters.timeoutMillis = timingParameters.timeoutMillis;
        defaultParameters.maxRetriesOnFailure = timingParameters.maxRetriesOnFailure;
        defaultParameters.intervalToWaitAfterFailureMillis =
                timingParameters.intervalToWaitAfterFailureMillis;
    }

    /**
     * Sets the new default from the properties (or fall back to standard values, if
     * <var>properties</var> do not contain values).
     */
    public static void setDefault(Properties properties)
    {
        defaultParameters.timeoutMillis = getTimeoutMillis(properties);
        defaultParameters.maxRetriesOnFailure = getMaxRetriesOnFailure(properties);
        defaultParameters.intervalToWaitAfterFailureMillis =
                getIntervalToWaitAfterFailureMillis(properties);
    }

    private static long getIntervalToWaitAfterFailureMillis(Properties properties)
    {
        return PropertyUtils.getInt(properties, FAILURE_INTERVAL,
                Constants.INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS)
                * DateUtils.MILLIS_PER_SECOND;
    }

    private static int getMaxRetriesOnFailure(Properties properties)
    {
        return PropertyUtils.getInt(properties, MAX_RETRY_PROPERTY, Constants.MAXIMUM_RETRY_COUNT);
    }

    private static long getTimeoutMillis(Properties properties)
    {
        final long timeoutSpecified =
                PropertyUtils.getInt(properties, TIMEOUT_PROPERTY, Constants.TIMEOUT_SECONDS)
                        * DateUtils.MILLIS_PER_SECOND;
        return (timeoutSpecified == 0) ? Long.MAX_VALUE : timeoutSpecified;
    }

    public static TimingParameters create(final long timeoutMillis, final int maxRetriesOnFailure,
            final long intervalToWaitAfterFailureMillis)
    {
        return new TimingParameters(timeoutMillis, maxRetriesOnFailure,
                intervalToWaitAfterFailureMillis);
    }

    public static TimingParameters create(final long timeoutMillis)
    {
        return new TimingParameters(timeoutMillis, Constants.MAXIMUM_RETRY_COUNT,
                Constants.MILLIS_TO_SLEEP_BEFORE_RETRYING);
    }

    public static TimingParameters createNoRetries(final long timeoutMillis)
    {
        return new TimingParameters(timeoutMillis, 0, 0L);
    }

    private TimingParameters(TimingParameters parameters)
    {
        this.timeoutMillis = parameters.timeoutMillis;
        this.maxRetriesOnFailure = parameters.maxRetriesOnFailure;
        this.intervalToWaitAfterFailureMillis = parameters.intervalToWaitAfterFailureMillis;
    }

    private TimingParameters(final long timeoutMillis, final int maxRetriesOnFailure,
            final long intervalToWaitAfterFailureMillis)
    {
        this.timeoutMillis = timeoutMillis;
        this.maxRetriesOnFailure = maxRetriesOnFailure;
        this.intervalToWaitAfterFailureMillis = intervalToWaitAfterFailureMillis;
    }

    private TimingParameters(Properties properties)
    {
        this.timeoutMillis = getTimeoutMillis(properties);
        this.maxRetriesOnFailure = getMaxRetriesOnFailure(properties);
        this.intervalToWaitAfterFailureMillis = getIntervalToWaitAfterFailureMillis(properties);
    }

    /**
     * Returns the timeout (in milli-seconds).
     */
    public long getTimeoutMillis()
    {
        return timeoutMillis;
    }

    /**
     * Returns the maximal number of retries when an operation fails.
     */
    public int getMaxRetriesOnFailure()
    {
        return maxRetriesOnFailure;
    }

    /**
     * Returns the time (in milli-seconds) to wait after a failure has occurred and before retrying
     * the operation.
     */
    public long getIntervalToWaitAfterFailureMillis()
    {
        return intervalToWaitAfterFailureMillis;
    }

    //
    // Object
    //

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof TimingParameters == false)
        {
            return false;
        }
        final TimingParameters that = (TimingParameters) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.timeoutMillis, that.timeoutMillis);
        builder.append(this.maxRetriesOnFailure, that.maxRetriesOnFailure);
        builder
                .append(this.intervalToWaitAfterFailureMillis,
                        that.intervalToWaitAfterFailureMillis);
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(timeoutMillis);
        builder.append(maxRetriesOnFailure);
        builder.append(intervalToWaitAfterFailureMillis);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return String.format("Timing: timeout: %d s, maximal retries: %d, sleep on failure: %d s",
                timeoutMillis / 1000, maxRetriesOnFailure, intervalToWaitAfterFailureMillis / 1000);
    }

}
