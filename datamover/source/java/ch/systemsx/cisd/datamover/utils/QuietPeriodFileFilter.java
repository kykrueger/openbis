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

package ch.systemsx.cisd.datamover.utils;

import java.io.FileFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * A {@link FileFilter} that picks all entries that haven't been changed for longer than some given quiet period.
 * <p>
 * Note that the system is designed to be robust when the clocks of the host that runs the datamover and the host that
 * writes the files are out of sync. To this end, we are <i>never<i> comparing times as provided by the datamover host
 * clock with last modification times. Instead we wait for some quiet period as defined solely by the clock of the
 * datamover host. After that time we require the last modification time to be not younger than the last time we checked
 * before we {@link #accept(StoreItem)} the path.
 * 
 * @author Bernd Rinn
 */
public class QuietPeriodFileFilter
{
    // @Private
    static final int CLEANUP_TO_QUIET_PERIOD_RATIO = 10;

    // @Private
    final static int MAX_CALLS_BEFORE_CLEANUP = 10000;

    private static final String TIME_FORMAT_TEMPLATE = "%#$tY-%#$tm-%#$td %#$tH:%#$tM:%#$tS";

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, QuietPeriodFileFilter.class);

    private final static Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, QuietPeriodFileFilter.class);

    private final long quietPeriodMillis;

    private final long cleanUpPeriodMillis;

    private final IFileStore fileStore;

    private final Map<StoreItem, PathCheckRecord> pathMap;

    private final ICurrentTimeProvider timeProvider;

    private int callCounter;

    /**
     * A value object that holds the information about the last check performed for a path.
     */
    private static final class PathCheckRecord
    {
        final private long timeChecked;

        final private long timeOfLastModification;

        PathCheckRecord(final long timeChecked, final long timeLastChanged)
        {
            this.timeChecked = timeChecked;
            this.timeOfLastModification = timeLastChanged;
        }

        /**
         * The time when the entry was checked.
         */
        public long getTimeChecked()
        {
            return timeChecked;
        }

        /**
         * The newest last modification time found during the check.
         */
        public long getTimeOfLastModification()
        {
            return timeOfLastModification;
        }
    }

    /**
     * Provider for the current time in milli-seconds since the Epoch. Overwrite this in unit-tests.
     */
    // @Private
    interface ICurrentTimeProvider
    {
        long getCurrentTimeMillis();
    }

    /**
     * Creates a <var>QuietPeriodFileFilter</var>.
     * 
     * @param fileStore Used to check when a pathname was changed.
     * @param timingParameters The timing parameter object to get the quiet period from.
     */
    public QuietPeriodFileFilter(IFileStore fileStore, ITimingParameters timingParameters)
    {
        this(fileStore, timingParameters, new ICurrentTimeProvider()
            {
                public long getCurrentTimeMillis()
                {
                    return System.currentTimeMillis();
                }
            });
    }

    /**
     * Creates a <var>QuietPeriodFileFilter</var>.
     * 
     * @param fileStore Used to check when a pathname was changed.
     * @param timingParameters The timing parameter object to get the quiet period from.
     * @param timeProvider A role that provides access to the current time.
     */
    public QuietPeriodFileFilter(IFileStore fileStore, ITimingParameters timingParameters,
            ICurrentTimeProvider timeProvider)
    {
        assert timingParameters != null;
        assert timingParameters.getQuietPeriodMillis() > 0;
        assert fileStore != null;
        assert timeProvider != null;

        this.quietPeriodMillis = timingParameters.getQuietPeriodMillis();
        this.cleanUpPeriodMillis = CLEANUP_TO_QUIET_PERIOD_RATIO * quietPeriodMillis;
        this.fileStore = fileStore;
        this.timeProvider = timeProvider;
        this.pathMap = new HashMap<StoreItem, PathCheckRecord>();
        this.callCounter = 0;
    }

    public boolean accept(StoreItem item)
    {
        final long now = timeProvider.getCurrentTimeMillis();
        try
        {
            final PathCheckRecord checkRecordOrNull = pathMap.get(item);
            if (checkRecordOrNull == null) // new item
            {
                pathMap.put(item, new PathCheckRecord(now, fileStore.lastChanged(item, 0L)));
                return false;
            }
            if (now - checkRecordOrNull.getTimeChecked() < quietPeriodMillis) // no need to check yet
            {
                return false;
            }
            final long oldLastChanged = checkRecordOrNull.getTimeOfLastModification();
            final long newLastChanged = fileStore.lastChanged(item, oldLastChanged);
            if (newLastChanged != oldLastChanged)
            {
                pathMap.put(item, new PathCheckRecord(now, newLastChanged));
                if (newLastChanged < oldLastChanged) // that is not supposed to happen
                {
                    machineLog.error(getClockProblemLogMessage(item.getName(), checkRecordOrNull
                            .getTimeChecked(), oldLastChanged, now, newLastChanged));
                }
                return false;
            }
            pathMap.remove(item);
            return true;
        } finally
        {
            if (++callCounter >= MAX_CALLS_BEFORE_CLEANUP)
            {
                cleanUpVanishedPaths(now);
                callCounter = 0;
            }
        }
    }

    private void cleanUpVanishedPaths(long now)
    {
        final Iterator<Map.Entry<StoreItem, PathCheckRecord>> iter = pathMap.entrySet().iterator();
        while(iter.hasNext())
        {
            final Map.Entry<StoreItem, PathCheckRecord> entry = iter.next();
            final long timeSinceLastCheckMillis = now - entry.getValue().getTimeChecked();
            if (timeSinceLastCheckMillis > cleanUpPeriodMillis)
            {
                final StoreItem item = entry.getKey();
                operationLog.warn("Path '" + item.getName() + "' hasn't been checked for "
                        + (timeSinceLastCheckMillis / 1000.0) + " s, removing from path map.");
                iter.remove();
            }
        }
    }

    // @Private
    static String getClockProblemLogMessage(final String pathname, final long oldCheck, final long oldLastChanged,
            final long newCheck, final long newLastChanged)
    {
        return String.format("Last modification time of path '%1$s' jumped back: check at " + getTimeFormat(2)
                + " -> last modification time " + getTimeFormat(3) + ", check at later time " + getTimeFormat(4)
                + " -> last modification time " + getTimeFormat(5) + " (which is %6$d ms younger)", pathname, oldCheck,
                oldLastChanged, newCheck, newLastChanged, (oldLastChanged - newLastChanged));
    }

    // @Private
    static String getTimeFormat(int position)
    {
        return StringUtils.replace(TIME_FORMAT_TEMPLATE, "#", Integer.toString(position));
    }

}
