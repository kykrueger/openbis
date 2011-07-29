/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * Test cases for corresponding {@link DataDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "externalData" })
public final class DataDAOTest extends AbstractDAOTest
{

    private static final int SPEED_HINT = Constants.DEFAULT_SPEED_HINT / 4;

    private final String PARENT_CODE = "20081105092158673-1";

    private final String CHILD_CODE = "20081105092159188-3";

    private final String CONTAINER_CODE = "20110509092359990-10";

    private final String COMPONENT_CODE = "20110509092359990-11";

    private final String VIRTUAL_DATA_SET_TYPE_CODE = "CONTAINER_TYPE";

    @Test
    public final void testListSampleDataSets()
    {
        testCreateVirtualDataSetWithSample();
        testCreateDataSetWithSample();
        final IDataDAO dataDAO = daoFactory.getDataDAO();
        List<DataPE> list = dataDAO.listDataSets(pickASample());

        assertEquals(2, list.size());
        // virtual
        DataPE dataSet1 = list.get(0);
        assertEquals("MP", dataSet1.tryGetSample().getCode());
        assertEquals(true, dataSet1.isContainer());
        assertNull(dataSet1.tryAsExternalData());
        // non-virtual
        DataPE dataSet2 = list.get(1);
        assertEquals("MP", dataSet2.tryGetSample().getCode());
        assertEquals(false, dataSet2.isContainer());
        assertNotNull(dataSet2.tryAsExternalData());
        ExternalDataPE externalData = dataSet2.tryAsExternalData();
        assertEquals("abcd", externalData.getLocation());
    }

    @Test
    public void testCreateVirtualDataSetWithSample()
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        SamplePE sample = pickASample();
        DataPE data = createVirtualDataSet(dataSetCode, sample);
        dataDAO.createDataSet(data);

        DataPE dataSet = dataDAO.tryToFindDataSetByCode(dataSetCode);
        assertDataEqual(data, dataSet);
    }

    @Test
    public void testCreateDataSetWithSample()
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        SamplePE sample = pickASample();
        ExternalDataPE externalData = createExternalData(dataSetCode, sample);
        externalData.setShareId("42");
        externalData.setSize(4711L);
        externalData.setSpeedHint(SPEED_HINT);
        dataDAO.createDataSet(externalData);

        ExternalDataPE dataSet = (ExternalDataPE) dataDAO.tryToFindDataSetByCode(dataSetCode);
        assertDataEqual(externalData, dataSet);
        assertEquals("42", dataSet.getShareId());
        assertEquals(4711L, dataSet.getSize().longValue());
        assertEquals(SPEED_HINT, dataSet.getSpeedHint());
    }

    @Test
    public void testCreateVirtualDataSetWithNoSample()
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        DataPE data = createVirtualDataSet(dataSetCode, null);
        dataDAO.createDataSet(data);

        DataPE dataSet = dataDAO.tryToFindDataSetByCode(dataSetCode);
        assertDataEqual(data, dataSet);
    }

    @Test
    public void testCreateVirtualDataSetWithComponents()
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        String vDataSetCode = daoFactory.getPermIdDAO().createPermId();
        DataPE virtualData = createVirtualDataSet(vDataSetCode, null);
        String c1DataSetCode = daoFactory.getPermIdDAO().createPermId();
        String c2DataSetCode = daoFactory.getPermIdDAO().createPermId();
        SamplePE sample = pickASample();
        DataPE component1 = createExternalData(c1DataSetCode, sample);
        component1.setOrderInContainer(1);
        DataPE component2 = createExternalData(c2DataSetCode, sample);
        component1.setOrderInContainer(2);
        virtualData.addComponent(component1);
        virtualData.addComponent(component2);
        dataDAO.createDataSet(virtualData);

        DataPE vDataSet = dataDAO.tryToFindDataSetByCode(vDataSetCode);
        assertDataEqual(virtualData, vDataSet);
        assertEquals(2, virtualData.getContainedDataSets().size());
        DataPE vc1 = virtualData.getContainedDataSets().get(0);
        assertDataEqual(component1, vc1);
        DataPE vc2 = virtualData.getContainedDataSets().get(1);
        assertDataEqual(component2, vc2);

        DataPE vDataSetFull = dataDAO.tryToFindFullDataSetByCode(vDataSetCode, true, false);
        assertDataEqual(virtualData, vDataSetFull);
    }

    @Test
    public void testCreateDataSetWithNoSample()
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        ExternalDataPE externalData = createExternalData(dataSetCode, null);
        dataDAO.createDataSet(externalData);

        ExternalDataPE dataSet = (ExternalDataPE) dataDAO.tryToFindDataSetByCode(dataSetCode);
        assertDataEqual(externalData, dataSet);
        assertEquals(Constants.DEFAULT_SPEED_HINT, dataSet.getSpeedHint());
    }

    private ExternalDataPE createExternalData(String dataSetCode, SamplePE sampleOrNull)
    {
        ExternalDataPE externalData = new ExternalDataPE();

        externalData.setCode(dataSetCode);
        externalData.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        externalData.setExperiment(pickAnExperiment());
        if (sampleOrNull != null)
        {
            externalData.setSampleAcquiredFrom(sampleOrNull);
        } else
        {
            externalData.setDerived(true);
        }
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        externalData.setLocation("abcd");
        externalData.setComplete(BooleanOrUnknown.U);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setPlaceholder(true);
        externalData.setDataStore(pickADataStore());
        return externalData;
    }

    private DataPE createVirtualDataSet(String dataSetCode, SamplePE sampleOrNull)
    {
        DataPE data = new DataPE();

        data.setCode(dataSetCode);
        data.setDataSetType(getDataSetType(VIRTUAL_DATA_SET_TYPE_CODE));
        data.setExperiment(pickAnExperiment());
        if (sampleOrNull != null)
        {
            data.setSampleAcquiredFrom(sampleOrNull);
        } else
        {
            data.setDerived(true);
        }
        data.setPlaceholder(true);
        data.setDataStore(pickADataStore());
        return data;
    }

    private void assertDataEqual(DataPE expectedDataSet, DataPE dataSet)
    {
        assertEquals(expectedDataSet.getCode(), dataSet.getCode());
        assertEquals(expectedDataSet.getDataSetType(), dataSet.getDataSetType());
        assertEquals(expectedDataSet.getExperiment(), dataSet.getExperiment());
        assertEquals(expectedDataSet.isPlaceholder(), dataSet.isPlaceholder());
        assertEquals(expectedDataSet.isMeasured(), dataSet.isMeasured());
        assertEquals(expectedDataSet.tryGetSample(), dataSet.tryGetSample());
        assertEquals(expectedDataSet.isContainer(), dataSet.isContainer());
        if (expectedDataSet.isContainer() == false)
        {
            ExternalDataPE expectedExternalData = expectedDataSet.tryAsExternalData();
            ExternalDataPE externalData = dataSet.tryAsExternalData();
            assertEquals(expectedExternalData.getFileFormatType(), externalData.getFileFormatType());
            assertEquals(expectedExternalData.getLocatorType(), externalData.getLocatorType());
            assertEquals(expectedExternalData.getLocation(), externalData.getLocation());
            assertEquals(expectedExternalData.getComplete(), externalData.getComplete());
            assertEquals(expectedExternalData.getStorageFormat(), externalData.getStorageFormat());
        }
    }

    @Test
    public void testUpdateDataSetAquiredFromSampleWithoutDataSetSize()
    {
        checkUpdateExternalData(null);
    }

    @Test
    public void testUpdateDataSetAquiredFromSampleWithDataSetSize()
    {
        checkUpdateExternalData(4711L);
    }

    private void checkUpdateExternalData(Long size)
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        DataPE data = new DataPE();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        data.setCode(dataSetCode);
        data.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        data.setExperiment(pickAnExperiment());
        data.setSampleAcquiredFrom(pickASample());
        data.setPlaceholder(true);
        data.setDataStore(pickADataStore());
        data.setModificationDate(new Date());
        dataDAO.createDataSet(data);

        ExternalDataPE externalData = new ExternalDataPE();
        externalData.setId(dataDAO.tryToFindDataSetByCode(dataSetCode).getId());
        externalData.setCode(dataSetCode);
        externalData.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        externalData.setDataStore(pickADataStore());
        externalData.setExperiment(pickAnExperiment());
        externalData.setSampleAcquiredFrom(pickASample());
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        externalData.setLocation("abcd");
        externalData.setShareId("share-42");
        externalData.setSize(size);
        externalData.setComplete(BooleanOrUnknown.U);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setPlaceholder(true);
        externalData.setStatus(DataSetArchivingStatus.AVAILABLE);
        final Date modificationTimestamp = data.getModificationDate();
        externalData.setModificationDate(modificationTimestamp);
        dataDAO.updateDataSet(externalData);

        ExternalDataPE dataSet = (ExternalDataPE) dataDAO.tryToFindDataSetByCode(dataSetCode);
        assertEquals(externalData.getCode(), dataSet.getCode());
        assertEquals(externalData.getDataSetType(), dataSet.getDataSetType());
        assertEquals(externalData.getExperiment(), dataSet.getExperiment());
        assertEquals(externalData.getFileFormatType(), dataSet.getFileFormatType());
        assertEquals(externalData.getLocatorType(), dataSet.getLocatorType());
        assertEquals(externalData.getShareId(), dataSet.getShareId());
        assertEquals(externalData.getLocation(), dataSet.getLocation());
        assertEquals(externalData.getSize(), dataSet.getSize());
        assertEquals(externalData.getComplete(), dataSet.getComplete());
        assertEquals(externalData.getStorageFormat(), dataSet.getStorageFormat());
        assertEquals(externalData.isPlaceholder(), dataSet.isPlaceholder());
        assertEquals(externalData.isMeasured(), dataSet.isMeasured());
        assertFalse(externalData.getModificationDate().equals(modificationTimestamp));
        assertFalse(externalData.isContainer());
    }

    @Test(groups = "broken")
    // FIXME LMS-2421
    public void testUpdateDataSetWithParent()
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();

        // try to add a parent to a data set that already had one
        final DataPE dataSetConnectedWithParent = findData(CHILD_CODE);
        assertFalse(dataSetConnectedWithParent.getParents().isEmpty());
        final DataPE anotherDataSet = findData("20081105092159111-1");
        dataSetConnectedWithParent.addParent(anotherDataSet);
        dataDAO.updateDataSet(dataSetConnectedWithParent);

        DataPE dataSet = dataDAO.tryToFindDataSetByCode(CHILD_CODE);
        assertEquals(dataSetConnectedWithParent.getParents().size(), dataSet.getParents().size());
        Set<DataPE> extractedParents = dataSet.getParents();
        for (DataPE parent : dataSetConnectedWithParent.getParents())
        {
            assertTrue(extractedParents.contains(parent));
        }
        assertTrue(extractedParents.contains(anotherDataSet));
    }

    @Test(groups = "broken")
    // FIXME LMS-2421
    public final void testDeleteWithPropertiesButParentPreserved()
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();
        final DataPE deletedData = findData(CHILD_CODE);

        // Deleted data set should have all collections which prevent it from deletion empty.
        assertTrue(deletedData.getChildren().isEmpty());

        // delete
        dataDAO.delete(deletedData);

        // test successful deletion of data set
        assertNull(dataDAO.tryGetByTechId(TechId.create(deletedData)));

        // test successful deletion of data set properties
        assertFalse(deletedData.getProperties().isEmpty());
        List<EntityTypePropertyTypePE> retrievedPropertyTypes =
                daoFactory.getEntityPropertyTypeDAO(EntityKind.DATA_SET).listEntityPropertyTypes(
                        deletedData.getEntityType());
        for (DataSetPropertyPE property : deletedData.getProperties())
        {
            int index = retrievedPropertyTypes.indexOf(property.getEntityTypePropertyType());
            EntityTypePropertyTypePE retrievedPropertyType = retrievedPropertyTypes.get(index);
            assertFalse(retrievedPropertyType.getPropertyValues().contains(property));
        }

        // deleted data set had parent connected that should not have been deleted
        // NOTE: somehow cannot get parents even though connection is the same as with children
        // DataPE parent = deletedData.tryGetParent();
        // assertNotNull(parent);
        // assertNotNull(externalDataDAO.tryGetByTechId(new TechId(HibernateUtils.getId(parent))));
        findData(PARENT_CODE);
    }

    @Test
    public final void testDeleteContainerWithComponentsPreserved()
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();
        DataPE containerDataSet = findData(CONTAINER_CODE);
        DataPE componentDataSet = findData(COMPONENT_CODE);

        assertEquals(containerDataSet.getId(), componentDataSet.getContainer().getId());

        dataDAO.delete(containerDataSet);

        DataPE reloadedContainer = dataDAO.tryToFindDataSetByCode(CONTAINER_CODE);
        assertNull(reloadedContainer);

        DataPE preservedComponent = findData(COMPONENT_CODE);
        assertNull(preservedComponent.getContainer());
    }

    @Test(groups = "broken")
    // FIXME LMS-2421
    public final void testDeleteParentPreservesChildren()
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();
        final DataPE deletedData = findData(PARENT_CODE);

        // Deleted data set should have 'child' data sets.
        assertFalse(deletedData.getChildren().isEmpty());
        List<String> childrenCodes = Code.extractCodes(deletedData.getChildren());

        // delete
        dataDAO.delete(deletedData);

        // test successful deletion of data set
        assertNull(dataDAO.tryGetByTechId(TechId.create(deletedData)));

        // deleted data set had child connected that should not have been deleted
        for (String child : childrenCodes)
        {
            findData(child);
        }
    }

    protected VocabularyTermPE pickAStorageFormatVocabularyTerm()
    {
        String code = StorageFormat.VOCABULARY_CODE;
        VocabularyPE vocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode(code);
        assertNotNull(vocabulary);
        return vocabulary.getTerms().iterator().next();
    }

    protected FileFormatTypePE pickAFileFormatType()
    {
        IFileFormatTypeDAO fileFormatTypeDAO = daoFactory.getFileFormatTypeDAO();
        FileFormatTypePE fileFormatType = fileFormatTypeDAO.tryToFindFileFormatTypeByCode("TIFF");
        assertNotNull(fileFormatType);
        return fileFormatType;
    }

    protected LocatorTypePE pickALocatorType()
    {
        ILocatorTypeDAO locatorTypeDAO = daoFactory.getLocatorTypeDAO();
        LocatorTypePE locatorType = locatorTypeDAO.tryToFindLocatorTypeByCode("RELATIVE_LOCATION");
        assertNotNull(locatorType);
        return locatorType;
    }

    protected SamplePE pickASample()
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        DatabaseInstancePE dbInstance = daoFactory.getHomeDatabaseInstance();
        SamplePE sample = sampleDAO.tryFindByCodeAndDatabaseInstance("MP", dbInstance);
        assertNotNull(sample);
        return sample;
    }

    protected DataStorePE pickADataStore()
    {
        return daoFactory.getDataStoreDAO().tryToFindDataStoreByCode("STANDARD");
    }

    protected DataSetTypePE getDataSetType(DataSetTypeCode type)
    {
        return getDataSetType(type.getCode());
    }

    protected DataSetTypePE getDataSetType(String code)
    {
        IDataSetTypeDAO dataSetTypeDAO = daoFactory.getDataSetTypeDAO();
        DataSetTypePE dataSetType = dataSetTypeDAO.tryToFindDataSetTypeByCode(code);
        assertNotNull(dataSetType);
        return dataSetType;
    }

    protected ExperimentPE pickAnExperiment()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        for (ExperimentPE experimentPE : experiments)
        {
            if (experimentPE.getDeletion() == null)
            {
                return experimentPE;
            }
        }
        fail("No valid experiment found.");
        return null; // to make the compiler happy
    }

    @Test(groups = "broken")
    // FIXME LMS-2421
    public void testCreateDataSetWithBothSampleAndParent()
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();

        // try to create a dataset connected with a sample and a parent at the same time
        final String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        final SamplePE sample = pickASample();
        DataPE parentData = findData(PARENT_CODE);
        DataPE data = createExternalData(dataSetCode, sample);
        data.addParent(parentData);
        dataDAO.createDataSet(data);
    }

    @Test(groups = "broken")
    // FIXME LMS-2421
    public void testUpdateOfDataSetAddParentWhenThereIsSampleConnected()
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();

        // try to update dataset connected with a sample - adding a parent should succeed
        final DataPE dataSetConnectedWithSample = findData(PARENT_CODE);
        assertNotNull(dataSetConnectedWithSample.tryGetSample());
        final DataPE anotherDataSet = findData("20081105092159111-1");
        dataSetConnectedWithSample.addParent(anotherDataSet);
        dataDAO.updateDataSet(dataSetConnectedWithSample);
    }

    @Test(groups = "broken")
    // FIXME LMS-2421
    public void testUpdateOfDataSetConnectSampleWhenThereIsParent()
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();

        // try to update dataset connected with a parent - connecting with a sample should succeed
        final DataPE dataSetConnectedWithParent = findData(CHILD_CODE);
        assertFalse(dataSetConnectedWithParent.getParents().isEmpty());
        dataSetConnectedWithParent.setSampleAcquiredFrom(pickASample());
        dataDAO.updateDataSet(dataSetConnectedWithParent);
    }

    @Test
    public void testLoadByPermId() throws Exception
    {
        DataPE data = daoFactory.getDataDAO().listAllEntities().get(0);
        HashSet<String> keys = new HashSet<String>();
        keys.add(data.getCode());
        keys.add("nonexistent");
        List<DataPE> result = daoFactory.getDataDAO().listByCode(keys);
        AssertJUnit.assertEquals(1, result.size());
        AssertJUnit.assertEquals(data, result.get(0));
    }

    @Test
    public void testLoadByPermIdNoEntries() throws Exception
    {
        HashSet<String> keys = new HashSet<String>();
        List<DataPE> result = daoFactory.getDataDAO().listByCode(keys);
        AssertJUnit.assertTrue(result.isEmpty());
    }

}
