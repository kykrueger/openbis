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

package ch.systemsx.cisd.openbis.generic.server.business;

import javax.annotation.Resource;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * The unique {@link IRelationshipService} implementation.
 * 
 * @author anttil
 */
public class RelationshipService implements IRelationshipService
{
    private static final String ERR_SAMPLE_PARENT_RELATIONSHIP_NOT_FOUND =
            "Sample '%s' did not have parent '%s'";

    private static final String ERR_DATASET_PARENT_RELATIONSHIP_NOT_FOUND =
            "DataSet '%s' did not have parent '%s'";

    private static final String ERR_DATASET_CONTAINER_RELATIONSHIP_NOT_FOUND =
            "DataSet '%s' did not have container '%s'";

    private DAOFactory daoFactory;

    /**
     * Reference to this instance of service, but as a spring bean, so that we can call methods of this service and run the additional authorization.
     */
    @Resource(name = "relationship-service")
    private IRelationshipService service;

    @Override
    public void assignExperimentToProject(IAuthSession session, ExperimentPE experiment,
            ProjectPE project)
    {
        SampleUtils.setSamplesSpace(experiment, project.getSpace());
        ProjectPE previousProject = experiment.getProject();
        RelationshipUtils.updateModificationDateAndModifier(previousProject, session);
        experiment.setProject(project);
        RelationshipUtils.updateModificationDateAndModifier(project, session);
        RelationshipUtils.updateModificationDateAndModifier(experiment, session);
    }

    @Override
    public void assignProjectToSpace(IAuthSession session, ProjectPE project, SpacePE space)
    {
        project.setSpace(space);
        for (ExperimentPE experiment : project.getExperiments())
        {
            SampleUtils.setSamplesSpace(experiment, space);
        }
    }

    @Override
    public void assignSampleToExperiment(IAuthSession session, SamplePE sample,
            ExperimentPE experiment)
    {
        ExperimentPE currentExperiment = sample.getExperiment();
        if (currentExperiment != null)
        {
            service.checkCanUnassignSampleFromExperiment(session, sample);
            RelationshipUtils.updateModificationDateAndModifier(currentExperiment, session);
        }

        sample.setExperiment(experiment);
        RelationshipUtils.updateModificationDateAndModifier(sample, session);
        RelationshipUtils.updateModificationDateAndModifier(experiment, session);

        for (DataPE dataset : sample.getDatasets())
        {
            RelationshipUtils.setExperimentForDataSet(dataset, experiment, session);
        }
    }

    @Override
    public void checkCanUnassignSampleFromExperiment(IAuthSession session, SamplePE sample)
    {
        // all the logic is done by the authorization mechanism
    }

    @Override
    public void unassignSampleFromExperiment(IAuthSession session, SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();
        if (experiment != null)
        {
            experiment.removeSample(sample);
            RelationshipUtils.updateModificationDateAndModifier(sample, session);
            RelationshipUtils.updateModificationDateAndModifier(experiment, session);
        }
    }

    @Override
    public void assignSampleToSpace(IAuthSession session, SamplePE sample, SpacePE space)
    {
        sample.setDatabaseInstance(null);
        sample.setSpace(space);
        RelationshipUtils.updateModificationDateAndModifier(sample, session);
    }

    @Override
    public void unshareSample(IAuthSession session, SamplePE sample, SpacePE space)
    {
        assignSampleToSpace(session, sample, space);
    }

    @Override
    public void shareSample(IAuthSession session, SamplePE sample)
    {
        SpacePE space = sample.getSpace();
        sample.setSpace(null);
        RelationshipUtils.updateModificationDateAndModifier(sample, session);
    }

    @Override
    public void assignDataSetToExperiment(IAuthSession session, DataPE data, ExperimentPE experiment)
    {
        RelationshipUtils.setExperimentForDataSet(data, experiment, session);
        data.setSample(null);
    }

    @Override
    public void assignDataSetToSample(IAuthSession session, DataPE data, SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();
        RelationshipUtils.setExperimentForDataSet(data, experiment, session);
        RelationshipUtils.setSampleForDataSet(data, sample, session);
    }

    @Override
    public void addParentToSample(IAuthSession session, SamplePE sample, SamplePE parent)
    {
        PersonPE actor = session.tryGetPerson();
        RelationshipTypePE relationshipType =
                daoFactory.getRelationshipTypeDAO().tryFindRelationshipTypeByCode(
                        BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);

        RelationshipUtils.updateModificationDateAndModifier(sample, session);
        RelationshipUtils.updateModificationDateAndModifier(parent, session);
        sample.addParentRelationship(new SampleRelationshipPE(parent, sample, relationshipType,
                actor));
    }

    @Override
    public void removeParentFromSample(IAuthSession session, SamplePE sample, SamplePE parent)
    {
        for (SampleRelationshipPE relationship : sample.getParentRelationships())
        {
            if (relationship.getParentSample().equals(parent))
            {
                RelationshipUtils.updateModificationDateAndModifier(relationship.getChildSample(),
                        session);
                RelationshipUtils.updateModificationDateAndModifier(relationship.getParentSample(),
                        session);
                sample.removeParentRelationship(relationship);
                return;
            }
        }
        throw UserFailureException.fromTemplate(ERR_SAMPLE_PARENT_RELATIONSHIP_NOT_FOUND,
                sample.getCode(), parent.getCode());
    }

    @Override
    public void assignSampleToContainer(IAuthSession session, SamplePE sample, SamplePE container)
    {
        RelationshipUtils.setContainerForSample(sample, container, session);
    }

    @Override
    public void removeSampleFromContainer(IAuthSession session, SamplePE sample)
    {
        RelationshipUtils.setContainerForSample(sample, null, session);
    }

    @Override
    public void addParentToDataSet(IAuthSession session, DataPE data, DataPE parent)
    {
        assignGenericParentChild(session, data, parent, getParentChildRelationshipType(), null);
    }

    private void assignGenericParentChild(IAuthSession session, DataPE child, DataPE parent, RelationshipTypePE type, Integer ordinalOrNull)
    {
        PersonPE actor = session.tryGetPerson();
        DataSetRelationshipPE relationship = new DataSetRelationshipPE(parent, child, type, ordinalOrNull, actor);
        child.addParentRelationship(relationship);
        if (parent.isChildrenRelationshipsInitialized())
        {
            parent.addChildRelationship(relationship);
        }
        RelationshipUtils.updateModificationDateAndModifier(child, session);
        RelationshipUtils.updateModificationDateAndModifier(parent, session);
    }

    @Override
    public void removeParentFromDataSet(IAuthSession session, DataPE data, DataPE parent)
    {
        releaseRelationship(session, BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP, data, parent,
                ERR_DATASET_PARENT_RELATIONSHIP_NOT_FOUND);
    }

    private void releaseRelationship(IAuthSession session, String relationshipTypeCode, DataPE child, DataPE parent, String errorTemplate)
    {
        DataSetRelationshipPE remove = null;
        for (DataSetRelationshipPE r : child.getParentRelationships())
        {
            if (r.getParentDataSet().equals(parent) && r.getRelationshipType().getCode().equals(relationshipTypeCode))
            {
                remove = r;
                break;
            }
        }

        if (remove != null)
        {
            child.removeParentRelationship(remove);
        } else
        {
            throw UserFailureException.fromTemplate(errorTemplate,
                    child.getCode(), parent.getCode());
        }
        RelationshipUtils.updateModificationDateAndModifier(child, session);
        RelationshipUtils.updateModificationDateAndModifier(parent, session);
    }

    @Override
    public void assignDataSetToContainer(IAuthSession session, DataPE data, DataPE container)
    {
        String componentSpace = data.getSpace().getCode();
        String containerSpace = container.getSpace().getCode();
        if (componentSpace.equals(containerSpace) == false)
        {
            throw new UserFailureException("Space '" + containerSpace + "' of the data set container '"
                    + container.getCode() + "' has to be the space '" + componentSpace
                    + "' of the data set component '" + data.getCode() + "'.");
        }
        int ordinal = RelationshipUtils.getContainerComponentRelationships(container.getChildRelationships()).size();
        RelationshipTypePE relationshipType = RelationshipUtils.getContainerComponentRelationshipType(daoFactory.getRelationshipTypeDAO());
        assignGenericParentChild(session, data, container, relationshipType, ordinal);
    }

    @Override
    public void removeDataSetFromContainer(IAuthSession session, DataPE data, DataPE container)
    {
        releaseRelationship(session, BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP, data,
                container, ERR_DATASET_CONTAINER_RELATIONSHIP_NOT_FOUND);
    }

    public void setDaoFactory(DAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    private RelationshipTypePE getParentChildRelationshipType()
    {
        return RelationshipUtils.getParentChildRelationshipType(daoFactory.getRelationshipTypeDAO());
    }

}
