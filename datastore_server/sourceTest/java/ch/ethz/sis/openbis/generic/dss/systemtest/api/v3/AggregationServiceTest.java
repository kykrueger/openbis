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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.AggregationServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ITableCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableDoubleCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableLongCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableStringCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author Franz-Josef Elmer
 */
public class AggregationServiceTest extends AbstractFileTest
{
    @Test
    public void testExecuteAggregationService()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-aggregation-service-report", new DataStorePermId("STANDARD"));
        AggregationServiceExecutionOptions options = new AggregationServiceExecutionOptions();
        options.withParameter("name", "Einstein");
        options.withParameter("pi", Math.PI);
        options.withParameter("answer", 42);
        Date date = new Date();
        options.withParameter("timestamp", date);

        // When
        TableModel tableModel = as.executeAggregationService(sessionToken, id, options);

        // Then
        assertEquals("[Key, Value]", tableModel.getColumns().toString());
        String timeStamp = new SimpleDateFormat(BasicConstant.CANONICAL_DATE_FORMAT_PATTERN).format(date);
        assertEquals("[[answer, 42], [name, Einstein], [pi, 3.141592653589793], [timestamp, " + timeStamp + "]]",
                tableModel.getRows().toString());
        assertEquals(getValueCellFor(tableModel.getRows(), "answer").getClass(), TableLongCell.class);
        assertEquals(getValueCellFor(tableModel.getRows(), "pi").getClass(), TableDoubleCell.class);
        assertEquals(getValueCellFor(tableModel.getRows(), "name").getClass(), TableStringCell.class);
        assertEquals(getValueCellFor(tableModel.getRows(), "timestamp").getClass(), TableStringCell.class);

        as.logout(sessionToken);
    }

    @Test
    public void testExecuteAggregationServiceWithMissingServiceId()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        AggregationServiceExecutionOptions options = new AggregationServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeAggregationService(sessionToken, null, options);
                }
            },
                // Then
                "Service id cannot be null.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteAggregationServiceWithUnknownServiceIdType()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new MyId();
        AggregationServiceExecutionOptions options = new AggregationServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeAggregationService(sessionToken, id, options);
                }
            },
                // Then
                "Unknown service id type: " + MyId.class.getName());
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteAggregationServiceWithEmptyPermId()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("", new DataStorePermId("STANDARD"));
        AggregationServiceExecutionOptions options = new AggregationServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeAggregationService(sessionToken, id, options);
                }
            },
                // Then
                "Service key cannot be empty.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteAggregationServiceWithUnknownDataStoreIdType()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("key", new MyId());
        AggregationServiceExecutionOptions options = new AggregationServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeAggregationService(sessionToken, id, options);
                }
            },
                // Then
                "Unknown data store id type: " + MyId.class.getName());
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteAggregationServiceWithEmptyDataStoreCode()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("key", new DataStorePermId(""));
        AggregationServiceExecutionOptions options = new AggregationServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeAggregationService(sessionToken, id, options);
                }
            },
                // Then
                "Data store code cannot be empty.");
        as.logout(sessionToken);
    }

    private ITableCell getValueCellFor(List<List<ITableCell>> rows, String key)
    {
        for (List<ITableCell> row : rows)
        {
            if (row.get(0).toString().equals(key))
            {
                return row.get(1);
            }
        }
        return null;
    }

    private static final class MyId implements IDssServiceId, IDataStoreId
    {
        private static final long serialVersionUID = 1L;
    }

}
