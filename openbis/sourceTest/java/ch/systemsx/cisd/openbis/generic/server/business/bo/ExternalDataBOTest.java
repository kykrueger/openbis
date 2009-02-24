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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.Date;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ExternalDataBOTest extends AbstractBOTest
{
    private static final String PARENT_CODE = "parent";
    private static final Date PRODUCTION_DATE = new Date(1001);
    private static final String DATA_PRODUCER_CODE = "DPC";
    private static final String DATA_SET_CODE = "DS1";
    private static final String LOCATION = "folder/subfolder/file.fft";
    private static final LocatorType LOCATOR_TYPE = new LocatorType("LT");
    private static final FileFormatType FILE_FORMAT_TYPE = new FileFormatType("FFT");
    private static final DataSetType DATA_SET_TYPE = new DataSetType("DST");

    @Test
    public void testDefineWithoutParentDataSet()
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        ProcedurePE procedure = new ProcedurePE();
        SamplePE sample = new SamplePE();
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType);
        
        IExternalDataBO sampleBO = createSampleBO();
        sampleBO.define(createData(null), procedure, sample, SourceType.DERIVED);
        ExternalDataPE externalData = sampleBO.getExternalData();
        
        assertEquals(DATA_SET_CODE, externalData.getCode());
        assertEquals(BooleanOrUnknown.U, externalData.getComplete());
        assertEquals(DATA_PRODUCER_CODE, externalData.getDataProducerCode());
        assertSame(dataSetType, externalData.getDataSetType());
        assertSame(fileFormatType, externalData.getFileFormatType());
        assertSame(locatorType, externalData.getLocatorType());
        assertEquals(LOCATION, externalData.getLocation());
        assertEquals(0, externalData.getParents().size());
        assertSame(procedure, externalData.getProcedure());
        assertEquals(PRODUCTION_DATE, externalData.getProductionDate());
        assertSame(null, externalData.getSampleAcquiredFrom());
        assertSame(sample, externalData.getSampleDerivedFrom());
        assertEquals(StorageFormat.PROPRIETARY, externalData.getStorageFormat());
        assertSame(vocabularyTerm, externalData.getStorageFormatVocabularyTerm());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDefineWithExistingParentDataSet()
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        ProcedurePE procedure = new ProcedurePE();
        SamplePE sample = new SamplePE();
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType);
        final DataPE data = new DataPE();
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(data));
                }
            });
        
        IExternalDataBO sampleBO = createSampleBO();
        sampleBO.define(createData(PARENT_CODE), procedure, sample, SourceType.MEASUREMENT);
        ExternalDataPE externalData = sampleBO.getExternalData();
        
        assertSame(sample, externalData.getSampleAcquiredFrom());
        assertSame(null, externalData.getSampleDerivedFrom());
        assertEquals(1, externalData.getParents().size());
        assertSame(data, externalData.getParents().iterator().next());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDefineWithNonExistingParentDataSet()
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        final ProcedurePE procedure = new ProcedurePE();
        ProcedureTypePE procedureType = new ProcedureTypePE();
        procedureType.setCode(ProcedureTypeCode.UNKNOWN.getCode());
        procedure.setProcedureType(procedureType);
        procedure.setExperiment(createExperiment());
        final SamplePE sample = new SamplePE();
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType);
        final DataSetTypePE dataSetTypeUnknown = new DataSetTypePE();
        final DataPE parentData = new DataPE();
        parentData.setCode(PARENT_CODE);
        parentData.setDataSetType(dataSetTypeUnknown);
        parentData.setProcedure(procedure);
        parentData.setSampleDerivedFrom(sample);
        parentData.setPlaceholder(true);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(null));
                    
                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode(ProcedureTypeCode.UNKNOWN.getCode());
                    will(returnValue(dataSetTypeUnknown));
                    
                    one(externalDataDAO).createDataSet(parentData);
                }
            });
        
        IExternalDataBO sampleBO = createSampleBO();
        sampleBO.define(createData(PARENT_CODE), procedure, sample, SourceType.MEASUREMENT);
        ExternalDataPE externalData = sampleBO.getExternalData();
        
        assertSame(sample, externalData.getSampleAcquiredFrom());
        assertSame(null, externalData.getSampleDerivedFrom());
        assertEquals(1, externalData.getParents().size());
        assertEquals(parentData, externalData.getParents().iterator().next());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDefineWithNonExistingParentDataSetAndNonExsitingProcedure()
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        final ProcedurePE procedure = new ProcedurePE();
        final ProcedureTypePE procedureType = new ProcedureTypePE();
        procedureType.setCode("new");
        procedure.setProcedureType(procedureType);
        procedure.setExperiment(createExperiment());
        final SamplePE sample = new SamplePE();
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType);
        final DataSetTypePE dataSetTypeUnknown = new DataSetTypePE();
        final DataPE parentData = new DataPE();
        parentData.setCode(PARENT_CODE);
        parentData.setDataSetType(dataSetTypeUnknown);
        parentData.setProcedure(procedure);
        parentData.setSampleDerivedFrom(sample);
        parentData.setPlaceholder(true);
        final ProcedureTypePE procedureTypeUnknown = new ProcedureTypePE();
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(null));

                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode(
                            DataSetTypeCode.UNKNOWN.getCode());
                    will(returnValue(dataSetTypeUnknown));

                    one(procedureTypeDAO).tryFindProcedureTypeByCode(
                            ProcedureTypeCode.UNKNOWN.getCode());
                    will(returnValue(procedureTypeUnknown));

                    ProcedurePE newProcedure = new ProcedurePE();
                    newProcedure.setProcedureType(procedureTypeUnknown);
                    newProcedure.setExperiment(createExperiment());
                    one(procedureDAO).createProcedure(newProcedure);

                    one(externalDataDAO).createDataSet(parentData);
                }
            });
        
        IExternalDataBO sampleBO = createSampleBO();
        sampleBO.define(createData(PARENT_CODE), procedure, sample, SourceType.MEASUREMENT);
        ExternalDataPE externalData = sampleBO.getExternalData();
        
        assertSame(sample, externalData.getSampleAcquiredFrom());
        assertSame(null, externalData.getSampleDerivedFrom());
        assertEquals(1, externalData.getParents().size());
        assertEquals(parentData, externalData.getParents().iterator().next());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testSaveNewDataSet()
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        ProcedurePE procedure = new ProcedurePE();
        SamplePE sample = new SamplePE();
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(DATA_SET_CODE);
                    will(returnValue(null));
                    
                    one(externalDataDAO).createDataSet(with(new BaseMatcher<DataPE>()
                        {
                            public void describeTo(Description description)
                            {
                                description.appendText(DATA_SET_CODE);
                            }
                    
                            public boolean matches(Object item)
                            {
                                if (item instanceof DataPE)
                                {
                                    DataPE data = (DataPE) item;
                                    return data.getCode().equals(DATA_SET_CODE);
                                }
                                return false;
                            }
                        }));
                }
            });
        
        IExternalDataBO sampleBO = createSampleBO();
        sampleBO.define(createData(null), procedure, sample, SourceType.DERIVED);
        sampleBO.save();
        
        context.assertIsSatisfied();
    }

    private ExperimentPE createExperiment()
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode("EXP1");
        ProjectPE project = new ProjectPE();
        project.setCode("P");
        GroupPE group = new GroupPE();
        group.setCode("G");
        DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode("DB");
        group.setDatabaseInstance(databaseInstance);
        project.setGroup(group);
        experiment.setProject(project);
        return experiment;
    }
    
    private void prepareDefine(final DataSetTypePE dataSetType,
            final FileFormatTypePE fileFormatType, final VocabularyPE vocabulary,
            final LocatorTypePE locatorType)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode(DATA_SET_TYPE.getCode());
                    will(returnValue(dataSetType));
                    one(fileFormatTypeDAO).tryToFindFileFormatTypeByCode(FILE_FORMAT_TYPE.getCode());
                    will(returnValue(fileFormatType));
                    one(vocabularyDAO).tryFindVocabularyByCode(StorageFormat.VOCABULARY_CODE);
                    will(returnValue(vocabulary));
                    one(locatorTypeDAO).tryToFindLocatorTypeByCode(LOCATOR_TYPE.getCode());
                    will(returnValue(locatorType));
                }
            });
    }

    private ExternalData createData(String parentDataSetCodeOrNull)
    {
        ExternalData data = new ExternalData();
        data.setCode(DATA_SET_CODE);
        data.setParentDataSetCode(parentDataSetCodeOrNull);
        data.setDataProducerCode(DATA_PRODUCER_CODE);
        data.setProductionDate(PRODUCTION_DATE);
        data.setComplete(BooleanOrUnknown.U);
        data.setStorageFormat(StorageFormat.PROPRIETARY);
        data.setDataSetType(DATA_SET_TYPE);
        data.setFileFormatType(FILE_FORMAT_TYPE);
        data.setLocatorType(LOCATOR_TYPE);
        data.setLocation(LOCATION);
        return data;
    }
    
    private final IExternalDataBO createSampleBO()
    {
        return new ExternalDataBO(daoFactory, EXAMPLE_SESSION);
    }

}
