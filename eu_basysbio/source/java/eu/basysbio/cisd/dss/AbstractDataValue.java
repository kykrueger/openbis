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

package eu.basysbio.cisd.dss;

import eu.basysbio.cisd.db.TimeSeriesColumnDescriptor;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataValue
{
    private int rowIndex;

    private int columnIndex;

    private long valueGroupId;

    private TimeSeriesColumnDescriptor descriptor;

    public final int getRowIndex()
    {
        return rowIndex;
    }

    public final void setRowIndex(int rowIndex)
    {
        this.rowIndex = rowIndex;
    }

    public final int getColumnIndex()
    {
        return columnIndex;
    }

    public final void setColumnIndex(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    public final long getValueGroupId()
    {
        return valueGroupId;
    }

    public final void setValueGroupId(long valueGroupId)
    {
        this.valueGroupId = valueGroupId;
    }

    public final TimeSeriesColumnDescriptor getDescriptor()
    {
        return descriptor;
    }

    public final void setDescriptor(TimeSeriesColumnDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

}
