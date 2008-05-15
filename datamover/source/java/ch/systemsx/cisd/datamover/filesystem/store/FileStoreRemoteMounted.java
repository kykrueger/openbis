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

package ch.systemsx.cisd.datamover.filesystem.store;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;

/**
 * A <code>FileStore</code> extension for remote paths mounted.
 * <p>
 * The works is mainly delegated to an internal {@link FileStoreLocal}.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class FileStoreRemoteMounted extends FileStore
{
    private final FileStoreLocal localImpl;

    public FileStoreRemoteMounted(final FileWithHighwaterMark file, final String desription,
            final IFileSysOperationsFactory factory)
    {
        super(file, null, true, desription, factory);
        this.localImpl = new FileStoreLocal(file, desription, factory);
    }

    //
    // FileStore
    //

    @Override
    public final IExtendedFileStore tryAsExtended()
    {
        return null;
    }

    @Override
    public final IStoreCopier getCopier(final FileStore destinationDirectory)
    {
        final boolean requiresDeletion = false;
        return constructStoreCopier(destinationDirectory, requiresDeletion);
    }

    @Override
    public final String toString()
    {
        final String pathStr = getPath().getPath();
        return "[mounted remote fs]" + pathStr;
    }

    @Override
    public final String getLocationDescription(final StoreItem item)
    {
        return localImpl.getLocationDescription(item);
    }

    @Override
    public final Status delete(final StoreItem item)
    {
        return localImpl.delete(item);
    }

    @Override
    public final boolean exists(final StoreItem item)
    {
        return localImpl.exists(item);
    }

    @Override
    public final long lastChanged(final StoreItem item, final long stopWhenFindYounger)
    {
        return localImpl.lastChanged(item, stopWhenFindYounger);
    }

    @Override
    public final long lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        return localImpl.lastChangedRelative(item, stopWhenFindYoungerRelative);
    }

    @Override
    public final String tryCheckDirectoryFullyAccessible(final long timeOutMillis)
    {
        return localImpl.tryCheckDirectoryFullyAccessible(timeOutMillis);
    }

    @Override
    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        return localImpl.tryListSortByLastModified(loggerOrNull);
    }

    @Override
    public final void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        localImpl.check();
    }
}
