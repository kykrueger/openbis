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

package ch.systemsx.cisd.datamover.filesystem.intf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.StoreItem;

/**
 * The abstract super-class of classes that represent a file store.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public abstract class FileStore implements IFileStore
{
    protected final File path;

    protected final String hostOrNull;

    protected final String kind;

    protected final boolean remote;

    protected final IFileSysOperationsFactory factory;

    protected FileStore(final File path, final String hostOrNull, final boolean remote,
            final String kind, final IFileSysOperationsFactory factory)
    {
        assert path != null;
        assert kind != null;
        this.path = path;
        this.kind = kind;
        this.hostOrNull = hostOrNull;
        this.remote = remote;
        this.factory = factory;
    }

    protected final File getPath()
    {
        return path;
    }

    protected final String tryGetHost()
    {
        return hostOrNull;
    }

    protected final String getDescription()
    {
        return kind;
    }

    protected final File getChildFile(final StoreItem item)
    {
        return new File(path, item.getName());
    }

    // does not take into account the fact, that the destination cannot be overwritten and must be
    // deleted beforehand
    protected final IStoreCopier constructStoreCopier(final FileStore destinationDirectory,
            final boolean requiresDeletionBeforeCreation)
    {
        final IPathCopier copier = factory.getCopier(requiresDeletionBeforeCreation);
        final String srcHostOrNull = hostOrNull;
        final String destHostOrNull = destinationDirectory.hostOrNull;
        final File destPath = destinationDirectory.path;
        return new IStoreCopier()
            {
                public Status copy(final StoreItem item)
                {
                    final File srcItem = getChildFile(item);
                    if (srcHostOrNull == null)
                    {
                        if (destHostOrNull == null)
                        {
                            return copier.copy(srcItem, destPath);
                        } else
                        {
                            return copier.copyToRemote(srcItem, destPath, destHostOrNull);
                        }
                    } else
                    {
                        assert destHostOrNull == null;
                        return copier.copyFromRemote(srcItem, srcHostOrNull, destPath);
                    }
                }

                public boolean terminate()
                {
                    return copier.terminate();
                }
            };
    }

    /**
     * Returns <code>true</code>, if the file store resides on a remote computer and
     * <code>false</code> otherwise.
     * <p>
     * Note that even if this method returns <code>true</code> paths on this file system might be
     * reached via local file system operation, if the remote file system is provided as a remote
     * share and mounted via NFS or CIFS.
     */
    public final boolean isRemote()
    {
        return remote;
    }

    public boolean isParentDirectory(final IFileStore child)
    {
        if (child instanceof FileStore == false)
        {
            return false;
        }
        final FileStore potentialChild = (FileStore) child;
        return StringUtils.equals(hostOrNull, potentialChild.hostOrNull)
                && getCanonicalPath(potentialChild.path).startsWith(getCanonicalPath(path));
    }

    private String getCanonicalPath(final File file)
    {
        if (hostOrNull != null)
        {
            return file.getPath();
        }
        try
        {
            return file.getCanonicalPath() + File.separator;
        } catch (final IOException e)
        {
            throw EnvironmentFailureException.fromTemplate(e,
                    "Cannot determine canonical form of path '%s'", file.getPath());
        }
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof FileStore == false)
        {
            return false;
        }
        final FileStore that = (FileStore) obj;
        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(hostOrNull, that.hostOrNull);
        equalsBuilder.append(kind, that.kind);
        equalsBuilder.append(path, that.path);
        return equalsBuilder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(hostOrNull);
        builder.append(kind);
        builder.append(path);
        return builder.toHashCode();
    }

    // -------------------

    /**
     * Checks whether this store is a directory and is fully accessible to the program.
     * 
     * @param timeOutMillis The time (in milli-seconds) to wait for the target to become available
     *            if it is not initially.
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error
     *         message describing the problem with the <var>directory</var> otherwise.
     */
    public abstract String tryCheckDirectoryFullyAccessible(final long timeOutMillis);

    public abstract boolean exists(StoreItem item);

    /**
     * Returns the last time when there was a write access to <var>item</var>.
     * 
     * @param item The {@link StoreItem} to check.
     * @param stopWhenFindYounger If &gt; 0, the recursive search for younger file will be stopped
     *            when a file or directory is found that is younger than the time specified in this
     *            parameter. Supposed to be used when one does not care about the absolutely
     *            youngest entry, but only, if there are entries that are "young enough".
     * @return The time (in milliseconds since the start of the epoch) when <var>resource</var> was
     *         last changed.
     */
    public abstract long lastChanged(StoreItem item, long stopWhenFindYounger);

    /**
     * Returns the last time when there was a write access to <var>item</var>.
     * 
     * @param item The {@link StoreItem} to check.
     * @param stopWhenFindYoungerRelative If &gt; 0, the recursive search for younger file will be
     *            stopped when a file or directory is found that is younger than
     *            <code>System.currentTimeMillis() - stopWhenYoungerRelative</code>.
     * @return The time (in milliseconds since the start of the epoch) when <var>resource</var> was
     *         last changed.
     */
    public abstract long lastChangedRelative(StoreItem item, long stopWhenFindYoungerRelative);

    /**
     * List files in the scanned store. Sort in order of "oldest first".
     */
    public abstract StoreItem[] tryListSortByLastModified(ISimpleLogger loggerOrNull);

    public abstract Status delete(StoreItem item);

    /**
     * @param destinationDirectory The directory to use as a destination in the copy operation. It
     *            must be readable and writable. Copier will override the destination item if it
     *            already exists.
     */
    public abstract IStoreCopier getCopier(FileStore destinationDirectory);

    // returned description should give the user the idea about file location. You should not use
    // the result for
    // something else than printing it for user. It should not be especially assumed that the result
    // is the path
    // which could be used in java.io.File constructor.
    public abstract String getLocationDescription(StoreItem item);

    public abstract IExtendedFileStore tryAsExtended();

    public static abstract class ExtendedFileStore extends FileStore implements IExtendedFileStore
    {
        protected ExtendedFileStore(final File path, final String hostOrNull, final boolean remote,
                final String kind, final IFileSysOperationsFactory factory)
        {
            super(path, hostOrNull, remote, kind, factory);
        }

        public abstract boolean createNewFile(StoreItem item);

        public abstract File tryMoveLocal(StoreItem sourceItem, File destinationDir,
                String newFilePrefix);
    }
}