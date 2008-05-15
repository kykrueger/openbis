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

package ch.systemsx.cisd.common.highwatermark;

import java.io.File;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * A high water mark watcher.
 * <p>
 * This class is thread-safe.
 * </p>
 * 
 * @see FileSystemUtils
 * @author Christian Ribeaud
 */
public final class HighwaterMarkWatcher implements Runnable
{

    private final static IFreeSpaceProvider DEFAULT_FREE_SPACE_PROVIDER =
            new DefaultFreeSpaceProvider();

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HighwaterMarkWatcher.class);

    /**
     * The high water mark value in <i>kilobytes</i>.
     */
    private final long highwaterMarkInKb;

    private final EventListenerList listenerList = new EventListenerList();

    /** The path to get free space for. Is not <code>null</code>, not empty on Unix. */
    private File path;

    /** The last {@link HighwaterMarkState} computed. */
    private HighwaterMarkState highwaterMarkState;

    private final IFreeSpaceProvider freeSpaceProvider;

    /**
     * @param highwaterMarkInKb the high water mark value in kilobytes. If negative, then
     *            {@link #run()} always returns without doing anything.
     */
    public HighwaterMarkWatcher(final long highwaterMarkInKb)
    {
        this(highwaterMarkInKb, DEFAULT_FREE_SPACE_PROVIDER);
    }

    public HighwaterMarkWatcher(final long highwaterMarkInKb,
            final IFreeSpaceProvider freeSpaceProvider)
    {
        assert freeSpaceProvider != null : "Unspecified IFreeSpaceProvider";
        this.highwaterMarkInKb = highwaterMarkInKb;
        this.freeSpaceProvider = freeSpaceProvider;
        addChangeListener(new NotificationLogChangeListener());
    }

    private final void fireChangeListeners(final HighwaterMarkEvent highwaterMarkEvent)
    {
        final ChangeListener[] listeners = listenerList.getListeners(ChangeListener.class);
        for (final ChangeListener changeListener : listeners)
        {
            changeListener.stateChanged(highwaterMarkEvent);
        }
    }

    public final static String displayKilobyteValue(final long value)
    {
        return FileUtils.byteCountToDisplaySize(value * FileUtils.ONE_KB);
    }

    public final static boolean isBelow(final HighwaterMarkState highwaterMarkState)
    {
        assert highwaterMarkState != null : "Unspecified WatermarkState";
        return highwaterMarkState.freeSpace < highwaterMarkState.getHighwaterMark();
    }

    /**
     * Adds a <code>ChangeListener</code> to this high water mark watcher.
     */
    public final synchronized void addChangeListener(final ChangeListener changeListener)
    {
        assert changeListener != null : "Unspecified ChangeListener";
        listenerList.add(ChangeListener.class, changeListener);
    }

    /**
     * Removes given <code>ChangeListener</code> from this high water mark watcher.
     */
    public final synchronized void removeChangeListener(final ChangeListener changeListener)
    {
        assert changeListener != null : "Unspecified ChangeListener";
        listenerList.remove(ChangeListener.class, changeListener);
    }

    /**
     * Whether the free space is below the high water mark or not.
     */
    public final synchronized boolean isBelow()
    {
        return highwaterMarkState == null ? false : isBelow(highwaterMarkState);
    }

    /**
     * Sets the path to get free space for.
     * <p>
     * The path must be set before calling {@link #run()}.
     * </p>
     */
    public final synchronized void setPath(final File path)
    {
        this.path = path;
    }

    /**
     * Sets the path and calls {@link #run()} at the same time.
     */
    public final synchronized void setPathAndRun(final File path)
    {
        setPath(path);
        run();
    }

    /**
     * Analyzes given <var>path</var> and returns a {@link HighwaterMarkState}.
     */
    public final HighwaterMarkState getHighwaterMarkState(final File file) throws IOException
    {
        assert file != null : "Unspecified file";
        final long freeSpaceInKb = freeSpaceProvider.freeSpaceKb(file);
        return new HighwaterMarkState(new FileWithHighwaterMark(file, highwaterMarkInKb),
                freeSpaceInKb);
    }

    /**
     * Returns the high water mark (in <i>kilobytes</i>) specified in the constructor.
     */
    public final long getHighwaterMark()
    {
        return highwaterMarkInKb;
    }

    //
    // Runnable
    //

    public final synchronized void run()
    {
        assert path != null : "Unspecified path";
        if (highwaterMarkInKb < 0)
        {
            return;
        }
        try
        {
            final HighwaterMarkState state = getHighwaterMarkState(path);
            final boolean newBelowValue = isBelow(state);
            final boolean stateChanged = isBelow() != newBelowValue;
            highwaterMarkState = state;
            if (stateChanged)
            {
                fireChangeListeners(new HighwaterMarkEvent(this, state));
            }
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Amount of available space on '%s' is: %s.",
                        state.fileWithHighwaterMark.getCanonicalPath(),
                        displayKilobyteValue(state.freeSpace)));
            }
        } catch (final IOException ex)
        {
            operationLog.error(
                    "The high water mark watcher can not work properly due to an I/O exception.",
                    ex);
        }
    }

    //
    // Helper classes
    //

    private final static class DefaultFreeSpaceProvider implements IFreeSpaceProvider
    {

        //
        // IFreeSpaceProvider
        //

        public final long freeSpaceKb(final File path) throws IOException
        {
            final String canonicalPath = FileUtilities.getCanonicalPath(path);
            return FileSystemUtils.freeSpaceKb(canonicalPath);
        }
    }

    public final static class HighwaterMarkState
    {
        private final FileWithHighwaterMark fileWithHighwaterMark;

        private final long freeSpace;

        HighwaterMarkState(final FileWithHighwaterMark fileWithHighwaterMark, final long freeSpace)
        {
            this.fileWithHighwaterMark = fileWithHighwaterMark;
            this.freeSpace = freeSpace;
        }

        public final File getPath()
        {
            return fileWithHighwaterMark.getFile();
        }

        /** Returns the free space (in <i>kilobytes</i>). */
        public final long getFreeSpace()
        {
            return freeSpace;
        }

        /** Returns the high water mark (in <i>kilobytes</i>). */
        public final long getHighwaterMark()
        {
            return fileWithHighwaterMark.getHighwaterMark();
        }

    }

    public final static class HighwaterMarkEvent extends ChangeEvent
    {

        private static final long serialVersionUID = 1L;

        private final HighwaterMarkState highwaterMarkState;

        HighwaterMarkEvent(final Object source, final HighwaterMarkState highwaterMarkState)
        {
            super(source);
            this.highwaterMarkState = highwaterMarkState;
        }

        /**
         * Whether the free space is below or reaches the high water mark.
         */
        public final boolean isBelow()
        {
            return HighwaterMarkWatcher.isBelow(highwaterMarkState);
        }

        /** Returns the canonical path. */
        public final String getPath()
        {
            return highwaterMarkState.fileWithHighwaterMark.getCanonicalPath();
        }

        public final long getFreeSpace()
        {
            return highwaterMarkState.freeSpace;
        }

        public final long getHighwaterMark()
        {
            return highwaterMarkState.fileWithHighwaterMark.getHighwaterMark();
        }
    }

    /**
     * A <code>ChangeListener</code> implementation which informs the administrator when free
     * space becomes tight or when free space is again "green".
     * 
     * @author Christian Ribeaud
     */
    final static class NotificationLogChangeListener implements ChangeListener
    {
        static final String INFO_LOG_FORMAT =
                "The amount of available space (%s) on '%s' "
                        + "is again sufficient (greater than the specified high water mark: %s).";

        static final String WARNING_LOG_FORMAT =
                "The amount of available space (%s) on '%s' "
                        + "is lower than the specified high water mark (%s).";

        private static final Logger notificationLog =
                LogFactory.getLogger(LogCategory.NOTIFY, NotificationLogChangeListener.class);

        NotificationLogChangeListener()
        {
        }

        //
        // ChangeListener
        //

        public final void stateChanged(final ChangeEvent e)
        {
            final HighwaterMarkEvent event = (HighwaterMarkEvent) e;
            final String path = event.getPath();
            final String highwaterMarkDisplayed = displayKilobyteValue(event.getHighwaterMark());
            final String freeSpaceDisplayed = displayKilobyteValue(event.getFreeSpace());
            if (event.isBelow())
            {
                notificationLog.warn(String.format(WARNING_LOG_FORMAT, freeSpaceDisplayed, path,
                        highwaterMarkDisplayed));
            } else
            {
                notificationLog.info(String.format(INFO_LOG_FORMAT, freeSpaceDisplayed, path,
                        highwaterMarkDisplayed));
            }
        }
    }

    /**
     * Each implementation is able to return the free space on a drive or volume.
     * 
     * @author Christian Ribeaud
     */
    public static interface IFreeSpaceProvider
    {

        /**
         * Returns the free space on a drive or volume in kilobytes by invoking the command line.
         */
        public long freeSpaceKb(final File path) throws IOException;
    }
}