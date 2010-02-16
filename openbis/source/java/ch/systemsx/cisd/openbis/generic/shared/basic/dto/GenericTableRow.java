/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;

/**
 * The values of a row of a {@link GenericTableResultSet}.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericTableRow implements Serializable, IsSerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ISerializableComparable[] cells;

    /**
     * Creates an instance for specified cells.
     */
    public GenericTableRow(ISerializableComparable... cells)
    {
        this.cells = cells;
    }

    /**
     * Returns the value of cell with specified index.
     * 
     * @return <code>null</code> if cell value is <code>null</code>.
     */
    public ISerializableComparable tryToGetValue(int index)
    {
        return cells[index];
    }

    /**
     * Return the length of the underlying array. Useful for debugging.
     */
    public int length()
    {
        return cells.length;
    }

    // GWT only
    @SuppressWarnings("unused")
    private GenericTableRow()
    {
    }

}
