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

package ch.systemsx.cisd.bds;

/**
 * Immutable class defining a reference between to paths.
 * 
 * @author Franz-Josef Elmer
 */
public final class Reference
{
    private final String path;

    private final String originalPath;

    private final ReferenceType referenceType;

    /**
     * Creates an instance for the specified paths and reference type.
     * 
     * @param path Path which refers to <code>originalPath</code>.
     * @param originalPath Path to which <code>path</code> refers. This can be <code>null</code>.
     * @param referenceType Type of reference.
     */
    public Reference(final String path, final String originalPath, final ReferenceType referenceType)
    {
        assert path != null : "Path can not be null.";
        assert originalPath != null : "Original path can not be null.";
        this.path = path;
        this.originalPath = originalPath;
        this.referenceType = referenceType;
    }

    /**
     * Returns the original path.
     */
    public final String getOriginalPath()
    {
        return originalPath;
    }

    /**
     * Returns the reference path.
     */
    public final String getPath()
    {
        return path;
    }

    /**
     * Returns the type of reference.
     */
    public final ReferenceType getReferenceType()
    {
        return referenceType;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Reference == false)
        {
            return false;
        }
        final Reference that = (Reference) obj;
        return that.path.equals(path);
    }

    @Override
    public final int hashCode()
    {
        return path.hashCode();
    }

    @Override
    public final String toString()
    {
        return getPath();
    }
}
