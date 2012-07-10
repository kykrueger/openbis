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

import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.and;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.not;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.or;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.rule;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * @author anttil
 */
public class AssignExperimentToProjectTest extends BaseTest
{

    Space sourceSpace;

    Space destinationSpace;

    Project sourceProject;

    Project destinationProject;

    @Test
    public void experimentCanBeUpdatedToAnotherProject() throws Exception
    {
        Experiment experiment = create(anExperiment().inProject(sourceProject));

        perform(anUpdateOf(experiment).toProject(destinationProject));

        assertThat(serverSays(experiment), is(inProject(destinationProject)));
    }

    @Test
    public void assigningExperimentToProjectInAnotherSpaceChangesTheSpaceAssignmentOfSamplesInThatExperiment()
            throws Exception
    {
        Experiment experiment = create(anExperiment().inProject(sourceProject));
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(experiment).toProject(destinationProject));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test(dataProvider = "rolesAllowedToAssignExperimentToProject", groups = "authorization")
    public void assigningExperimentToProjectIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Space unrelatedObserver = create(aSpace());
        Space unrelatedAdmin = create(aSpace());
        create(aSpace().withCode("unrelatedWithoutAccess"));
        String user =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));
        Experiment experiment = create(anExperiment().inProject(sourceProject));

        perform(anUpdateOf(experiment).toProject(destinationProject).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignExperimentToProject", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningExperimentToProjectIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Space unrelatedObserver = create(aSpace());
        Space unrelatedAdmin = create(aSpace());
        create(aSpace().withCode("unrelatedWithoutAccess"));
        String user =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));
        Experiment experiment = create(anExperiment().inProject(sourceProject));

        perform(anUpdateOf(experiment).toProject(destinationProject).as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());
        sourceProject = create(aProject().inSpace(sourceSpace));
        destinationProject = create(aProject().inSpace(destinationSpace));
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignExperimentToProjectRule;

    @BeforeClass
    void createAuthorizationRules()
    {
        instance = new InstanceDomain("instance");
        source = new SpaceDomain("space1", instance);
        destination = new SpaceDomain("space2", instance);

        assignExperimentToProjectRule =
                or(
                        and(
                                rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                                rule(destination, RoleWithHierarchy.SPACE_POWER_USER)
                        ),

                        and(
                                rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(destination, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                        )
                );

    }

    @DataProvider
    Object[][] rolesAllowedToAssignExperimentToProject()
    {
        return RolePermutator.getAcceptedPermutations(assignExperimentToProjectRule, source,
                destination,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignExperimentToProject()
    {
        return RolePermutator.getAcceptedPermutations(not(assignExperimentToProjectRule), source,
                destination, instance);
    }
}
