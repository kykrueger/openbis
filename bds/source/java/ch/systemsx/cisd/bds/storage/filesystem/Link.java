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

import org.apache.commons.lang.ObjectUtils;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.IFileBasedLink;
import ch.systemsx.cisd.bds.storage.IFileBasedNode;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * An <code>ILink</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
final class Link implements IFileBasedLink
{
    private final String name;

    private final IFileBasedNode reference;

    private IDirectory parent;

    Link(final String name, final IFileBasedNode reference)
    {
        assert name != null : "A name must be specified.";
        assert reference != null : "Reference can not be null.";
        assert reference.isValid() : "Given reference must be valid: " + reference;
        this.name = name;
        this.reference = reference;
    }

    /** Sets the parent of this {@link INode}. */
    @Override
    public final void setParent(final IDirectory parentOrNull)
    {
        parent = parentOrNull;
    }

    @Override
    public IDirectory tryAsDirectory()
    {
        return (reference instanceof IDirectory) ? (IDirectory) reference : null;
    }

    @Override
    public IFile tryAsFile()
    {
        return (reference instanceof IFile) ? (IFile) reference : null;
    }

    //
    // ILink
    //

    @Override
    public final String getName()
    {
        return name;
    }

    @Override
    public String getPath()
    {
        return parent.getPath() + "/" + getPath();
    }

    @Override
    public final IDirectory tryGetParent()
    {
        return parent;
    }

    @Override
    public final INode getReference()
    {
        return reference;
    }

    @Override
    public final void extractTo(final File directory)
    {
        reference.extractTo(directory);
    }

    @Override
    public final void moveTo(final File directory)
    {
        reference.moveTo(directory);
    }

    @Override
    public final boolean isValid()
    {
        if (reference.isValid() == false)
        {
            return false;
        }
        if (parent != null)
        {
            final INode node = parent.tryGetNode(name);
            return node != null && node.isValid();
        }
        return true;
    }

    @Override
    public File getNodeFile()
    {
        return reference.getNodeFile();
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getPath();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IFileBasedLink == false)
        {
            return false;
        }
        return ObjectUtils.equals(getPath(), ((IFileBasedLink) obj).getPath());
    }

    @Override
    public int hashCode()
    {
        return getPath().hashCode();
    }

}
