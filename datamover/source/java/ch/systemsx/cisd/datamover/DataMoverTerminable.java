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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.CompoundTerminable;
import ch.systemsx.cisd.common.utilities.ITerminable;

/**
 * The <i>DataMover</i> specific {@link ITerminable} implementation.
 * 
 * @author Christian Ribeaud
 */
final class DataMoverTerminable extends CompoundTerminable
{

    public static final String SHUTDOWN_MARKER_FILENAME = Constants.MARKER_PREFIX + "shutdown";
    private final File outgoingTargetLocationFile;

    DataMoverTerminable(final File locationFile, final DataMoverProcess... dataMoverProcesses)
    {
        super(dataMoverProcesses);
        if (locationFile == null)
        {
            throw new IllegalArgumentException("Unspecified outgoing target location file.");
        }
        this.outgoingTargetLocationFile = locationFile;
    }

    private final static void createMarkerFile(final File markerFile)
    {
        try
        {
            FileUtils.touch(markerFile);
        } catch (final IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Can not create marker file '%s'.",
                    markerFile.getAbsolutePath());
        }
    }

    private final static void deleteMarkerFile(final File markerFile)
    {
        final boolean deleted = markerFile.delete();
        if (deleted == false)
        {
            throw EnvironmentFailureException.fromTemplate("Can not delete marker file '%s'.",
                    markerFile.getAbsolutePath());
        }
    }

    //
    // CompoundTerminable
    //

    @Override
    public final boolean terminate()
    {
        final File markerFile = new File(SHUTDOWN_MARKER_FILENAME);
        createMarkerFile(markerFile);
        final boolean terminate = super.terminate();
        deleteMarkerFile(markerFile);
        outgoingTargetLocationFile.delete();
        return terminate;
    }
}
