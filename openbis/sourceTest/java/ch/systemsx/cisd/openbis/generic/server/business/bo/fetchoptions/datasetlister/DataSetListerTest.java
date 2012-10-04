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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import net.lemnik.eodsql.QueryTool;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.api.v1.ResourceNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStoreForDataSets;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;

/**
 * Test cases for {@link IDataSetListingQuery}.
 * 
 * @author Piotr Kupczyk
 */
@Test(groups =
    { "db", "dataset" })
public class DataSetListerTest extends AbstractDAOTest
{

    @Resource(name = ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
    IGeneralInformationService service;

    private IDataSetLister lister;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryToAuthenticateForAllServices("test", "password");
        lister =
                new DataSetLister(EntityListingTestUtils.createQuery(daoFactory,
                        IDataSetListingQuery.class));
    }

    @Test
    public void testGetDataSetMetaDataForEmptyDataSetCodesShouldReturnEmptyList()
    {
        List<String> codes = new ArrayList<String>();
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodeShouldReturnDataSet()
    {
        List<String> codes = Collections.singletonList("20081105092159188-3");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodesShouldReturnDataSets()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataStoreBaseURLs()
    {
        DatabaseConfigurationContext context = DatabaseContextUtils.getDatabaseContext(daoFactory);
        QueryTool
                .update(context.getDataSource(),
                        "update data_stores set download_url='http://download_1' where code='STANDARD'");
        final long newDataStoreId =
                (Long) QueryTool
                        .select(context.getDataSource(),
                                "insert into data_stores (id,dbin_id,code,download_url,remote_url,session_token)"
                                        + " values (nextval('data_store_id_seq'),1,'DSS2','http://download_2','','') returning id")
                        .get(0).get("id");
        QueryTool.update(context.getDataSource(),
                "update data set dast_id = ?{1} where code = ?{2}", newDataStoreId,
                "20081105092259000-20");
        QueryTool.update(context.getDataSource(),
                "update data set dast_id = ?{1} where code = ?{2}", newDataStoreId,
                "20081105092259000-21");

        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        codes.add("20081105092259000-20");
        codes.add("20081105092259000-21");
        List<DataStoreForDataSets> result = lister.getDataStoreBaseURLs(codes);
        assertEquals(2, result.size());
        assertEquals("http://download_1", result.get(0).getDataStoreDownloadURL());
        assertEquals(
                Arrays.asList("20081105092159188-3", "20081105092159111-1", "20081105092259000-19"),
                result.get(0).getDataSetCodes());
        assertEquals("http://download_2", result.get(1).getDataStoreDownloadURL());
        assertEquals(Arrays.asList("20081105092259000-20", "20081105092259000-21"), result.get(1)
                .getDataSetCodes());
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodesWithParentsShouldReturnDataSetsWithParents()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        codes.add("20081105092259000-20");
        codes.add("20081105092259000-21");
        DataSetFetchOptions fetchOptions =
                new DataSetFetchOptions(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS);

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodesWithChildrenShouldReturnDataSetsWithChildren()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        codes.add("20081105092259000-20");
        codes.add("20081105092259000-21");
        DataSetFetchOptions fetchOptions =
                new DataSetFetchOptions(DataSetFetchOption.BASIC, DataSetFetchOption.CHILDREN);

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testGetDataSetMetaDataForExistingAndNotExistingDataSetCodesShouldThrowException()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("UNKNOWN-DATASET-CODE");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        lister.getDataSetMetaData(codes, fetchOptions);
    }

    @Test
    public void testGetDataSetMetaDataForLinkDataSets()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20120628092259000-23");
        codes.add("20120628092259000-24");
        codes.add("20120628092259000-25");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> results = lister.getDataSetMetaData(codes, fetchOptions);

        assertEquals(3, results.size());
        assertTrue(results.get(0).isLinkDataSet());
        assertTrue(results.get(1).isLinkDataSet());
        assertTrue(results.get(2).isLinkDataSet());
        assertFalse(results.get(0).isContainerDataSet());
        assertFalse(results.get(1).isContainerDataSet());
        assertFalse(results.get(2).isContainerDataSet());
        assertEquals("CODE1", results.get(0).getExternalDataSetCode());
        assertEquals("CODE2", results.get(1).getExternalDataSetCode());
        assertEquals("CODE3", results.get(2).getExternalDataSetCode());
        assertEquals("http://example.edms.pl/code=CODE1", results.get(0).getExternalDataSetLink());
        assertEquals("http://example.edms.pl/code=CODE2", results.get(1).getExternalDataSetLink());
        assertEquals("http://www.openbis.ch/perm_id=CODE3", results.get(2).getExternalDataSetLink());

        DatabaseInstance db = new DatabaseInstance();
        db.setId(1L);
        db.setCode("CISD");
        db.setUuid("57F0FA8F-80AC-42AB-9C6A-AAADBCC37A3E");
        db.setHomeDatabase(true);

        ExternalDataManagementSystem dms1 = new ExternalDataManagementSystem();
        dms1.setId(1L);
        dms1.setCode("DMS_1");
        dms1.setLabel("Test EDMS");
        dms1.setOpenBIS(false);
        dms1.setUrlTemplate("http://example.edms.pl/code=${code}");
        dms1.setDatabaseInstance(db);

        ExternalDataManagementSystem dms2 = new ExternalDataManagementSystem();
        dms2.setId(2L);
        dms2.setCode("DMS_2");
        dms2.setLabel("Test External openBIS instance");
        dms2.setOpenBIS(true);
        dms2.setUrlTemplate("http://www.openbis.ch/perm_id=${code}");
        dms2.setDatabaseInstance(db);

        assertEqualsToExternalDMS(dms1, results.get(0).getExternalDataManagementSystem());
        assertEqualsToExternalDMS(dms1, results.get(1).getExternalDataManagementSystem());
        assertEqualsToExternalDMS(dms2, results.get(2).getExternalDataManagementSystem());
    }

    private static void sortDataSetsByCode(List<DataSet> dataSets)
    {
        Collections.sort(dataSets, new Comparator<DataSet>()
            {
                @Override
                public int compare(DataSet o1, DataSet o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
    }

    private void assertEqualToDataSetMetaDataWithCodes(List<String> expectedDataSetsCodes,
            DataSetFetchOptions expectedFetchOptions, List<DataSet> actualDataSets)
    {
        assertEquals(expectedDataSetsCodes.size(), actualDataSets.size());

        List<DataSet> expectedDataSets =
                service.getDataSetMetaData(sessionToken, expectedDataSetsCodes);

        sortDataSetsByCode(expectedDataSets);
        sortDataSetsByCode(actualDataSets);

        Iterator<DataSet> expectedIterator = expectedDataSets.iterator();
        Iterator<DataSet> actualIterator = actualDataSets.iterator();

        // results should be returned in the same order
        while (expectedIterator.hasNext() && actualIterator.hasNext())
        {
            DataSet expectedDataSet = expectedIterator.next();
            DataSet actualDataSet = actualIterator.next();
            assertNotNull(expectedDataSet);
            assertNotNull(actualDataSet);
            assertEqualsToDataSet(expectedDataSet, expectedFetchOptions, actualDataSet);
        }
    }

    private void assertEqualsToDataSet(DataSet expected, DataSetFetchOptions expectedFetchOptions,
            DataSet actual)
    {
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getExperimentIdentifier(), actual.getExperimentIdentifier());
        assertEquals(expected.getSampleIdentifierOrNull(), actual.getSampleIdentifierOrNull());
        assertEquals(expected.getDataSetTypeCode(), actual.getDataSetTypeCode());
        assertEquals(expected.isContainerDataSet(), actual.isContainerDataSet());
        assertEquals(expected.isLinkDataSet(), actual.isLinkDataSet());
        assertEqualsToRegistrationDetails(expected.getRegistrationDetails(),
                expected.getRegistrationDetails());

        if (expectedFetchOptions.isSupersetOf(DataSetFetchOption.PARENTS))
        {
            assertCollections(expected.getParentCodes(), actual.getParentCodes());
        }

        if (expectedFetchOptions.isSupersetOf(DataSetFetchOption.CHILDREN))
        {
            assertCollections(expected.getChildrenCodes(), actual.getChildrenCodes());
        }

        assertEquals(expectedFetchOptions, actual.getFetchOptions());
    }

    private static <T extends Comparable<T>> void assertCollections(Collection<T> col1,
            Collection<T> col2)
    {
        ArrayList<T> a1 = new ArrayList<T>(col1);
        ArrayList<T> a2 = new ArrayList<T>(col2);

        Collections.sort(a1);
        Collections.sort(a2);

        assertEquals(a1, a2);
    }

    private void assertEqualsToRegistrationDetails(EntityRegistrationDetails expected,
            EntityRegistrationDetails actual)
    {
        assertFalse(expected == null ^ actual == null);

        if (expected != null && actual != null)
        {
            assertEquals(expected.getUserId(), actual.getUserId());
            assertEquals(expected.getUserFirstName(), actual.getUserFirstName());
            assertEquals(expected.getUserLastName(), actual.getUserLastName());
            assertEquals(expected.getUserEmail(), actual.getUserEmail());
            assertEquals(expected.getRegistrationDate(), actual.getRegistrationDate());
        }

    }

    private void assertEqualsToExternalDMS(ExternalDataManagementSystem expected,
            ExternalDataManagementSystem actual)
    {
        assertFalse(expected == null ^ actual == null);

        if (expected != null && actual != null)
        {
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getCode(), actual.getCode());
            assertEquals(expected.getLabel(), actual.getLabel());
            assertEquals(expected.getUrlTemplate(), actual.getUrlTemplate());
            assertEquals(expected.isOpenBIS(), actual.isOpenBIS());
            assertEqualsToDatabaseInstance(expected.getDatabaseInstance(),
                    actual.getDatabaseInstance());
        }
    }

    private void assertEqualsToDatabaseInstance(DatabaseInstance expected, DatabaseInstance actual)
    {
        assertFalse(expected == null ^ actual == null);

        if (expected != null && actual != null)
        {
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getCode(), actual.getCode());
            assertEquals(expected.getIdentifier(), actual.getIdentifier());
            assertEquals(expected.getUuid(), actual.getUuid());
        }
    }

}