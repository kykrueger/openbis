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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectBO extends AbstractBusinessObject implements IMetaprojectBO
{

    private IExperimentBO experimentBO;

    private ISampleBO sampleBO;

    private IDataBO dataBO;

    private IMaterialBO materialBO;

    private MetaprojectPE metaproject;

    private Map<Class<?>, List<Long>> addedEntitiesIds;

    private Map<Class<?>, List<Long>> removedEntitiesIds;

    private boolean dataChanged;

    public MetaprojectBO(final IDAOFactory daoFactory, IExperimentBO experimentBO,
            ISampleBO sampleBO, IDataBO dataBO, IMaterialBO materialBO, final Session session)
    {
        super(daoFactory, session);

        this.experimentBO = experimentBO;
        this.sampleBO = sampleBO;
        this.dataBO = dataBO;
        this.materialBO = materialBO;
    }

    @Override
    public MetaprojectPE tryFindByMetaprojectId(IMetaprojectId metaprojectId)
    {
        if (metaprojectId == null)
        {
            throw new IllegalArgumentException("Metaproject id cannot be null");
        }
        if (metaprojectId instanceof MetaprojectIdentifierId)
        {
            MetaprojectIdentifierId identifierId = (MetaprojectIdentifierId) metaprojectId;
            MetaprojectIdentifier identifier =
                    MetaprojectIdentifier.parse(identifierId.getIdentifier());
            return getMetaprojectDAO().tryFindByOwnerAndName(identifier.getMetaprojectOwnerId(),
                    identifier.getMetaprojectName());
        } else if (metaprojectId instanceof MetaprojectTechIdId)
        {
            MetaprojectTechIdId techIdId = (MetaprojectTechIdId) metaprojectId;
            return getMetaprojectDAO().tryGetByTechId(new TechId(techIdId.getTechId()));
        } else
        {
            throw new IllegalArgumentException("Unsupported metaproject id: " + metaprojectId);
        }
    }

    @Override
    public void loadDataByTechId(TechId metaprojectId)
    {
        try
        {
            metaproject = getMetaprojectDAO().getByTechId(metaprojectId);
        } catch (ObjectRetrievalFailureException exception)
        {
            throw new UserFailureException(String.format(
                    "Metaproject with ID '%s' does not exist.", metaprojectId));
        }

        initEntitiesMaps();
        dataChanged = false;
    }

    @Override
    public void loadByMetaprojectId(IMetaprojectId metaprojectId)
    {
        metaproject = tryFindByMetaprojectId(metaprojectId);

        if (metaproject == null)
        {
            throw new UserFailureException(String.format(
                    "Metaproject with ID '%s' does not exist.", metaprojectId));
        }

        initEntitiesMaps();
        dataChanged = false;
    }

    @Override
    public MetaprojectPE getMetaproject()
    {
        return metaproject;
    }

    @Override
    public void save() throws UserFailureException
    {
        assert metaproject != null : "Can not save an undefined metaproject.";

        if (dataChanged)
        {
            try
            {
                getMetaprojectDAO().createOrUpdateMetaproject(metaproject, findPerson());

                IFullTextIndexUpdateScheduler indexUpdater =
                        getPersistencyResources().getIndexUpdateScheduler();

                for (Map.Entry<Class<?>, List<Long>> addedEntry : addedEntitiesIds.entrySet())
                {
                    indexUpdater.scheduleUpdate(IndexUpdateOperation.reindex(addedEntry.getKey(),
                            addedEntry.getValue()));
                }

                for (Map.Entry<Class<?>, List<Long>> removedEntry : removedEntitiesIds.entrySet())
                {
                    indexUpdater.scheduleUpdate(IndexUpdateOperation.remove(removedEntry.getKey(),
                            removedEntry.getValue()));
                }

            } catch (final DataAccessException ex)
            {
                throwException(ex, "Metaproject '" + metaproject.getName() + "'");
            }

            dataChanged = false;
        }
    }

    public void define(final String metaprojectName, final String description, final String ownerId)
            throws UserFailureException
    {
        assert metaprojectName != null : "Unspecified metaproject name.";
        assert ownerId != null : "Unspecified metaproject owner";

        this.metaproject = createMetaproject(metaprojectName, description, ownerId);

        initEntitiesMaps();
        dataChanged = true;
    }

    private MetaprojectPE createMetaproject(final String metaprojectName, final String description,
            String ownerId)
    {
        final MetaprojectPE result = new MetaprojectPE();

        result.setName(metaprojectName);
        result.setDescription(description);
        PersonPE owner = getPersonDAO().tryFindPersonByUserId(ownerId);
        if (owner == null)
        {
            throw new UserFailureException("Person '%s' not found in the database.");
        }
        result.setOwner(owner);
        result.setPrivate(true);

        return result;
    }

    @Override
    public void deleteByMetaprojectId(IMetaprojectId metaprojectId) throws UserFailureException
    {
        loadByMetaprojectId(metaprojectId);

        getMetaprojectDAO().delete(metaproject);
        getEventDAO().persist(createDeletionEvent(metaproject, session.tryGetPerson()));
    }

    private static EventPE createDeletionEvent(MetaprojectPE metaproject, PersonPE registrator)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.METAPROJECT);
        event.setIdentifiers(Collections.singletonList(metaproject.getName()));
        event.setDescription(metaproject.getName());
        event.setRegistrator(registrator);

        return event;
    }

    @Override
    public void addExperiments(List<IExperimentId> experimentIds)
    {
        for (IExperimentId experimentId : experimentIds)
        {
            ExperimentPE experimentPE = experimentBO.tryFindByExperimentId(experimentId);
            if (experimentPE == null)
            {
                throw new IllegalArgumentException("Experiment for id: " + experimentId
                        + " doesn't exist.");
            }
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setExperiment(experimentPE);
            metaproject.addAssignment(metaprojectAssignmentPE);
            addToAddedEntities(ExperimentPE.class, experimentPE.getId());
        }

        dataChanged = true;
    }

    @Override
    public void addSamples(List<ISampleId> sampleIds)
    {
        for (ISampleId sampleId : sampleIds)
        {
            SamplePE samplePE = sampleBO.tryFindBySampleId(sampleId);
            if (samplePE == null)
            {
                throw new IllegalArgumentException("Sample for id: " + sampleId + " doesn't exist.");
            }
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setSample(samplePE);
            metaproject.addAssignment(metaprojectAssignmentPE);
            addToAddedEntities(SamplePE.class, samplePE.getId());
        }

        dataChanged = true;
    }

    @Override
    public void addDataSets(List<IDataSetId> dataSetIds)
    {
        for (IDataSetId dataSetId : dataSetIds)
        {
            DataPE dataPE = dataBO.tryFindByDataSetId(dataSetId);
            if (dataPE == null)
            {
                throw new IllegalArgumentException("Data set for id: " + dataSetId
                        + " doesn't exist.");
            }
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setDataSet(dataPE);
            metaproject.addAssignment(metaprojectAssignmentPE);
            addToAddedEntities(DataPE.class, dataPE.getId());
        }

        dataChanged = true;
    }

    @Override
    public void addMaterials(List<IMaterialId> materialIds)
    {
        for (IMaterialId materialId : materialIds)
        {
            MaterialPE materialPE = materialBO.tryFindByMaterialId(materialId);
            if (materialPE == null)
            {
                throw new IllegalArgumentException("Material for id: " + materialId
                        + " doesn't exist.");
            }
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setMaterial(materialPE);
            metaproject.addAssignment(metaprojectAssignmentPE);
            addToAddedEntities(MaterialPE.class, materialPE.getId());
        }

        dataChanged = true;
    }

    @Override
    public void removeExperiments(List<IExperimentId> experimentIds)
    {
        for (IExperimentId experimentId : experimentIds)
        {
            ExperimentPE experimentPE = experimentBO.tryFindByExperimentId(experimentId);
            if (experimentPE != null)
            {
                MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
                metaprojectAssignmentPE.setExperiment(experimentPE);
                metaproject.removeAssignment(metaprojectAssignmentPE);
                addToRemovedEntities(ExperimentPE.class, experimentPE.getId());
            }
        }

        dataChanged = true;
    }

    @Override
    public void removeSamples(List<ISampleId> sampleIds)
    {
        for (ISampleId sampleId : sampleIds)
        {
            SamplePE samplePE = sampleBO.tryFindBySampleId(sampleId);
            if (samplePE != null)
            {
                MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
                metaprojectAssignmentPE.setSample(samplePE);
                metaproject.removeAssignment(metaprojectAssignmentPE);
                addToRemovedEntities(SamplePE.class, samplePE.getId());
            }
        }

        dataChanged = true;
    }

    @Override
    public void removeDataSets(List<IDataSetId> dataSetIds)
    {
        for (IDataSetId dataSetId : dataSetIds)
        {
            DataPE dataPE = dataBO.tryFindByDataSetId(dataSetId);
            if (dataPE != null)
            {
                MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
                metaprojectAssignmentPE.setDataSet(dataPE);
                metaproject.removeAssignment(metaprojectAssignmentPE);
                addToRemovedEntities(DataPE.class, dataPE.getId());
            }
        }

        dataChanged = true;
    }

    @Override
    public void removeMaterials(List<IMaterialId> materialIds)
    {
        for (IMaterialId materialId : materialIds)
        {
            MaterialPE materialPE = materialBO.tryFindByMaterialId(materialId);
            if (materialPE != null)
            {
                MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
                metaprojectAssignmentPE.setMaterial(materialPE);
                metaproject.removeAssignment(metaprojectAssignmentPE);
                addToRemovedEntities(MaterialPE.class, materialPE.getId());
            }
        }

        dataChanged = true;
    }

    private MetaprojectAssignmentPE createAssignement()
    {
        MetaprojectAssignmentPE metaprojectAssignmentPE = new MetaprojectAssignmentPE();
        metaprojectAssignmentPE.setMetaproject(metaproject);
        return metaprojectAssignmentPE;
    }

    private void initEntitiesMaps()
    {
        addedEntitiesIds = new HashMap<Class<?>, List<Long>>();
        removedEntitiesIds = new HashMap<Class<?>, List<Long>>();
    }

    private void addToAddedEntities(Class<?> entityClass, Long entityId)
    {
        addToEntitiesMap(entityClass, entityId, addedEntitiesIds);
    }

    private void addToRemovedEntities(Class<?> entityClass, Long entityId)
    {
        addToEntitiesMap(entityClass, entityId, removedEntitiesIds);
    }

    private void addToEntitiesMap(Class<?> entityClass, Long entityId,
            Map<Class<?>, List<Long>> entitiesMap)
    {
        List<Long> ids = entitiesMap.get(entityClass);
        if (ids == null)
        {
            ids = new ArrayList<Long>();
            entitiesMap.put(entityClass, ids);
        }
        ids.add(entityId);
    }

}
