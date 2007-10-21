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

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
class Directory extends AbstractNode implements IDirectory
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, AbstractNode.class);

    private static final Log4jSimpleLogger errorLogger = new Log4jSimpleLogger(Level.ERROR, operationLog);

    Directory(java.io.File directory)
    {
        super(directory);
        if (directory.isDirectory() == false)
        {
            throw new UserFailureException("Not a directory: " + directory.getAbsolutePath());
        }
    }

    private final static void moveFileToDirectory(final java.io.File source, final java.io.File directory)
            throws EnvironmentFailureException
    {
        assert source != null;
        assert directory != null && directory.isDirectory();
        final java.io.File destination = new java.io.File(directory, source.getName());
        if (destination.exists() == false)
        {
            final boolean successful = source.renameTo(destination);
            if (successful == false)
            {
                throw EnvironmentFailureException.fromTemplate("Couldn't not move file '%s' to directory '%s'.", source
                        .getAbsolutePath(), directory.getAbsolutePath());
            }
        } else
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("Destination file '%s' already exists. Will not overwrite", destination
                        .getAbsolutePath()));
            }
        }
    }

    //
    // IDirectory
    //

    public INode tryToGetNode(String name)
    {
        final java.io.File[] files = FileUtilities.listFiles(nodeFile);
        for (java.io.File file : files)
        {
            if (file.getName().equals(name))
            {
                return NodeFactory.createNode(file);
            }
        }
        return null;
    }

    public IDirectory makeDirectory(String name)
    {
        java.io.File dir = new java.io.File(nodeFile, name);
        if (dir.exists())
        {
            if (dir.isDirectory() == false)
            {
                throw new UserFailureException("There already exists a file named '" + name + "' in directory " + this);
            }
            return new Directory(dir);
        }
        boolean successful = dir.mkdir();
        if (successful == false)
        {
            throw new EnvironmentFailureException("Couldn't create directory " + dir.getAbsolutePath()
                    + " for some unknown reason.");
        }
        return new Directory(dir);
    }

    public IFile addKeyValuePair(String key, String value)
    {
        java.io.File file = new java.io.File(nodeFile, key);
        FileUtilities.writeToFile(file, value);
        return new File(file);
    }

    public INode addFile(final java.io.File file, final boolean move) throws UserFailureException,
            EnvironmentFailureException
    {
        final java.io.File newFile = new java.io.File(nodeFile, file.getName());
        if (move)
        {
            moveFileToDirectory(file, nodeFile);
        } else
        {
            try
            {
                if (file.isDirectory())
                {
                    FileUtils.copyDirectory(file, newFile);
                } else
                {
                    FileUtils.copyFile(file, newFile);
                }
            } catch (IOException ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex, "Couldn't not copy file '%s' to directory '%s'.",
                        file, nodeFile.getAbsolutePath());
            }
        }
        return NodeFactory.createNode(newFile);
    }

    public ILink addLink(String name, INode node)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator<INode> iterator()
    {
        return new Iterator<INode>()
            {
                private java.io.File[] files = FileUtilities.listFiles(nodeFile);

                private int index;

                //
                // Iterator
                //

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

                public INode next()
                {
                    return index >= files.length ? null : NodeFactory.createNode(files[index++]);
                }

                public boolean hasNext()
                {
                    return index < files.length;
                }
            };
    }

    public final void extractTo(final java.io.File directory) throws EnvironmentFailureException
    {
        assert directory != null : "Directory could not be null";
        // ...but might not exist
        try
        {
            FileUtils.copyDirectoryToDirectory(nodeFile, directory);
        } catch (IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Couldn't copy directory '%s' to directory '%s'.",
                    nodeFile.getAbsolutePath(), directory.getAbsolutePath());
        }
    }

    public final void removeNode(final INode node) throws UserFailureException, EnvironmentFailureException
    {
        assert node != null : "Node could not be null";
        AbstractNode abstractNode = (AbstractNode) node;
        final java.io.File file = abstractNode.nodeFile;
        if (file.isDirectory())
        {
            if (FileUtilities.deleteRecursively(file, errorLogger) == false)
            {
                throw EnvironmentFailureException.fromTemplate("Couldn't remove directory '%s'.", file
                        .getAbsolutePath());
            }
        } else if (file.isFile())
        {
            if (file.delete() == false)
            {
                throw EnvironmentFailureException.fromTemplate("Couldn't remove file '%s'.", file.getAbsolutePath());
            }
        }
    }
}
