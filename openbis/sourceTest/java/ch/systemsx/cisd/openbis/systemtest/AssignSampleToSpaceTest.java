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
import static org.hamcrest.CoreMatchers.nullValue;
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
public class AssignSampleToSpaceTest extends BaseTest
{

    Experiment experiment;

    Space sourceSpace;

    Space destinationSpace;

    @Test
    public void sampleWithExperimentCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfSampleIsRemovedWhenSampleIsAssignedToAnotherSpace()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test
    public void spaceSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void sharedSampleCanBeAssignedToSpace() throws Exception
    {
        Sample sample = create(aSample());

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void childSampleCanBeAssignedToAnotherSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(child).toSpace(destinationSpace));

        assertThat(serverSays(child), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfParentSampleIsNotChangedWhenChildSampleIsAssignedToAnotherSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(child).toSpace(destinationSpace));

        assertThat(serverSays(parent), is(inSpace(sourceSpace)));
    }

    @Test
    public void parentSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(parent).toSpace(destinationSpace));

        assertThat(serverSays(parent), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfChildSampleIsNotChangedWhenParentSampleIsAssignedToAnotherSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(parent).toSpace(destinationSpace));

        assertThat(serverSays(child), is(inSpace(sourceSpace)));
    }

    @Test
    public void componentSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).toSpace(destinationSpace));

        assertThat(serverSays(component), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfContainerSampleIsNotChangedWhenComponentSampleIsAssignedToAnotherSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).toSpace(destinationSpace));

        assertThat(serverSays(container), is(inSpace(sourceSpace)));
    }

    @Test
    public void containerSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).toSpace(destinationSpace));

        assertThat(serverSays(container), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfComponentSampleIsNotChangedWhenContainerSampleIsAssingnedToAnotherSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).toSpace(destinationSpace));

        assertThat(serverSays(component), is(inSpace(sourceSpace)));
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToSpace", groups = "authorization")
    public void assigningSampleToAnotherSpaceIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToSpace", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToAnotherSpaceIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAssignSharedSampleToSpace", groups = "authorization")
    public void assigningSharedSampleToSpaceIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole));
        Sample sharedSample = create(aSample());

        perform(anUpdateOf(sharedSample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSharedSampleToSpace", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSharedSampleToSpaceIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole));
        Sample sharedSample = create(aSample());

        perform(anUpdateOf(sharedSample).toSpace(destinationSpace).as(user));
    }

    @Test
    public void assigningSampleToSameSpaceIsAllowedToAllSpaceUsers()
            throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));
        String user = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_USER, sourceSpace));

        perform(anUpdateOf(sample).toSpace(sourceSpace).as(user));

        assertThat(serverSays(sample), is(inSpace(sourceSpace)));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());
        Project project = create(aProject().inSpace(sourceSpace));
        experiment =
                create(anExperiment().inProject(project));
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignSampleToSpaceRule;

    AuthorizationRule assignSharedSampleToSpaceRule;

    @BeforeClass
    void createAuthorizationRules()
    {
        instance = new InstanceDomain("instance");
        source = new SpaceDomain("space1", instance);
        destination = new SpaceDomain("space2", instance);

        assignSampleToSpaceRule =
                or(
                        and(
                                rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                                rule(destination, RoleWithHierarchy.SPACE_POWER_USER)),

                        and(
                                rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(destination, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER))
                );

        assignSharedSampleToSpaceRule =
                and(
                        rule(destination, RoleWithHierarchy.SPACE_USER),
                        rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                );
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(assignSampleToSpaceRule, source, destination,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSampleToSpaceRule), source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSharedSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(assignSharedSampleToSpaceRule,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSharedSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSharedSampleToSpaceRule),
                destination, instance);
    }
}
