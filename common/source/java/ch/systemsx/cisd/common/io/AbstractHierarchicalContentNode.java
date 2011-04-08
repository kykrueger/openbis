/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * Abstract {@link IHierarchicalContent} implementation with checks before calling methods specific
 * to directories or non-directories.
 * 
 * @author Piotr Buczek
 */
abstract class AbstractHierarchicalContentNode implements IHierarchicalContentNode
{
    static final String OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY =
            "Operation not supported for a directory";

    static final String OPERATION_SUPPORTED_ONLY_FOR_A_DIRECTORY =
            "Operation supported only for a directory";

    /** Returns list of child nodes of a node known to be a directory. */
    abstract List<IHierarchicalContentNode> doGetChildNodes();

    /** Returns size of a node known NOT to be a directory. */
    abstract long doGetSize();

    /** Returns {@link IRandomAccessFile} of a node known NOT to be a directory. */
    abstract IRandomAccessFile doGetFileContent();

    /** Returns {@link InputStream} of a node known NOT to be a directory. */
    abstract InputStream doGetInputStream();

    private final void requireDirectory()
    {
        if (isDirectory() == false)
        {
            throw new UnsupportedOperationException(OPERATION_SUPPORTED_ONLY_FOR_A_DIRECTORY);
        }
    }

    private final void failOnDirectory()
    {
        if (isDirectory())
        {
            throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY);
        }
    }

    public final List<IHierarchicalContentNode> getChildNodes()
    {
        requireDirectory();
        return doGetChildNodes();
    }

    public final long getSize() throws UnsupportedOperationException
    {
        failOnDirectory();
        return doGetSize();
    }

    public final IRandomAccessFile getFileContent()
    {
        failOnDirectory();
        return doGetFileContent();
    }

    public final InputStream getInputStream()
    {
        failOnDirectory();
        return doGetInputStream();
    }

}
