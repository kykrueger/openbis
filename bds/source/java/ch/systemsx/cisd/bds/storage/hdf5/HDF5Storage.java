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

package ch.systemsx.cisd.bds.storage.hdf5;

import java.io.File;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;

/**
 * Storage based on HDF5.
 * 
 * @author Franz-Josef Elmer
 */
public class HDF5Storage implements IStorage
{
    public HDF5Storage(final File hdf5File)
    {
        assert hdf5File != null : "Unspecified HDF5 file.";
    }

    //
    // IStorage
    //

    @Override
    public final boolean isMounted()
    {
        throw new NotImplementedException();
    }

    @Override
    public final IDirectory getRoot()
    {
        throw new NotImplementedException();
    }

    @Override
    public final void mount()
    {
        throw new NotImplementedException();
    }

    @Override
    public final void unmount()
    {
        throw new NotImplementedException();
    }

}
