/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class DataSetListingTest extends SystemTestCase
{
    @Test
    public void testListExperimentDataSetsDirectlyConnected()
    {
        logIntoCommonClientService();
        DefaultResultSetConfig<String, TableModelRowWithObject<ExternalData>> criteria =
                new DefaultResultSetConfig<String, TableModelRowWithObject<ExternalData>>();

        TypedTableResultSet<ExternalData> resultSet =
                commonClientService.listExperimentDataSets(new TechId(18), criteria, true);

        List<ExternalData> dataSets = asList(resultSet);
        assertEquals("20081105092159111-1", dataSets.get(0).getCode());
        assertProperties(
                "[ANY_MATERIAL: 1000_C (SIRNA), BACTERIUM: BACTERIUM1 (BACTERIUM), COMMENT: no comment, GENDER: FEMALE]",
                dataSets.get(0));
        assertEquals(1, dataSets.size());
    }

    @Test
    public void testListExperimentDataSetsAlsoIndirectlyConnected()
    {
        logIntoCommonClientService();
        DefaultResultSetConfig<String, TableModelRowWithObject<ExternalData>> criteria =
                new DefaultResultSetConfig<String, TableModelRowWithObject<ExternalData>>();

        TypedTableResultSet<ExternalData> resultSet =
                commonClientService.listExperimentDataSets(new TechId(18), criteria, false);

        List<ExternalData> dataSets = asList(resultSet);
        Collections.sort(dataSets, new Comparator<ExternalData>()
            {
                public int compare(ExternalData e1, ExternalData e2)
                {
                    return e1.getCode().compareTo(e2.getCode());
                }
            });
        assertEquals("20081105092159111-1", dataSets.get(0).getCode());
        assertProperties(
                "[ANY_MATERIAL: 1000_C (SIRNA), BACTERIUM: BACTERIUM1 (BACTERIUM), COMMENT: no comment, GENDER: FEMALE]",
                dataSets.get(0));
        assertEquals("20081105092259000-9", dataSets.get(1).getCode());
        assertProperties("[COMMENT: no comment]", dataSets.get(1));
        assertEquals("20081105092259900-0", dataSets.get(2).getCode());
        assertProperties("[COMMENT: no comment]", dataSets.get(2));
        assertEquals("20081105092259900-1", dataSets.get(3).getCode());
        assertEquals("20081105092359990-2", dataSets.get(4).getCode());
        assertEquals(5, dataSets.size());
    }
}
