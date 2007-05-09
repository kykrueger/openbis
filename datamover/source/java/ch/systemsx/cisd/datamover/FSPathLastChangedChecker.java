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

package ch.systemsx.cisd.datamover;

import java.io.File;

import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Checks for last change time of a directory in the file system.
 * 
 * @author Bernd Rinn
 */
public class FSPathLastChangedChecker implements IPathLastChangedChecker
{

    /**
     * @return The time when any file below <var>directory</var> has last been changed in the file system.
     * @throws ch.systemsx.cisd.common.exceptions.EnvironmentFailureException If the <var>directory</var> does not
     *             exist, is not readable, or is not a directory.
     */
    public long lastChanged(File path)
    {
        return FileUtilities.lastChanged(path);
    }

}
