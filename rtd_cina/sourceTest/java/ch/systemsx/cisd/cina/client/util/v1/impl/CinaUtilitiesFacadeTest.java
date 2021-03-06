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

package ch.systemsx.cisd.cina.client.util.v1.impl;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaUtilitiesFacadeTest extends AssertJUnit
{
    private final static String USER_ID = "userid";

    private final static String PASSWORD = "password";

    private final static String SESSION_TOKEN = "sessionToken";

    private Mockery context;

    private IGeneralInformationService service;

    private IServiceForDataStoreServer openbisService;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
        openbisService = context.mock(IServiceForDataStoreServer.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForSamples()
    {
        final SearchCriteria searchCriteria = new SearchCriteria();
        context.checking(new Expectations()
            {
                {
                    final ArrayList<Sample> samples = new ArrayList<Sample>();

                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    one(service).getMinorVersion();
                    will(returnValue(1));

                    one(service).searchForSamples(SESSION_TOKEN, searchCriteria);
                    will(returnValue(samples));

                    one(service).logout(SESSION_TOKEN);
                }
            });
        CinaUtilitiesFacade facade = createFacade(service, openbisService);
        facade.login(USER_ID, PASSWORD, 1);
        assertEquals(facade.getSessionToken(), SESSION_TOKEN);
        List<Sample> result = facade.searchForSamples(searchCriteria);
        assertEquals(0, result.size());
        facade.logout();
        context.assertIsSatisfied();
    }

    @Test
    public void testGenerateSampleCode()
    {
        final String sampleTypeCode = "SampleTypeCode";
        final SampleType sampleType = new SampleType();
        sampleType.setAutoGeneratedCode(true);
        sampleType.setGeneratedCodePrefix("STC-");
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    one(service).getMinorVersion();
                    will(returnValue(0));

                    one(openbisService).getSampleType(SESSION_TOKEN, sampleTypeCode);
                    will(returnValue(sampleType));

                    one(openbisService).drawANewUniqueID(SESSION_TOKEN);
                    will(returnValue((long) 1));

                    one(service).logout(SESSION_TOKEN);
                }
            });
        CinaUtilitiesFacade facade = createFacade(service, openbisService);
        facade.login(USER_ID, PASSWORD, 0);
        assertEquals(facade.getSessionToken(), SESSION_TOKEN);
        String result = facade.generateSampleCode(sampleTypeCode);
        assertEquals("STC-1", result);
        facade.logout();
        context.assertIsSatisfied();
    }

    @Test
    public void testListVisibleExperiments()
    {
        final ArrayList<Project> projects = new ArrayList<Project>();
        Project project = new Project("PROJECT-1", "SPACE-1");
        projects.add(project);

        final ArrayList<Experiment> experiments = new ArrayList<Experiment>();

        final ArrayList<SpaceWithProjectsAndRoleAssignments> spaces =
                new ArrayList<SpaceWithProjectsAndRoleAssignments>();
        SpaceWithProjectsAndRoleAssignments space =
                new SpaceWithProjectsAndRoleAssignments("SPACE-1");
        space.add(project);
        space.add("user", new Role("ADMIN", true));
        spaces.add(space);
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    one(service).getMinorVersion();
                    will(returnValue(2));

                    one(service).listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);
                    will(returnValue(spaces));

                    one(service).listExperiments(SESSION_TOKEN, projects, "EXP-TYPE");
                    will(returnValue(experiments));

                    one(service).logout(SESSION_TOKEN);
                }
            });
        CinaUtilitiesFacade facade = createFacade(service, openbisService);
        facade.login(USER_ID, PASSWORD, 0);
        assertEquals(facade.getSessionToken(), SESSION_TOKEN);
        List<Experiment> result = facade.listVisibleExperiments("EXP-TYPE");
        assertEquals(0, result.size());
        facade.logout();
        context.assertIsSatisfied();
    }

    /**
     * Utility method to create a CinaUtilitiesFacade object for testing.
     */
    public static CinaUtilitiesFacade createFacade(IGeneralInformationService service,
            IServiceForDataStoreServer openbisService)
    {
        CinaUtilitiesFacade facade = new CinaUtilitiesFacade(service, openbisService, null, null);
        return facade;
    }

    /**
     * Utility method to create a CinaUtilitiesFacade object for testing.
     */
    public static CinaUtilitiesFacade createFacade(IGeneralInformationService service,
            IServiceForDataStoreServer openbisService, String userId, String password)
    {
        CinaUtilitiesFacade facade = new CinaUtilitiesFacade(service, openbisService, null, null);
        facade.login(userId, password, 0);
        return facade;
    }

    /**
     * Utility method to create a CinaUtilitiesFacade object with a mocked dssComponent for testing.
     */
    @SuppressWarnings("deprecation")
    public static CinaUtilitiesFacade createFacade(IGeneralInformationService service,
            IServiceForDataStoreServer openbisService, IDssComponent dssComponent, String userId,
            String password)
    {
        CinaUtilitiesFacade facade = new CinaUtilitiesFacade(service, openbisService, null, null);
        // The loginForTesting method is marked deprecated to discourage accidental use.
        facade.loginForTesting(userId, password, dssComponent);
        return facade;
    }
}
