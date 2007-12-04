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

package ch.systemsx.cisd.bds.hcs;

import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * An <code>AbstractGeometry</code> implementation suitable for <i>Well</i>.
 * 
 * @author Christian Ribeaud
 */
public final class WellGeometry extends Geometry
{

    /**
     * Directory name which contains the well geometry.
     * <p>
     * Also used as unique identifier when used as {@link FormatParameter}.
     * </p>
     */
    public static final String WELL_GEOMETRY = "well_geometry";

    public WellGeometry(final int rows, final int columns)
    {
        super(rows, columns);
    }

    /**
     * Loads the geometry from the specified directory.
     */
    final static Geometry loadFrom(final IDirectory directory)
    {
        return loadFrom(directory, WELL_GEOMETRY);
    }

    //
    // Geometry
    //

    @Override
    protected final String getGeometryDirectoryName()
    {
        return WELL_GEOMETRY;
    }

}
