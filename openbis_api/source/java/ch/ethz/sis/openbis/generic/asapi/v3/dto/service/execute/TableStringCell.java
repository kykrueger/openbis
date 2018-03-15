/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.service.execute.TableStringCell")
public final class TableStringCell implements ITableCell
{
    private static final long serialVersionUID = 1L;

    private String value;

    public TableStringCell(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (super.equals(obj) == false || getClass() != obj.getClass())
        {
            return false;
        }
        TableStringCell that = (TableStringCell) obj;
        return value == null ? value == that.value : value.equals(that.value);
    }

    @Override
    public String toString()
    {
        return String.valueOf(value);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private TableStringCell()
    {
    }

    @SuppressWarnings("unused")
    private void setValue(String value)
    {
        this.value = value;
    }

}
