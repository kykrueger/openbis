/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnHeader;

/**
 * Stores:<br>
 * - a pointer to rows of a table model in the server cache<br>
 * - the table model header
 * 
 * @author Tomasz Pylak
 */
public class TableModelReference implements IsSerializable
{
    // a key at which data are stored in the server cache
    private String resultSetKey;

    private List<TableModelColumnHeader> header;

    public TableModelReference(String resultSetKey, List<TableModelColumnHeader> header)
    {
        this.resultSetKey = resultSetKey;
        this.header = header;
    }

    public String getResultSetKey()
    {
        return resultSetKey;
    }

    public List<TableModelColumnHeader> getHeader()
    {
        return header;
    }

    // GWT only
    @SuppressWarnings("unused")
    private TableModelReference()
    {
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setResultSetKey(String resultSetKey)
    {
        this.resultSetKey = resultSetKey;
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setHeader(List<TableModelColumnHeader> header)
    {
        this.header = header;
    }

}
