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

package ch.systemsx.cisd.openbis.generic.server;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * The unique {@link IRelationshipService} implementation.
 * 
 * @author anttil
 */
public class RelationshipService implements IRelationshipService
{
    private static final String ERR_EXPERIMENT_NOT_FOUND =
            "Experiment '%s' not found";

    private static final String ERR_PROJECT_NOT_FOUND =
            "Project '%s' not found";

    private static final String ERR_SPACE_NOT_FOUND =
            "Space '%s' not found";

    private static final String ERR_DATABASE_NOT_FOUND =
            "Database '%s' not found";

    private static final String ERR_SAMPLE_NOT_FOUND =
            "Sample '%s' not found";

    private static final String ERR_DATASET_NOT_FOUND =
            "Sample '%s' not found";

    private static final String ERR_SAMPLE_PARENT_RELATIONSHIP_NOT_FOUND =
            "Sample '%s' did not have parent '%s'";

    private DAOFactory daoFactory;

    @Override
    public void assignExperimentToProject(IAuthSession session, ExperimentPE experiment,
            ProjectPE project)
    {
        SampleUtils.setSamplesGroup(experiment, project.getSpace());
        experiment.setProject(project);
    }

    @Override
    public void assignProjectToSpace(IAuthSession session, ProjectPE project,
            SpacePE space)
    {
        project.setSpace(space);
        for (ExperimentPE experiment : project.getExperiments())
        {
            SampleUtils.setSamplesGroup(experiment, space);
        }
    }

    @Override
    public void assignSampleToExperiment(IAuthSession session, SampleIdentifier sampleId,
            ExperimentIdentifier experimentId)
    {
        SamplePE sample = findSample(sampleId);
        ExperimentPE experiment = findExperiment(experimentId);

        sample.setExperiment(experiment);

        for (DataPE dataset : sample.getDatasets())
        {
            dataset.setExperiment(experiment);
        }
    }

    @Override
    public void unassignSampleFromExperiment(IAuthSession session, SampleIdentifier sampleId)
    {
        SamplePE sample = findSample(sampleId);
        if (sample.getExperiment() != null)
        {
            sample.getExperiment().removeSample(sample);
        }
    }

    @Override
    public void assignSampleToSpace(IAuthSession session, SampleIdentifier sampleId,
            SpaceIdentifier spaceId)
    {
        SamplePE sample = findSample(sampleId);
        SpacePE space = findSpace(spaceId);
        sample.setDatabaseInstance(null);
        sample.setSpace(space);
    }

    @Override
    public void unshareSample(IAuthSession session, SampleIdentifier sampleId,
            SpaceIdentifier spaceId)
    {
        assignSampleToSpace(session, sampleId, spaceId);
    }

    @Override
    public void shareSample(IAuthSession session, SampleIdentifier sampleId)
    {
        SamplePE sample = findSample(sampleId);
        SpacePE space = sample.getSpace();
        sample.setSpace(null);
        sample.setDatabaseInstance(space.getDatabaseInstance());
    }

    @Override
    public void assignDataSetToExperiment(IAuthSession session, String dataSetCode,
            ExperimentIdentifier experimentId)
    {
        DataPE data = findDataSet(dataSetCode);
        ExperimentPE experiment = findExperiment(experimentId);
        data.setExperiment(experiment);
        data.setSample(null);
    }

    @Override
    public void assignDataSetToSample(IAuthSession session, String dataSetCode,
            SampleIdentifier sampleId)
    {
        DataPE data = findDataSet(dataSetCode);
        SamplePE sample = findSample(sampleId);
        data.setExperiment(sample.getExperiment());
        data.setSample(sample);
    }

    @Override
    public void addParentToSample(IAuthSession session, SampleIdentifier sampleId,
            SampleIdentifier parentId)
    {
        PersonPE actor = session.tryGetPerson();
        RelationshipTypePE relationshipType =
                daoFactory.getRelationshipTypeDAO().tryFindRelationshipTypeByCode(
                        BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);

        SamplePE sample = findSample(sampleId);
        SamplePE parent = findSample(parentId);

        sample.addParentRelationship(new SampleRelationshipPE(parent, sample, relationshipType,
                actor));
    }

    @Override
    public void removeParentFromSample(IAuthSession session, SampleIdentifier sampleId,
            SampleIdentifier parentId)
    {
        SamplePE sample = findSample(sampleId);
        SamplePE parent = findSample(parentId);
        for (SampleRelationshipPE relationship : sample.getParentRelationships())
        {
            if (relationship.getParentSample().equals(parent))
            {
                sample.removeParentRelationship(relationship);
                return;
            }
        }
        throw UserFailureException.fromTemplate(ERR_SAMPLE_PARENT_RELATIONSHIP_NOT_FOUND, sampleId,
                parentId);
    }

    @Override
    public void assignSampleToContainer(IAuthSession session, SampleIdentifier sampleId,
            SamplePE sample,
            SampleIdentifier containerId, SamplePE container)
    {
        // SamplePE sample = findSample(sampleId);
        // SamplePE container = findSample(containerId);
        sample.setContainer(container);
    }

    @Override
    public void removeSampleFromContainer(IAuthSession session, SampleIdentifier sampleId,
            SamplePE sample)
    {
        // SamplePE sample = findSample(sampleId);
        sample.setContainer(null);
    }

    private DataPE findDataSet(String dataSetCode)
    {
        DataPE data = daoFactory.getDataDAO().tryToFindDataSetByCode(dataSetCode);
        if (data == null)
        {
            throw UserFailureException.fromTemplate(ERR_DATASET_NOT_FOUND, dataSetCode);
        }
        return data;
    }

    private SamplePE findSample(SampleIdentifier sampleId)
    {
        SamplePE sample;
        if (sampleId.getDatabaseInstanceLevel() != null)
        {
            DatabaseInstancePE dbin = findDatabaseInstance(sampleId.getDatabaseInstanceLevel());
            sample = daoFactory.getSampleDAO().tryFindByCodeAndDatabaseInstance(
                    sampleId.getSampleCode(),
                    dbin);
        } else
        {
            SpacePE space = findSpace(sampleId.getSpaceLevel());
            sample =
                    daoFactory.getSampleDAO()
                            .tryFindByCodeAndSpace(sampleId.getSampleCode(), space);
        }

        if (sample == null)
        {
            throw UserFailureException.fromTemplate(ERR_SAMPLE_NOT_FOUND, sampleId);
        }

        return sample;
    }

    private ExperimentPE findExperiment(ExperimentIdentifier experimentId)
    {
        ProjectPE project = findProject(experimentId);
        ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryFindByCodeAndProject(project,
                        experimentId.getExperimentCode());
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(ERR_EXPERIMENT_NOT_FOUND, experimentId);
        }

        return experiment;
    }

    private ProjectPE findProject(ProjectIdentifier projectId)
    {
        ProjectPE project =
                daoFactory.getProjectDAO().tryFindProject(projectId.getDatabaseInstanceCode(),
                        projectId.getSpaceCode(), projectId.getProjectCode());

        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, projectId);
        }
        return project;
    }

    private SpacePE findSpace(SpaceIdentifier spaceId)
    {
        DatabaseInstancePE dbin = findDatabaseInstance(spaceId);
        SpacePE space =
                daoFactory.getSpaceDAO().tryFindSpaceByCodeAndDatabaseInstance(
                        spaceId.getSpaceCode(), dbin);
        if (space == null)
        {
            throw UserFailureException.fromTemplate(ERR_SPACE_NOT_FOUND, spaceId);
        }

        return space;
    }

    private DatabaseInstancePE findDatabaseInstance(DatabaseInstanceIdentifier dbinId)
    {
        DatabaseInstancePE dbin =
                daoFactory.getDatabaseInstanceDAO().tryFindDatabaseInstanceByCode(
                        dbinId.getDatabaseInstanceCode());

        if (dbin == null)
        {
            throw UserFailureException.fromTemplate(ERR_DATABASE_NOT_FOUND, dbinId);
        }
        return dbin;
    }

    public void setDaoFactory(DAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }
}
