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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class QueryFacadeTest extends AbstractQueryFacadeTest
{

    @Test
    public void testAggregationServiceReport() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "world");

        QueryTableModel table =
                createReportFromAggregationService("example-aggregation-service", parameters);

        assertEquals("[String, Integer]", getHeaders(table).toString());
        assertEquals("[Hello, 20]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals("[world, 30]", Arrays.asList(table.getRows().get(1)).toString());
        assertEquals(2, table.getRows().size());
    }

    @Test
    public void testJythonAggregationServiceReport() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "world");

        QueryTableModel table =
                createReportFromAggregationService("example-jython-aggregation-service-report",
                        parameters);

        assertEquals("[Key, Value]", getHeaders(table).toString());
        assertEquals("[name, world]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
        assertEquals(
                "[From: datastore_server@localhost, To: franz-josef.elmer@systemsx.ch, Subject: test, Content:]",
                FileUtilities.loadToStringList(getLatestEmail()).subList(1, 5).toString());
    }

    @Test
    public void testJythonReportWhichSendsEMail() throws Exception
    {
        new File(store, "1/xml/result-8").mkdirs();
        ReportDescription description = getTableReportDescription("example-jython-report");

        QueryTableModel table =
                queryFacade.createReportFromDataSets(description,
                        Arrays.asList("20081105092259000-8"));

        assertEquals("[Data Set, Data Set Type]", getHeaders(table).toString());
        assertEquals("[20081105092259000-8, HCS_IMAGE]", Arrays.asList(table.getRows().get(0))
                .toString());
        assertEquals(1, table.getRows().size());
        assertEquals(
                "[From: datastore_server@localhost, To: franz-josef.elmer@systemsx.ch, Subject: test, Content:]",
                FileUtilities.loadToStringList(getLatestEmail()).subList(1, 5).toString());
    }

    @Test
    public void testDbModifyingAggregationServiceReport() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "world");

        QueryTableModel table =
                createReportFromAggregationService("example-db-modifying-aggregation-service",
                        parameters);

        assertEquals("[String, Integer]", getHeaders(table).toString());
        assertEquals("[Hello, 20]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals("[world, 30]", Arrays.asList(table.getRows().get(1)).toString());
        assertEquals(2, table.getRows().size());

        IGeneralInformationService generalInformationService =
                queryFacade.getGeneralInformationService();
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(
                        queryFacade.getSessionToken(), null);
        boolean foundSpace = false;
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            if ("NEWDUMMYSPACE".equalsIgnoreCase(space.getCode()))
            {
                foundSpace = true;
            }
        }

        assertTrue("Did not find a space called [NEWDUMMYSPACE]", foundSpace);
    }

    @Test
    public void testJythonDbModifyingAggregationServiceReport() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("code", "JYTHON-TEST");

        QueryTableModel table =
                createReportFromAggregationService(
                        "example-jython-db-modifying-aggregation-service", parameters);

        assertEquals("[CODE, IDENTIFIER]", getHeaders(table).toString());
        assertEquals("[JYTHON-TEST, /CISD/JYTHON-TEST]", Arrays.asList(table.getRows().get(0))
                .toString());
        assertEquals(1, table.getRows().size());

        IGeneralInformationService generalInformationService =
                queryFacade.getGeneralInformationService();

        List<Sample> samples =
                generalInformationService.listSamplesForExperiment(queryFacade.getSessionToken(),
                        "/CISD/NEMO/EXP-TEST-1");
        boolean foundSample = false;
        for (Sample sample : samples)
        {
            if ("JYTHON-TEST".equalsIgnoreCase(sample.getCode()))
            {
                foundSample = true;
            }
        }

        assertTrue("Did not find a sample called [JYTHON-TEST]", foundSample);
    }

    /**
     * The observer trying to access the forbidden dataset via the authorized content provider.
     */
    @Test(expectedExceptions = Exception.class)
    public void testJythonAggregationServiceWithContentProviderAuthentication() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        createReportFromAggregationService("observer", "content-provider-aggregation-service",
                parameters);
    }

    /**
     * The testcase, where the observer tries to acces the dataset that he cannot see, but through
     * the non-authorized content provider.
     */
    @Test
    public void testJythonAggregationServiceWithoutContentProviderAuthentication() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        QueryTableModel table =
                createReportFromAggregationService("observer",
                        "content-provider-aggregation-service-no-authorization", parameters);

        assertEquals("[name]", getHeaders(table).toString());
        assertEquals("[1]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
    }

    /**
     * The authorized user tries to access the dataset via the authorized content provider.
     */
    @Test
    public void testJythonAggregationServiceWithContentProviderAuthenticationAndAuthorizedUser()
            throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        QueryTableModel table =
                createReportFromAggregationService("content-provider-aggregation-service",
                        parameters);

        assertEquals("[name]", getHeaders(table).toString());
        assertEquals("[1]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
    }

    private ReportDescription getTableReportDescription(String key)
    {
        List<ReportDescription> services = queryFacade.listTableReportDescriptions();
        for (ReportDescription description : services)
        {
            if (description.getKey().equals(key))
            {
                return description;
            }
        }
        throw new AssertionError("No reporting for key '" + key + "'.");
    }

    private File getLatestEmail()
    {
        File latestFile = null;
        File[] files = new File("targets/email").listFiles();
        for (File file : files)
        {
            if (latestFile == null || latestFile.lastModified() < file.lastModified())
            {
                latestFile = file;
            }
        }
        return latestFile;
    }

    /**
     * The testcase, that checks whether the results of the search service calls for different users
     */
    @Test
    public void testDataSetSearchServiceInAggregationService() throws Exception
    {
        int allTest = getSearchServiceAggregationServiceResult("datasetsAll", "test");
        int allObserver = getSearchServiceAggregationServiceResult("datasetsAll", "observer");

        int filteredTest = getSearchServiceAggregationServiceResult("datasetsFiltered", "test");
        int filteredObserver =
                getSearchServiceAggregationServiceResult("datasetsFiltered", "test_space");

        assertEquals(allTest, allObserver);
        assertEquals(allTest, filteredTest);
        assertTrue("There are " + allTest
                + " datasets, and observer should see less, but he did see " + filteredObserver,
                allTest > filteredObserver);
        assertTrue("There should be some datasets visible to the observer, but there are no",
                filteredObserver > 0);
    }

    /**
     * Test searching for samples via search service for different users
     */
    @Test
    public void testSampleSearchServiceInAggregationService() throws Exception
    {
        int allTest = getSearchServiceAggregationServiceResult("samplesAll", "test");
        int allObserver = getSearchServiceAggregationServiceResult("samplesAll", "observer");

        int filteredTest = getSearchServiceAggregationServiceResult("samplesFiltered", "test");
        int filteredObserver =
                getSearchServiceAggregationServiceResult("samplesFiltered", "observer");
        int filteredObserver2 =
                getSearchServiceAggregationServiceResult("samplesFiltered", "test_space");

        assertEquals(allTest, allObserver);
        assertEquals(allTest, filteredTest);
        assertTrue("There are " + allTest
                + " samples, and observer should see less, but he did see " + filteredObserver,
                allTest > filteredObserver);
        assertTrue("There are " + allTest
                + " samples, and test_space should see less, but he did see " + filteredObserver,
                allTest > filteredObserver2);
        assertTrue("There should be some samples visible to the observer, but there are no",
                filteredObserver > 0);
        assertTrue("There should be some samples visible to the test_space, but there are no",
                filteredObserver2 > 0);
    }

    /**
     * Check listing experiments for different users via search service
     */
    @Test
    public void testExperimentSearchServiceInAggregationService() throws Exception
    {
        int allNemoTest =
                getSearchServiceAggregationServiceResult("experimentsAll", "test", "projectId",
                        "/CISD/NEMO");
        int allNemoObserver =
                getSearchServiceAggregationServiceResult("experimentsAll", "observer", "projectId",
                        "/CISD/NEMO");

        assertTrue(allNemoTest > 0);
        assertEquals(allNemoTest, allNemoObserver);

        int filteredNemoTest =
                getSearchServiceAggregationServiceResult("experimentsFiltered", "test",
                        "projectId", "/CISD/NEMO");
        int filteredNemoObserver =
                getSearchServiceAggregationServiceResult("experimentsFiltered", "observer",
                        "projectId", "/CISD/NEMO");
        int filteredNemoTestSpace =
                getSearchServiceAggregationServiceResult("experimentsFiltered", "test_space",
                        "projectId", "/CISD/NEMO");

        assertEquals(allNemoTest, filteredNemoTest);
        assertEquals(0, filteredNemoObserver);
        assertEquals(0, filteredNemoTestSpace);

        int filteredTestSpace =
                getSearchServiceAggregationServiceResult("experimentsFiltered", "test_space",
                        "projectId", "/TEST-SPACE/TEST-PROJECT");

        assertTrue(filteredTestSpace > 0);
    }

    private int getSearchServiceAggregationServiceResult(String mode, String user, String... params)
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("mode", mode);

        for (int i = 0; 2 * i + 1 < params.length; i += 2)
        {
            parameters.put(params[2 * i], params[2 * i + 1]);
        }

        QueryTableModel table =
                createReportFromAggregationService(user, "search-service-aggregation-service",
                        parameters);

        assertEquals("[result]", getHeaders(table).toString());
        assertEquals(1, table.getRows().size());
        return Integer.parseInt(table.getRows().get(0)[0].toString());
    }
}
