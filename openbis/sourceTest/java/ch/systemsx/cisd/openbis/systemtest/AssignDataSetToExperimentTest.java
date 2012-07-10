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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
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
public class AssignDataSetToExperimentTest extends BaseTest
{
    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Space sourceSpace;

    Space destinationSpace;

    @Test
    public void dataSetWithoutSampleCanBeUpdatedToAnotherExperiment() throws Exception
    {
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void dataSetWithSampleCanBeUpdatedToAnotherExperiment() throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        ExternalData dataset = create(aDataSet().inSample(sample));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void sampleAssignmentOfDataSetIsRemovedWhenDataSetIsAssignedToAnotherExperiment()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        ExternalData dataset = create(aDataSet().inSample(sample));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));

        assertThat(serverSays(dataset).getSample(), is(nullValue()));
    }

    @Test
    public void childDataSetCanBeAssignedToAnotherExperiment() throws Exception
    {
        ExternalData parent = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData child = create(aDataSet().inExperiment(sourceExperiment).withParent(parent));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(serverSays(child), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void experimentAssignmentOfParentDataSetIsNotChangedWhenChildDataSetIsAssignedToAnotherExperiment()
            throws Exception
    {
        ExternalData parent = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData child = create(aDataSet().inExperiment(sourceExperiment).withParent(parent));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(serverSays(parent), is(inExperiment(sourceExperiment)));
    }

    @Test
    public void parentDataSetCanBeAssignedToAnotherExperiment() throws Exception
    {
        ExternalData parent = create(aDataSet().inExperiment(sourceExperiment));
        create(aDataSet().inExperiment(sourceExperiment).withParent(parent));

        perform(anUpdateOf(parent).toExperiment(destinationExperiment));

        assertThat(serverSays(parent), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void experimentAssignmentOfChildDataSetIsNotChangedWhenParentDatasetIsAssignedToAnotherExperiment()
            throws Exception
    {
        ExternalData parent = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData child = create(aDataSet().inExperiment(sourceExperiment).withParent(parent));

        perform(anUpdateOf(parent).toExperiment(destinationExperiment));

        assertThat(serverSays(child), is(inExperiment(sourceExperiment)));
    }

    @Test
    public void componentDataSetCanBeAssignedToAnotherExperiment() throws Exception
    {
        ExternalData component = create(aDataSet().inExperiment(sourceExperiment));
        create(aDataSet().inExperiment(sourceExperiment).withComponent(component));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(serverSays(component), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void experimentAssignmentOfContainerDataSetIsNotChangedWhenComponentDataSetIsAssignedToAnotherExperiment()
            throws Exception
    {
        ExternalData component = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData container =
                create(aDataSet().inExperiment(sourceExperiment).withComponent(component));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(serverSays(container), is(inExperiment(sourceExperiment)));
    }

    @Test
    public void containerDataSetCanBeAssignedToAnotherExperiment() throws Exception
    {
        ExternalData component = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData container =
                create(aDataSet().inExperiment(sourceExperiment).withComponent(component));

        perform(anUpdateOf(container).toExperiment(destinationExperiment));

        assertThat(serverSays(container), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void experimentAssignmentOfComponentDataSetIsNotChangedWhenContainerDataSetIsAssignedToAnotherExperiment()
            throws Exception
    {
        ExternalData component = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData container =
                create(aDataSet().inExperiment(sourceExperiment).withComponent(component));

        perform(anUpdateOf(container).toExperiment(destinationExperiment));

        assertThat(serverSays(component), is(inExperiment(sourceExperiment)));
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToExperiment", groups = "authorization")
    public void assigningDataSetToAnotherExperimentIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningDataSetToAnotherExperimentIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment).as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        Project sourceProject = create(aProject().inSpace(sourceSpace));
        sourceExperiment = create(anExperiment().inProject(sourceProject));

        destinationSpace = create(aSpace());
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignDataSetToExperimentRule;

    @BeforeClass
    void createAuthorizationRules()
    {
        instance = new InstanceDomain("instance");
        source = new SpaceDomain("space1", instance);
        destination = new SpaceDomain("space2", instance);

        assignDataSetToExperimentRule =
                and(
                        rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        or(
                                rule(destination, RoleWithHierarchy.SPACE_POWER_USER),
                                rule(destination, RoleWithHierarchy.SPACE_ETL_SERVER)
                        )
                );
    }

    @DataProvider
    Object[][] rolesAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignDataSetToExperimentRule, source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignDataSetToExperimentRule), source,
                destination, instance);
    }
}
