/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_GROUP;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_PERSON;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

/**
 * Test cases for corresponding {@link SampleBO} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class SampleBOTest extends AbstractBOTest
{
    private static final String DB = "DB";

    private static final String DILUTION_PLATE = SampleTypeCode.DILUTION_PLATE.getCode();

    private static final String MASTER_PLATE = SampleTypeCode.MASTER_PLATE.getCode();

    private static final String DEFAULT_SAMPLE_CODE = "xx";

    private final static SampleProperty createSampleProperty()
    {
        final SampleProperty sampleProperty = new SampleProperty();
        sampleProperty.setValue("blue");
        final SampleTypePropertyType sampleTypePropertyType = new SampleTypePropertyType();
        final PropertyType propertyType = new PropertyType();
        propertyType.setLabel("color");
        propertyType.setCode("color");
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setDataType(dataType);
        sampleTypePropertyType.setPropertyType(propertyType);
        sampleProperty.setEntityTypePropertyType(sampleTypePropertyType);
        return sampleProperty;
    }

    static SamplePE createSample(final String sampleCode)
    {
        return createSample(sampleCode, "sample-type-code");
    }

    private static SamplePE createSample(final String sampleCode, final String sampleTypeCode)
    {
        return createSample(SampleIdentifier.createHomeGroup(sampleCode), sampleTypeCode);
    }

    private static SamplePE createSample(final SampleIdentifier sampleIdentifier,
            final String sampleTypeCode)
    {
        final SamplePE sample = new SamplePE();
        sample.setRegistrator(EXAMPLE_PERSON);
        final SampleTypePE sampleTypeDTO = new SampleTypePE();
        sampleTypeDTO.setCode(sampleTypeCode);
        sample.setSampleType(sampleTypeDTO);
        return sample;
    }

    private final static SampleIdentifier getSharedSampleIdentifier(final String code)
    {
        return new SampleIdentifier(new DatabaseInstanceIdentifier(DB), code);
    }

    private final static SampleIdentifier getGroupSampleIdentifier(final String code)
    {
        return new SampleIdentifier(IdentifierHelper.createGroupIdentifier(EXAMPLE_GROUP), code);
    }

    
    private void prepareTryToLoadOfInstanceSample(final SampleIdentifier sampleIdentifier,
            final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(DB);
                    DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
                    databaseInstance.setCode(DB);
                    will(returnValue(databaseInstance));

                    String sampleCode = sampleIdentifier.getSampleCode();
                    one(sampleDAO).tryFindByCodeAndDatabaseInstance(sampleCode, databaseInstance,
                            HierarchyType.CHILD);
                    will(returnValue(sample));
                }
            });
    }

    private void prepareTryToLoadOfGroupSample(final SampleIdentifier sampleIdentifier,
            final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode("MY_DATABASE_INSTANCE");
                    DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
                    databaseInstance.setCode("MY_DATABASE_INSTANCE");
                    will(returnValue(databaseInstance));

                    one(groupDAO).tryFindGroupByCodeAndDatabaseInstance(
                            sampleIdentifier.getGroupLevel().getGroupCode(), databaseInstance);
                    GroupPE group = new GroupPE();
                    will(returnValue(group));

                    String sampleCode = sampleIdentifier.getSampleCode();
                    one(sampleDAO).tryFindByCodeAndGroup(sampleCode, group, HierarchyType.CHILD);
                    will(returnValue(sample));
                }
            });
    }

    private ProcedurePE createProcedure(boolean invalidExperiment)
    {
        ProcedurePE procedure = new ProcedurePE();
        ExperimentPE experiment = new ExperimentPE();
        experiment.setInvalidation(invalidExperiment ? new InvalidationPE() : null);
        procedure.setExperiment(experiment);
        return procedure;
    }

    private final SampleBO createSampleBO()
    {
        return new SampleBO(daoFactory, EXAMPLE_SESSION, propertiesConverter);
    }

    @Test
    public void testTryToLoadByUnkownSampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getSharedSampleIdentifier("s1");
        prepareTryToLoadOfInstanceSample(sampleIdentifier, null);
        
        SampleBO sampleBO = createSampleBO();
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        sampleBO.enrichWithValidProcedure();
        try
        {
            sampleBO.getSample();
            fail("IllegalStateException expected.");
        } catch (IllegalStateException e)
        {
            assertEquals("Unloaded sample.", e.getMessage());
        }

        assertSame(null, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToLoadBySampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getSharedSampleIdentifier("s1");
        SamplePE sample = new SamplePE();
        prepareTryToLoadOfInstanceSample(sampleIdentifier, sample);
        
        SampleBO sampleBO = createSampleBO();
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        
        assertSame(sample, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoadByGroupSampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getGroupSampleIdentifier("s1");
        SamplePE sample = new SamplePE();
        prepareTryToLoadOfGroupSample(sampleIdentifier, sample);
        
        SampleBO sampleBO = createSampleBO();
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        
        assertSame(sample, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetUndefinedSample()
    {
        try
        {
            createSampleBO().getSample();
            fail("UserFailureException expected");
        } catch (final IllegalStateException e)
        {
            assertEquals("Unloaded sample.", e.getMessage());
        }
    }
    
    @Test
    public void testEnrichWithValidProcedure()
    {
        SampleIdentifier sampleIdentifier = getSharedSampleIdentifier("s1");
        SamplePE sample = new SamplePE();
        sample.setProcedures(Arrays.asList(createProcedure(false), createProcedure(true)));
        prepareTryToLoadOfInstanceSample(sampleIdentifier, sample);
        
        SampleBO sampleBO = createSampleBO();
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        sampleBO.enrichWithValidProcedure();
        
        assertSame(sample, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testEnrichWithTwoValidProcedures()
    {
        SampleIdentifier sampleIdentifier = getSharedSampleIdentifier("s1");
        SamplePE sample = new SamplePE();
        sample.setProcedures(Arrays.asList(createProcedure(false), createProcedure(false)));
        prepareTryToLoadOfInstanceSample(sampleIdentifier, sample);
        
        SampleBO sampleBO = createSampleBO();
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        try
        {
            sampleBO.enrichWithValidProcedure();
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            String message = e.getMessage();
            assertTrue("Unexpected message: " + message, message
                    .startsWith("Expected exactly one valid procedure, but found 2"));
        }
        
        assertSame(sample, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }
    
    @Test(expectedExceptions = UserFailureException.class)
    public final void testFailToDefineSharedSampleWithParentInAGroup()
    {
        final SampleIdentifier sharedSampleIdentifier =
                getSharedSampleIdentifier(DEFAULT_SAMPLE_CODE);
        final NewSample newSharedSample = new NewSample();
        newSharedSample.setIdentifier(sharedSampleIdentifier.toString());
        final SampleType sampleType = createSampleType(SampleTypeCode.DILUTION_PLATE);
        newSharedSample.setSampleType(sampleType);

        final SampleIdentifier parentGroupIdentifier = getGroupSampleIdentifier("SAMPLE_GENERATOR");
        newSharedSample.setParentIdentifier(parentGroupIdentifier.toString());

        newSharedSample.setProperties(SampleProperty.EMPTY_ARRAY);

        context.checking(new Expectations()
            {
                {
                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(DB);
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    final SamplePE groupParent = new SamplePE();
                    groupParent.setRegistrator(EXAMPLE_PERSON);
                    groupParent.setGroup(EXAMPLE_GROUP);
                    groupParent.setCode("SAMPLE_GENERATOR");
                    one(sampleDAO).tryFindByCodeAndGroup(parentGroupIdentifier.getSampleCode(),
                            EXAMPLE_GROUP, HierarchyType.CHILD);
                    will(returnValue(groupParent));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(BeanUtils.createBean(SampleTypePE.class, sampleType)));

                    one(propertiesConverter).convertProperties(newSharedSample.getProperties(),
                            DILUTION_PLATE, EXAMPLE_PERSON);
                    will(returnValue(new ArrayList<SamplePropertyPE>()));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSharedSample);
    }

    private SampleType createSampleType(final SampleTypeCode sampleTypeCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode.getCode());
        return sampleType;
    }

    @Test
    public final void testDefineSampleHappyCase()
    {
        final SampleIdentifier sampleIdentifier = getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE);
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(sampleIdentifier.toString());
        newSample.setSampleType(createSampleType(SampleTypeCode.DILUTION_PLATE));

        final SampleIdentifier generatedFromIdentifier =
                getGroupSampleIdentifier("SAMPLE_GENERATOR");
        newSample.setParentIdentifier(generatedFromIdentifier.toString());

        final SampleIdentifier containerIdentifier = getGroupSampleIdentifier("SAMPLE_CONTAINER");
        newSample.setContainerIdentifier(containerIdentifier.toString());

        newSample.setProperties(SampleProperty.EMPTY_ARRAY);

        final SamplePE generatedFrom = new SamplePE();
        generatedFrom.setRegistrator(EXAMPLE_PERSON);
        generatedFrom.setGroup(EXAMPLE_GROUP);
        generatedFrom.setCode("SAMPLE_GENERATOR");

        final SamplePE container = new SamplePE();
        container.setRegistrator(EXAMPLE_PERSON);
        container.setGroup(EXAMPLE_GROUP);
        container.setCode("SAMPLE_CONTAINER");

        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(DILUTION_PLATE);

        final SamplePE samplePE = new SamplePE();
        samplePE.setRegistrator(EXAMPLE_PERSON);
        samplePE.setGeneratedFrom(generatedFrom);
        samplePE.setContainer(container);
        samplePE.setSampleType(sampleType);

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleDAO).tryFindByCodeAndGroup(generatedFromIdentifier.getSampleCode(),
                            EXAMPLE_GROUP, HierarchyType.CHILD);
                    will(returnValue(generatedFrom));

                    one(sampleDAO).tryFindByCodeAndGroup(containerIdentifier.getSampleCode(),
                            EXAMPLE_GROUP, HierarchyType.CHILD);
                    will(returnValue(container));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(sampleType));

                    one(propertiesConverter).convertProperties(newSample.getProperties(),
                            DILUTION_PLATE, EXAMPLE_PERSON);
                    will(returnValue(new ArrayList<SamplePropertyPE>()));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSample);

        final SamplePE sample = sampleBO.getSample();
        assertEquals(sampleIdentifier.toString(), sample.getSampleIdentifier().toString());
        assertEquals(EXAMPLE_PERSON, sample.getRegistrator());
        assertSame(sampleType, sample.getSampleType());
        assertEquals(container, sample.getContainer());
        assertEquals(generatedFrom, sample.getGeneratedFrom());

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineAndSaveSampleWithProperties()
    {
        final NewSample newSample = new NewSample();

        newSample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        newSample.setSampleType(createSampleType(SampleTypeCode.MASTER_PLATE));

        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(MASTER_PLATE);
        sampleType.setId(new Long(21L));
        final SampleProperty sampleProperty = createSampleProperty();
        newSample.setProperties(new SampleProperty[]
            { sampleProperty });
        final SamplePropertyPE samplePropertyPE = new SamplePropertyPE();
        samplePropertyPE.setRegistrator(EXAMPLE_SESSION.tryGetPerson());
        final SampleTypePropertyTypePE sampleTypePropertyType = new SampleTypePropertyTypePE();
        samplePropertyPE.setEntityTypePropertyType(sampleTypePropertyType);

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(MASTER_PLATE);
                    will(returnValue(sampleType));

                    one(propertiesConverter).convertProperties(newSample.getProperties(),
                            MASTER_PLATE, EXAMPLE_PERSON);
                    final List<SamplePropertyPE> set = new ArrayList<SamplePropertyPE>();
                    set.add(samplePropertyPE);
                    will(returnValue(set));

                    one(sampleDAO).createSample(with(new BaseMatcher<SamplePE>()
                        {
                            public void describeTo(final Description description)
                            {
                            }

                            public boolean matches(final Object item)
                            {
                                if (item instanceof SamplePE == false)
                                {
                                    return false;
                                }
                                final SamplePE sample = (SamplePE) item;
                                assertEquals(EXAMPLE_SESSION.tryGetHomeGroupId(), sample.getGroup()
                                        .getId());
                                assertNull(sample.getDatabaseInstance());
                                assertEquals(newSample.getIdentifier(), sample
                                        .getSampleIdentifier().toString());
                                assertEquals(EXAMPLE_PERSON, sample.getRegistrator());
                                return true;
                            }
                        }));

                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSample);

        final Set<SamplePropertyPE> properties = sampleBO.getSample().getProperties();
        assertEquals(1, properties.size());
        assertSame(samplePropertyPE, properties.iterator().next());

        sampleBO.save();

        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSampleWithUnknownContainer()
    {
        final NewSample sample = new NewSample();
        sample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        sample.setSampleType(createSampleType(SampleTypeCode.DILUTION_PLATE));
        sample.setContainerIdentifier(getGroupSampleIdentifier("DOES_NOT_EXIST").toString());

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(
                            SampleTypeCode.DILUTION_PLATE.getCode());
                    will(returnValue(new SampleTypePE()));

                    one(propertiesConverter).convertProperties(SampleProperty.EMPTY_ARRAY, null,
                            EXAMPLE_PERSON);

                    one(sampleDAO).tryFindByCodeAndGroup("DOES_NOT_EXIST",
                            EXAMPLE_SESSION.tryGetHomeGroup(), HierarchyType.CHILD);
                    will(returnValue(null));
                }
            });
        try
        {
            createSampleBO().define(sample);
        } catch (final UserFailureException ex)
        {
            assertEquals("No sample could be found for identifier "
                    + "'MY_DATABASE_INSTANCE:/MY_GROUP/DOES_NOT_EXIST'.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSampleWithUnknownParent()
    {
        final NewSample sample = new NewSample();
        sample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        sample.setSampleType(createSampleType(SampleTypeCode.DILUTION_PLATE));
        sample.setParentIdentifier(getGroupSampleIdentifier("DOES_NOT_EXIST").toString());

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(
                            SampleTypeCode.DILUTION_PLATE.getCode());
                    will(returnValue(new SampleTypePE()));

                    one(propertiesConverter).convertProperties(SampleProperty.EMPTY_ARRAY, null,
                            EXAMPLE_PERSON);

                    one(sampleDAO).tryFindByCodeAndGroup("DOES_NOT_EXIST",
                            EXAMPLE_SESSION.tryGetHomeGroup(), HierarchyType.CHILD);
                    will(returnValue(null));
                }
            });
        try
        {
            createSampleBO().define(sample);
        } catch (final UserFailureException ex)
        {
            assertEquals("No sample could be found for identifier "
                    + "'MY_DATABASE_INSTANCE:/MY_GROUP/DOES_NOT_EXIST'.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

}
