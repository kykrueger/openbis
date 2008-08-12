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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;

/**
 * Data structure Version 1.1.
 * 
 * @author Christian Ribeaud
 */
public final class DataStructureV1_1 extends DataStructureV1_0
{
    private static final Version VERSION = new Version(1, 1);

    public DataStructureV1_1(final IStorage storage)
    {
        super(storage);
    }

    //
    // DataStructureV1_0
    //

    @Override
    public final Version getVersion()
    {
        return VERSION;
    }

    /**
     * Returns the sample.
     * <p>
     * For backward compatibility, loads a {@link Sample} when no
     * <code>SampleWithOwner.GROUP_CODE</code> node could be found in sample directory.
     * </p>
     * 
     * @throws DataStructureException if the sample hasn't be loaded nor hasn't be set by
     *             {@link #setSample(Sample)}.
     */
    @Override
    public final Sample getSample()
    {
        assertOpenOrCreated();
        final IDirectory metaDataDirectory = getMetaDataDirectory();
        final IDirectory sampleDir = metaDataDirectory.tryGetNode(Sample.FOLDER).tryAsDirectory();
        if (sampleDir.tryGetNode(SampleWithOwner.GROUP_CODE) == null)
        {
            return Sample.loadFrom(metaDataDirectory);
        }
        return SampleWithOwner.loadFrom(metaDataDirectory);
    }

    /**
     * Sets the sample. Overwrites an already set or loaded value.
     * 
     * @param sample Must be an instance of {@link SampleWithOwner}.
     */
    @Override
    public final void setSample(final Sample sample)
    {
        assert sample != null : "Unspecified sample.";
        assert sample instanceof SampleWithOwner : "Must be an instance of SampleWithOwner.";
        assertOpenOrCreated();
        ((SampleWithOwner) sample).saveTo(getMetaDataDirectory());
    }

}
