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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ITableCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableColumn;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableDoubleCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableLongCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableStringCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * @author Franz-Josef Elmer
 *
 */
abstract class AbstractDssServiceExecutor
{
    protected void checkData(IDssServiceId serviceId)
    {
        if (serviceId == null)
        {
            throw new UserFailureException("Service id cannot be null.");
        }
        if (serviceId instanceof DssServicePermId == false)
        {
            throw new UserFailureException("Unknown service id type: " + serviceId.getClass().getName());
        }
        DssServicePermId permId = (DssServicePermId) serviceId;
        if (StringUtils.isBlank(permId.getPermId()))
        {
            throw new UserFailureException("Service key cannot be empty.");
        }
        IDataStoreId dataStoreId = permId.getDataStoreId();
        if (dataStoreId == null)
        {
            throw new UserFailureException("Data store id cannot be null.");
        }
        if (dataStoreId instanceof DataStorePermId == false)
        {
            throw new UserFailureException("Unknown data store id type: " + dataStoreId.getClass().getName());
        }
        if (StringUtils.isBlank(((DataStorePermId) dataStoreId).getPermId()))
        {
            throw new UserFailureException("Data store code cannot be empty.");
        }
    }

    protected TableModel translate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel tableModel)
    {
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        List<TableColumn> columns = new ArrayList<>(headers.size());
        for (TableModelColumnHeader header : headers)
        {
            columns.add(new TableColumn(header.getTitle()));
        }
        SimpleDateFormat format = new SimpleDateFormat(BasicConstant.CANONICAL_DATE_FORMAT_PATTERN);
        List<TableModelRow> rows = tableModel.getRows();
        List<List<ITableCell>> translatedRows = new ArrayList<>(rows.size());
        for (TableModelRow row : rows)
        {
            List<ISerializableComparable> values = row.getValues();
            List<ITableCell> cells = new ArrayList<>(values.size());
            for (ISerializableComparable value : values)
            {
                ITableCell cell = null;
                if (value instanceof IntegerTableCell)
                {
                    cell = new TableLongCell(((IntegerTableCell) value).getNumber());
                } else if (value instanceof DoubleTableCell)
                {
                    cell = new TableDoubleCell(((DoubleTableCell) value).getNumber());
                } else if (value instanceof DateTableCell)
                {
                    cell = new TableStringCell(format.format(((DateTableCell) value).getDateTime()));
                } else if (value != null)
                {
                    cell = new TableStringCell(value.toString());
                }
                cells.add(cell);
            }
            translatedRows.add(cells);
        }
        return new TableModel(columns, translatedRows);
    }

}
