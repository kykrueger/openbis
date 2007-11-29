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

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.storage.StorageException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * An abstract implementation of <code>INode</code>.
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractNode implements INode
{
    protected final static void moveFileToDirectory(final File source, final File directory)
            throws EnvironmentFailureException
    {
        assert source != null;
        assert directory != null && directory.isDirectory();
        final File destination = new File(directory, source.getName());
        if (destination.exists() == false)
        {
            final boolean successful = source.renameTo(destination);
            if (successful == false)
            {
                throw EnvironmentFailureException.fromTemplate("Couldn't not move file '%s' to directory '%s'.", source
                        .getAbsolutePath(), directory.getAbsolutePath());
            }
        }
    }

    protected final File nodeFile;

    AbstractNode(File file)
    {
        checkFile(file);
        this.nodeFile = file;
    }

    static void checkFile(final File file)
    {
        if (file == null)
        {
            throw new StorageException("Unspecified file.");
        }
        if (file.exists() == false)
        {
            throw new StorageException(String.format("Non existing file '%s'.", file.getAbsolutePath()));
        }
    }

    //
    // INode
    //

    public final String getName()
    {
        return nodeFile.getName();
    }

    public final IDirectory tryToGetParent()
    {
        File dir = nodeFile.getParentFile();
        return dir == null ? null : new Directory(dir);
    }

    public final void moveTo(final File directory)
    {
        moveFileToDirectory(nodeFile, directory);
    }

    public boolean isValid()
    {
        return nodeFile.exists();
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return nodeFile.getAbsolutePath();
    }
}
