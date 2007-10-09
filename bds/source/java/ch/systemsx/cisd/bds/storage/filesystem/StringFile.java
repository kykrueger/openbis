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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;

import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class StringFile extends AbstractNode implements IFile<String>
{
    StringFile(File file)
    {
        super(file);
        assert file.isFile() : "Not a file " + file.getAbsolutePath();
    }
    
    public String getValue()
    {
        return FileUtilities.loadToString(fileNode);
    }

    public void extractTo(File directory) throws UserFailureException, EnvironmentFailureException
    {
        // TODO Auto-generated method stub
    }

}
