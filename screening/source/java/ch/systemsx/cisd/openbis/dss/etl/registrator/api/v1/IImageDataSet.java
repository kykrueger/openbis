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

package ch.systemsx.cisd.openbis.dss.etl.registrator.api.v1;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;

/**
 * Image specific extension of generic {@link IDataSet}.
 *
 * @author Franz-Josef Elmer
 */
public interface IImageDataSet extends IDataSet
{
    /**
     * Sets the tile geometry by specified number of rows and columns.
     */
    public void setTileGeometry(int numberOfRows, int numberOfColumns);
}
