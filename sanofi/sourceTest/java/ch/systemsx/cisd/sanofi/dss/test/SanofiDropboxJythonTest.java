/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.sanofi.dss.test;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;
import static ch.systemsx.cisd.common.test.AssertionUtil.assertContains;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.eodsql.MockDataSet;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * <pre>
 * Things not tested
 * - skip well creation when plate library already exists
 * - skip material creation for preexisting materials
 * </pre>
 * 
 * @author Kaloyan Enimanev
 */
public class SanofiDropboxJythonTest extends AbstractJythonDataSetHandlerTest
{
    private static final String PLATE_CODE = "plateCode.variant";

    private static final String LIBRARY_TEMPLATE_PROPNAME = "LIBRARY_TEMPLATE";

    private static final String EXPERIMENT_RECIPIENTS_PROPNAME = "OBSERVER_EMAILS";

    private static final String[] EXPERIMENT_RECIPIENTS = new String[]
        { "admin@sanofi.com", "mickey@mouse.org" };

    private static final String MATERIAL_TYPE = "COMPOUND";
    
    private static final String POSITIVE_CONTROL_TYPE = "POSITIVE_CONTROL";

    private static final String NEGATIVE_CONTROL_TYPE = "NEGATIVE_CONTROL";

    private static final String COMPOUND_WELL_TYPE = "COMPOUND_WELL";

    private static final String COMPOUND_WELL_CONCENTRATION_PROPNAME = "CONCENTRATION_M";

    private static final String COMPOUND_WELL_MATERIAL_PROPNAME = "COMPOUND";

    private static final String IMAGE_DATA_SET_DIR_NAME = "batchNr_plateCode.variant_2011.07.05";

    private static final String OVERLAYS_DATA_SET_DIR_NAME = "overlays";

    private static final String ANALYSIS_DATA_SET_FILE_NAME = "LC80463-RS101117.xml.csv";

    private static final String IMAGE_DATA_SET_CODE = "data-set-code";

    private static final DataSetType IMAGE_DATA_SET_TYPE = new DataSetType("HCS_IMAGE_RAW");

    private static final String IMAGE_DATA_SET_BATCH_PROP = "ACQUISITION_BATCH";

    private static final String OVERLAY_DATA_SET_CODE = "overlay-data-set-code";

    private static final DataSetType OVERLAY_DATA_SET_TYPE = new DataSetType(
            "HCS_IMAGE_SEGMENTATION");

    private static final String ANALYSIS_DATA_SET_CODE = "analysis-data-set-code";

    private static final DataSetType ANALYSIS_DATA_SET_TYPE = new DataSetType(
            "HCS_ANALYSIS_WELL_FEATURES");

    private static final String EXPERIMENT_IDENTIFIER = "/SANOFI/PROJECT/EXP";
    private static final String PLATE_IDENTIFIER = "/SANOFI/TEST-PLATE";

    private RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails;

    private RecordingMatcher<ListMaterialCriteria> materialCriteria;

    private RecordingMatcher<String> email;

    private BeanFactory applicationContext;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        applicationContext = context.mock(BeanFactory.class);
        
        extendJythonLibPath(getRegistrationScriptsFolderPath());

        atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        materialCriteria = new RecordingMatcher<ListMaterialCriteria>();
        email = new RecordingMatcher<String>();

        ServiceProviderTestWrapper.setApplicationContext(applicationContext);
        context.checking(new Expectations()
            {
                {
                    allowing(applicationContext).getBean("openBIS-service");
                    will(returnValue(openBisService));
                }
            });
    }

    @Override
    public void tearDown() throws IOException
    {
        super.tearDown();

        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testLibraryWider() throws IOException
    {
        createDataSetHandler(false, false);
        final Sample plate =
                plateWithLibTemplateAndGeometry("1.45\t\tH\n0.12\t0.002\tL", "10_WELLS_1X10");
        context.checking(new Expectations()
            {
                {

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(EXPERIMENT_RECIPIENTS)));
                }
            });

        final String error =
                "The property LIBRARY_TEMPLATE of experiment '/SANOFI/PROJECT/EXP' contains 2 rows, "
                        + "but the geometry of plate 'TEST-PLATE' allows a maximum of 1 rows. You should either reduce the "
                        + "number of rows in the library template or change the plate geometry.";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, error);

        handler.handle(markerFile);

        assertContains(error, email.recordedObject());
        assertContains(IMAGE_DATA_SET_DIR_NAME, email.recordedObject());

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testLibraryHigher() throws IOException
    {
        createDataSetHandler(false, false);
        final Sample plate =
                plateWithLibTemplateAndGeometry("1.45\t\tH\n0.12\t0.002\tL", "5_WELLS_5X1");
        context.checking(new Expectations()
            {
                {

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(EXPERIMENT_RECIPIENTS)));
                }
            });

        final String error =
                "The property LIBRARY_TEMPLATE of experiment '/SANOFI/PROJECT/EXP' contains 3 "
                        + "columns in row 1, but the geometry of plate 'TEST-PLATE' allows a maximum of "
                        + "5 columns. You should either reduce the number of columns in the library "
                        + "template or change the plate geometry.";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, error);

        handler.handle(markerFile);

        assertContains(error, email.recordedObject());
        assertContains(IMAGE_DATA_SET_DIR_NAME, email.recordedObject());

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testLibraryTemplateWithWellNotPresentInAbase() throws IOException
    {
        createDataSetHandler(false, false);

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1"));

        final Sample plate = plateWithLibTemplateAndGeometry("1.45\tH\n0.12\tL", "25_WELLS_5X5");
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(EXPERIMENT_RECIPIENTS)));
                }
            });

        final String error =
                "Error registering library for plate 'TEST-PLATE'. The library template specified in "
                        + "property 'LIBRARY_TEMPLATE' of experiment '/SANOFI/PROJECT/EXP' contains concentration value "
                        + "for well 'B1', but no mapping to a material was found in the ABASE DB.";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, error);

        handler.handle(markerFile);

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testLibraryTemplateIncompleteAccordingToAbase() throws IOException
    {
        createDataSetHandler(false, false);

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1"));
        queryResult.add(createQueryResult("A3"));

        final Sample plate = plateWithLibTemplateAndGeometry("1.45\tH", "25_WELLS_5X5");
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(EXPERIMENT_RECIPIENTS)));
                }
            });

        final String error =
                " Error registering library for plate 'TEST-PLATE'. The ABASE DB contains a material definition "
                        + "for well 'A3', but no valid concentration was found in the library template of experiment "
                        + "'/SANOFI/PROJECT/EXP'. The library template should contain a number for 'A3' but no value was found";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, error);

        handler.handle(markerFile);

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCaseWithLibraryCreation() throws IOException
    {
        createDataSetHandler(false, true);
        final Sample plate =
                plateWithLibTemplateAndGeometry("1.45\t\tH\n0.12\t0.002\tL", "6_WELLS_10X10");

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1"));
        queryResult.add(createQueryResult("B1"));
        queryResult.add(createQueryResult("B2"));

        setDataSetExpectations();
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)),
                            with(anything()));
                    will(returnValue(queryResult));

                    one(openBisService).listMaterials(with(materialCriteria), with(equal(true)));
                    will(returnValue(Collections.emptyList()));
                    
                    exactly(5).of(openBisService).createPermId();
                    will(returnValue("well-permId"));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    exactly(4).of(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    exactly(3).of(openBisService).getPropertiesOfTopSampleRegisteredFor(
                            sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));

                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(EXPERIMENT_RECIPIENTS)));
                }
            });

        handler.handle(markerFile);

        assertEquals(MATERIAL_TYPE, materialCriteria.recordedObject().tryGetMaterialType()
                .getCode());
        assertEquals(true, queryResult.hasCloseBeenInvoked());

        List<NewSample> registeredSamples =
                atomicatOperationDetails.recordedObject().getSampleRegistrations();

        assertEquals(5, registeredSamples.size());
        assertAllSamplesHaveContainer(registeredSamples, plate.getIdentifier());
        assertCompoundWell(registeredSamples, "A1", "1.45");
        assertPositiveControl(registeredSamples, "A3");
        assertCompoundWell(registeredSamples, "B1", "0.12");
        assertCompoundWell(registeredSamples, "B2", "0.002");
        assertNegativeControl(registeredSamples, "B3");

        List<? extends NewExternalData> dataSetsRegistered =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations();
        assertEquals(3, dataSetsRegistered.size());

        NewExternalData imageDataSet = dataSetsRegistered.get(0);
        assertEquals(IMAGE_DATA_SET_CODE, imageDataSet.getCode());
        assertEquals(IMAGE_DATA_SET_TYPE, imageDataSet.getDataSetType());
        assertHasProperty(imageDataSet, IMAGE_DATA_SET_BATCH_PROP, "batchNr");

        NewExternalData overlayDataSet = dataSetsRegistered.get(1);
        assertEquals(OVERLAY_DATA_SET_CODE, overlayDataSet.getCode());
        assertEquals(OVERLAY_DATA_SET_TYPE, overlayDataSet.getDataSetType());

        NewExternalData analysisDataSet = dataSetsRegistered.get(2);
        assertEquals(ANALYSIS_DATA_SET_CODE, analysisDataSet.getCode());
        assertEquals(ANALYSIS_DATA_SET_TYPE, analysisDataSet.getDataSetType());


        AssertionUtil
                .assertContains(
                        "New data from folder 'batchNr_plateCode.variant_2011.07.05' has been successfully registered in plate "
                                + "<a href='https://bwl27.sanofi-aventis.com:8443/openbis#entity=SAMPLE&sample_type=PLATE&action=SEARCH&code=plateCode.variant'>plateCode.variant</a>",
                        email.recordedObject());
        context.assertIsSatisfied();
    }

    @Test
    public void testFatalErrorSentToAdmin() throws IOException
    {
        createDataSetHandler(false, false);
        final Sample plate = plateWithLibTemplateAndGeometry("0.75\tH\n54.12\tL", "8_WELLS_2X4");

        final String[] adminEmails = new String[]
            { "admin@sanofi.com", null, "admin@openbis.org", "" };

        final String[] nonEmptyAdminEmails = new String[]
            { "admin@sanofi.com", "admin@openbis.org" };

        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(throwException(new RuntimeException("Connection to ABASE DB Failed")));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());

                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(openBisService).listAdministrators();
                    List<String> adminEmailsList = Arrays.asList(adminEmails);
                    will(returnValue(createAdministrators(adminEmailsList)));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(nonEmptyAdminEmails)));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(EXPERIMENT_RECIPIENTS)));

                }

            });

        handler.handle(markerFile);

        assertEquals(0, atomicatOperationDetails.getRecordedObjects().size());


        assertContains("java.lang.RuntimeException: Connection to ABASE DB Failed", email
                .getRecordedObjects().get(0));
        assertEquals(
                "Dear openBIS user,\n"
                        + "    \n"
                        + "      Registering new data from incoming folder 'batchNr_plateCode.variant_2011.07.05' has failed due to a system error.\n"
                        + "      \n"
                        + "      openBIS has sent a notification to the responsible system administrators and they should be \n"
                        + "      fixing the problem as soon as possible. \n" + "      \n"
                        + "      We are sorry for any inconveniences this may have caused. \n"
                        + "      \n" + "    openBIS Administrators", email.getRecordedObjects()
                        .get(1).trim());

        context.assertIsSatisfied();
    }

    private List<Person> createAdministrators(List<String> adminEmails)
    {
        List<Person> result = new ArrayList<Person>();
        for (String adminEmail : adminEmails)
        {
            Person person = new Person();
            person.setEmail(adminEmail);
            result.add(person);
        }
        return result;
    }

    @Test
    public void testHappyCaseWithLibraryCreationAndNonUniqueMaterials() throws IOException
    {
        createDataSetHandler(false, true);
        final Sample plate = plateWithLibTemplateAndGeometry("0.75\tH\n54.12\tL", "8_WELLS_2X4");

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1", "material-1"));
        queryResult.add(createQueryResult("B1", "material-1"));

        setDataSetExpectations();
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    one(openBisService).listMaterials(with(materialCriteria), with(equal(true)));
                    will(returnValue(Collections.emptyList()));

                    exactly(4).of(openBisService).createPermId();
                    will(returnValue("well-permId"));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    exactly(4).of(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    exactly(3).of(openBisService).getPropertiesOfTopSampleRegisteredFor(
                            sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));

                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(EXPERIMENT_RECIPIENTS)));
                }
            });

        handler.handle(markerFile);

        assertEquals(MATERIAL_TYPE, materialCriteria.recordedObject().tryGetMaterialType()
                .getCode());
        assertEquals(true, queryResult.hasCloseBeenInvoked());

        List<NewSample> registeredSamples =
                atomicatOperationDetails.recordedObject().getSampleRegistrations();

        assertEquals(4, registeredSamples.size());
        assertAllSamplesHaveContainer(registeredSamples, plate.getIdentifier());
        assertCompoundWell(registeredSamples, "A1", "0.75", "material-1");
        assertPositiveControl(registeredSamples, "A2");
        assertCompoundWell(registeredSamples, "B1", "54.12", "material-1");

        List<? extends NewExternalData> dataSetsRegistered =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations();
        assertEquals(3, dataSetsRegistered.size());

        NewExternalData imageDataSet = dataSetsRegistered.get(0);
        assertEquals(IMAGE_DATA_SET_CODE, imageDataSet.getCode());
        assertEquals(IMAGE_DATA_SET_TYPE, imageDataSet.getDataSetType());
        assertHasProperty(imageDataSet, IMAGE_DATA_SET_BATCH_PROP, "batchNr");

        NewExternalData overlayDataSet = dataSetsRegistered.get(1);
        assertEquals(OVERLAY_DATA_SET_CODE, overlayDataSet.getCode());
        assertEquals(OVERLAY_DATA_SET_TYPE, overlayDataSet.getDataSetType());

        NewExternalData analysisDataSet = dataSetsRegistered.get(2);
        assertEquals(ANALYSIS_DATA_SET_CODE, analysisDataSet.getCode());
        assertEquals(ANALYSIS_DATA_SET_TYPE, analysisDataSet.getDataSetType());

        Map<String, List<NewMaterial>> materialsRegistered =
                atomicatOperationDetails.recordedObject().getMaterialRegistrations();
        assertEquals(1, materialsRegistered.size());
        assertEquals("material-1", materialsRegistered.get(MATERIAL_TYPE).get(0).getCode());

        AssertionUtil
                .assertContains(
                        "New data from folder 'batchNr_plateCode.variant_2011.07.05' has been successfully registered in plate "
                                + "<a href='https://bwl27.sanofi-aventis.com:8443/openbis#entity=SAMPLE&sample_type=PLATE&action=SEARCH&code=plateCode.variant'>plateCode.variant</a>",
                        email.recordedObject());
        context.assertIsSatisfied();
    }
    private void assertHasProperty(NewExternalData dataSet, String propCode, String propValue)
    {
        for (NewProperty prop : dataSet.getDataSetProperties())
        {
            if (prop.getPropertyCode().equals(propCode))
            {
                assertEquals("Invalid value in property " + propCode, propValue, prop.getValue());
                return;
            }
        }

        fail(String.format("No property with code %s was found in data set %s", propCode,
                dataSet.getCode()));

    }

    private void assertAllSamplesHaveContainer(List<NewSample> newSamples,
            String containerIdentifier)
    {
        for (NewSample newSample : newSamples)
        {
            assertEquals(containerIdentifier, newSample.getContainerIdentifier());
        }
    }

    private NewSample findByWellCode(List<NewSample> newSamples, String wellCode)
    {
        for (NewSample newSample : newSamples)
        {
            if (newSample.getIdentifier().endsWith(":" + wellCode))
            {
                return newSample;
            }
        }
        throw new RuntimeException("Failed to find sample registration for well " + wellCode);
    }

    private void assertNegativeControl(List<NewSample> newSamples, String wellCode)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(NEGATIVE_CONTROL_TYPE, newSample.getSampleType().getCode());
        assertEquals(0, newSample.getProperties().length);
    }

    private void assertPositiveControl(List<NewSample> newSamples, String wellCode)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(POSITIVE_CONTROL_TYPE, newSample.getSampleType().getCode());
        assertEquals(0, newSample.getProperties().length);
    }

    private void assertCompoundWell(List<NewSample> newSamples, String wellCode,
            String concentration)
    {
        String materialCode = getMaterialCodeByWellCode(wellCode);
        assertCompoundWell(newSamples, wellCode, concentration, materialCode);
    }

    private void assertCompoundWell(List<NewSample> newSamples, String wellCode,
            String concentration, String materialCode)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(COMPOUND_WELL_TYPE, newSample.getSampleType().getCode());

        IEntityProperty concentrationProp =
                EntityHelper.tryFindProperty(newSample.getProperties(),
                        COMPOUND_WELL_CONCENTRATION_PROPNAME);
        assertNotNull(concentrationProp);
        assertEquals("Invalid concentration value for well '" + wellCode + "': ", concentration,
                concentrationProp.tryGetAsString());

        MaterialIdentifier materialIdentifier = new MaterialIdentifier(materialCode, MATERIAL_TYPE);

        IEntityProperty wellMaterialProp =
                EntityHelper.tryFindProperty(newSample.getProperties(),
                        COMPOUND_WELL_MATERIAL_PROPNAME);
        assertNotNull(wellMaterialProp);
        assertEquals("Invalid material found in well '" + wellCode + "': ",
                materialIdentifier.print(), wellMaterialProp.tryGetAsString());

    }

    public Sample plateWithLibTemplateAndGeometry(String libraryTemplate, String plateGeometry)
            throws IOException
    {
        Sample plate = createPlate(libraryTemplate, plateGeometry);
        setUpPlateSearchExpectations(plate);
        setUpLibraryTemplateExpectations(plate);
        return plate;
    }

    private void createDataSetHandler(boolean shouldRegistrationFail, boolean rethrowExceptions)
            throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("dropbox-all-in-one-with-library.py");
        createHandler(properties, shouldRegistrationFail, rethrowExceptions);
        createData();
    }

    private void setDataSetExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(IMAGE_DATA_SET_CODE));

                    one(openBisService).createDataSetCode();
                    will(returnValue(OVERLAY_DATA_SET_CODE));

                    one(openBisService).createDataSetCode();
                    will(returnValue(ANALYSIS_DATA_SET_CODE));

                    one(dataSetValidator).assertValidDataSet(
                            IMAGE_DATA_SET_TYPE,
                            new File(new File(stagingDirectory, IMAGE_DATA_SET_CODE),
                                    IMAGE_DATA_SET_DIR_NAME));

                    one(dataSetValidator).assertValidDataSet(
                            OVERLAY_DATA_SET_TYPE,
                            new File(new File(stagingDirectory, OVERLAY_DATA_SET_CODE),
                                    OVERLAYS_DATA_SET_DIR_NAME));

                    one(dataSetValidator).assertValidDataSet(
                            ANALYSIS_DATA_SET_TYPE,
                            new File(new File(stagingDirectory, ANALYSIS_DATA_SET_CODE),
                                    ANALYSIS_DATA_SET_FILE_NAME));
                }
            });
    }

    private void setUpPlateSearchExpectations(final Sample plate)
    {
        context.checking(new Expectations()
            {
                {
                    SearchCriteria sc = new SearchCriteria();
                    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                            "PLATE"));
                    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                            PLATE_CODE));
                    oneOf(openBisService).searchForSamples(sc);

                    will(returnValue(Arrays.asList(plate)));
                }
            });
    }

    private void setUpLibraryTemplateExpectations(final Sample plate)
    {
        context.checking(new Expectations()
            {
                {
                    final String identifierString = plate.getExperiment().getIdentifier();
                    ExperimentIdentifier identifier =
                            ExperimentIdentifierFactory.parse(identifierString);
                    oneOf(openBisService).tryToGetExperiment(identifier);
                    will(returnValue(plate.getExperiment()));
                }
            });
    }

    private void createData() throws IOException
    {
        File dataDirectory = new File("./sourceTest/examples/" + IMAGE_DATA_SET_DIR_NAME);
        FileUtils.copyDirectoryToDirectory(dataDirectory, workingDirectory);
        incomingDataSetFile = new File(workingDirectory, dataDirectory.getName());

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + dataDirectory.getName());
        FileUtilities.writeToFile(markerFile, "");
    }

    private Sample createPlate(String libraryTemplate, String plateGeometry)
    {
        ExperimentBuilder experimentBuilder = new ExperimentBuilder();
        experimentBuilder.identifier(EXPERIMENT_IDENTIFIER);
        experimentBuilder.property(LIBRARY_TEMPLATE_PROPNAME, libraryTemplate);
        String recipients = StringUtils.join(Arrays.asList(EXPERIMENT_RECIPIENTS), ",");
        experimentBuilder.property(EXPERIMENT_RECIPIENTS_PROPNAME, recipients);

        SampleBuilder sampleBuilder = new SampleBuilder();
        sampleBuilder.experiment(experimentBuilder.getExperiment());
        sampleBuilder.identifier(PLATE_IDENTIFIER);
        sampleBuilder.property(ScreeningConstants.PLATE_GEOMETRY, plateGeometry);

        final Sample plate = sampleBuilder.getSample();
        return plate;
    }

    private Map<String, Object> createQueryResult(String wellCode)
    {
        return createQueryResult(wellCode, getMaterialCodeByWellCode(wellCode));
    }

    private Map<String, Object> createQueryResult(String wellCode, String materialCode)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("WELL_CODE", wellCode);
        result.put("MATERIAL_CODE", materialCode);
        result.put("ABASE_COMPOUND_ID", wellCode + "_compound_id");
        result.put("ABASE_COMPOUND_BATCH_ID", wellCode + "_compound_batch_id");
        return result;
    }

    private String getMaterialCodeByWellCode(String wellCode)
    {
        return wellCode + "_material_code";
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/sanofi-dropbox/";
    }

    @Override
    protected void createHandler(Properties threadProperties, final boolean registrationShouldFail,
            boolean shouldReThrowException)
    {
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(threadProperties);

        handler = new TestingPlateDataSetHandler(globalState, registrationShouldFail,
                shouldReThrowException);

    }

    private class TestingPlateDataSetHandler extends JythonPlateDataSetHandler
    {
        private final boolean shouldRegistrationFail;

        private final boolean shouldReThrowRollbackException;

        public TestingPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
                boolean shouldRegistrationFail, boolean shouldReThrowRollbackException)
        {
            super(globalState);
            this.shouldRegistrationFail = shouldRegistrationFail;
            this.shouldReThrowRollbackException = shouldReThrowRollbackException;
        }

        @Override
        public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
                NewExternalData data) throws Throwable
        {
            if (shouldRegistrationFail)
            {
                throw new UserFailureException("Didn't work.");
            } else
            {
                super.registerDataSetInApplicationServer(dataSetInformation, data);
            }
        }

        @Override
        public void rollback(DataSetRegistrationService<DataSetInformation> service,
                Throwable throwable)
        {
            super.rollback(service, throwable);
            if (shouldReThrowRollbackException)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            } else
            {
                throwable.printStackTrace();
            }
        }

        @Override
        public void didRollbackTransaction(DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction,
                DataSetStorageAlgorithmRunner<DataSetInformation> algorithmRunner,
                Throwable throwable)
        {
            super.didRollbackTransaction(service, transaction, algorithmRunner, throwable);

            if (shouldReThrowRollbackException)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            } else
            {
                throwable.printStackTrace();
            }
        }

    }

}