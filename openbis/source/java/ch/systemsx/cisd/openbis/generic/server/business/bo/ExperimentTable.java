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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The only productive implementation of {@link IExperimentTable}.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentTable extends AbstractBusinessObject implements IExperimentTable
{
    private List<ExperimentPE> experiments;

    private boolean dataChanged = false;

    private IRelationshipService relationshipService;

    ExperimentTable(final IDAOFactory daoFactory, final Session session,
            IEntityPropertiesConverter converter)
    {
        super(daoFactory, session, converter);
    }

    public ExperimentTable(final IDAOFactory daoFactory, final Session session,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, EntityKind.EXPERIMENT);
        this.relationshipService = relationshipService;
    }

    //
    // IExperimentTable
    //

    @Override
    public final void load(final String experimentTypeCode,
            final ProjectIdentifier projectIdentifier)
    {
        load(experimentTypeCode, projectIdentifier, false, false);
    }

    @Override
    public final void load(final String experimentTypeCode,
            final ProjectIdentifier projectIdentifier, boolean onlyHavingSamples,
            boolean onlyHavingDataSets)
    {
        checkNotNull(experimentTypeCode, projectIdentifier);
        fillSpaceIdentifier(projectIdentifier);
        final ProjectPE project =
                getProjectDAO().tryFindProject(projectIdentifier.getDatabaseInstanceCode(),
                        projectIdentifier.getSpaceCode(), projectIdentifier.getProjectCode());
        checkNotNull(projectIdentifier, project);
        if (EntityType.isAllTypesCode(experimentTypeCode))
        {
            experiments =
                    getExperimentDAO().listExperimentsWithProperties(project, onlyHavingSamples,
                            onlyHavingDataSets);
        } else
        {
            final EntityTypePE entityType =
                    getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(
                            experimentTypeCode);
            checkNotNull(experimentTypeCode, entityType);
            experiments =
                    getExperimentDAO().listExperimentsWithProperties((ExperimentTypePE) entityType,
                            project, null, onlyHavingSamples, onlyHavingDataSets);
        }
    }

    @Override
    public final void load(final String experimentTypeCode, final SpaceIdentifier spaceIdentifier)
    {
        checkNotNull(experimentTypeCode, spaceIdentifier);
        fillSpaceIdentifier(spaceIdentifier);
        final SpacePE space =
                getSpaceDAO().tryFindSpaceByCodeAndDatabaseInstance(spaceIdentifier.getSpaceCode(),
                        getHomeDatabaseInstance());
        checkNotNull(spaceIdentifier, space);
        if (EntityType.isAllTypesCode(experimentTypeCode))
        {
            experiments = getExperimentDAO().listExperimentsWithProperties(space);
        } else
        {
            final EntityTypePE entityType =
                    getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(
                            experimentTypeCode);
            checkNotNull(experimentTypeCode, entityType);
            experiments =
                    getExperimentDAO().listExperimentsWithProperties((ExperimentTypePE) entityType,
                            null, space);
        }
    }

    @Override
    public final void load(final Collection<ExperimentIdentifier> identifiers)
    {
        checkNotEmpty(identifiers);
        experiments = listExperimentsByIdentifiers(identifiers);
    }

    private void checkNotNull(final SpaceIdentifier spaceIdentifier, final SpacePE space)
    {
        if (space == null)
        {
            throw new UserFailureException("Space '" + spaceIdentifier + "' unknown.");
        }
    }

    private void checkNotNull(final ProjectIdentifier projectIdentifier, final ProjectPE project)
    {
        if (project == null)
        {
            throw new UserFailureException("Project '" + projectIdentifier + "' unknown.");
        }
    }

    private void checkNotNull(final String experimentTypeCode, final EntityTypePE entityType)
    {
        if (entityType == null)
        {
            throw new UserFailureException("Unknown experiment type '" + experimentTypeCode + "'.");
        }
    }

    private void checkNotNull(final String experimentTypeCode,
            final SpaceIdentifier projectIdentifier)
    {
        if (experimentTypeCode == null)
        {
            throw new UserFailureException("Experiment type not specified.");
        }
        if (projectIdentifier == null)
        {
            throw new UserFailureException("Project not specified.");
        }
    }

    private void checkNotEmpty(Collection<ExperimentIdentifier> identifiers)
    {
        if (identifiers == null || identifiers.isEmpty())
        {
            throw new UserFailureException("Experiment identifiers cannot be NULL or empty.");
        }
    }

    @Override
    public final List<ExperimentPE> getExperiments()
    {
        assert experiments != null : "Experiments have not been loaded.";
        return experiments;
    }

    @Override
    public void add(List<NewBasicExperiment> entities, ExperimentTypePE experimentTypePE)
    {
        experiments = new ArrayList<ExperimentPE>();
        setBatchUpdateMode(true);
        for (NewBasicExperiment ne : entities)
        {
            experiments.add(createExperiment(ne, experimentTypePE));
        }
        setBatchUpdateMode(false);
        dataChanged = true;
    }

    @Override
    public void prepareForUpdate(List<ExperimentBatchUpdatesDTO> updates)
    {
        assert updates != null : "Unspecified experiments.";

        setBatchUpdateMode(true);
        experiments = loadExperiments(updates);
        Map<String, ExperimentPE> experimentsByIdentifier = new HashMap<String, ExperimentPE>();
        for (ExperimentPE experiment : experiments)
        {
            experimentsByIdentifier.put(experiment.getIdentifier(), experiment);
        }
        for (ExperimentBatchUpdatesDTO experimentUpdates : updates)
        {
            final ExperimentPE sample =
                    experimentsByIdentifier.get(experimentUpdates.getOldExperimentIdentifier()
                            .toString());
            prepareBatchUpdate(sample, experimentUpdates);
        }

        dataChanged = true;

        setBatchUpdateMode(false);
    }

    private void prepareBatchUpdate(ExperimentPE experiment, ExperimentBatchUpdatesDTO updates)
    {
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment could be found with given identifier '%s'.",
                    updates.getOldExperimentIdentifier());
        }
        ExperimentBatchUpdateDetails details = updates.getDetails();
        batchUpdateProperties(experiment, updates.getProperties(), details.getPropertiesToUpdate());

        if (updates.isProjectUpdateRequested())
        {
            ProjectPE previousProject = experiment.getProject();
            ProjectPE project = findProject(updates.getProjectIdentifier());

            if (previousProject.equals(project))
            {
                return;
            }
            relationshipService.assignExperimentToProject(session, experiment, project);
        }
    }

    private ProjectPE findProject(ProjectIdentifier newProjectIdentifier)
    {
        ProjectPE project =
                getProjectDAO().tryFindProject(newProjectIdentifier.getDatabaseInstanceCode(),
                        newProjectIdentifier.getSpaceCode(), newProjectIdentifier.getProjectCode());
        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, newProjectIdentifier);
        }
        return project;
    }

    private void batchUpdateProperties(ExperimentPE experiment, List<IEntityProperty> properties,
            Set<String> propertiesToUpdate)
    {
        final Set<ExperimentPropertyPE> existingProperties = experiment.getProperties();
        final ExperimentTypePE type = experiment.getExperimentType();
        final PersonPE registrator = findPerson();
        experiment.setProperties(entityPropertiesConverter.updateProperties(existingProperties,
                type, properties, registrator, propertiesToUpdate));

    }

    private List<ExperimentPE> loadExperiments(List<ExperimentBatchUpdatesDTO> updates)
    {
        final List<ExperimentPE> results = new ArrayList<ExperimentPE>();
        List<ExperimentIdentifier> identifiers = new ArrayList<ExperimentIdentifier>();
        for (ExperimentBatchUpdatesDTO sampleUpdates : updates)
        {
            ExperimentIdentifier identifier = sampleUpdates.getOldExperimentIdentifier();
            identifiers.add(identifier);
        }
        results.addAll(listExperimentsByIdentifiers(identifiers));
        return results;
    }

    protected List<ExperimentPE> listExperimentsByIdentifiers(
            final Collection<ExperimentIdentifier> experimentIdentifiers)
    {
        assert experimentIdentifiers != null : "Experiment identifiers unspecified.";

        // Get the experiments we want by getting all experiments and then filtering down.
        final IExperimentDAO experimentDAO = getExperimentDAO();
        final List<ExperimentPE> results = new ArrayList<ExperimentPE>();
        List<ExperimentPE> allExperiments = experimentDAO.listExperiments();
        HashSet<String> desiredIds = new HashSet<String>(experimentIdentifiers.size());
        for (ExperimentIdentifier experimentId : experimentIdentifiers)
        {
            desiredIds.add(experimentId.toString());
        }
        for (ExperimentPE experiment : allExperiments)
        {
            if (desiredIds.contains(experiment.getIdentifier()))
            {
                results.add(experiment);
            }
        }

        return results;
    }

    private ExperimentPE createExperiment(NewBasicExperiment newExperiment,
            ExperimentTypePE experimentTypePE)
    {
        final ExperimentPE result = new ExperimentPE();
        result.setExperimentType(experimentTypePE);
        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(newExperiment.getIdentifier()).createIdentifier();
        result.setCode(experimentIdentifier.getExperimentCode());
        final PersonPE registrator = findPerson();
        result.setRegistrator(registrator);
        defineExperimentProperties(result, experimentTypePE.getCode(),
                newExperiment.getProperties(), registrator);
        defineExperimentProject(result, experimentIdentifier);
        result.setPermId(getOrCreatePermID(newExperiment));
        return result;
    }

    static final String ERR_PROJECT_NOT_FOUND =
            "No project for experiment '%s' could be found in the database.";

    private void defineExperimentProject(ExperimentPE result,
            final ExperimentIdentifier experimentIdentifier)
    {
        ProjectPE project =
                getProjectDAO().tryFindProject(experimentIdentifier.getDatabaseInstanceCode(),
                        experimentIdentifier.getSpaceCode(), experimentIdentifier.getProjectCode());
        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, experimentIdentifier);
        }
        result.setProject(project);
    }

    private final void defineExperimentProperties(ExperimentPE result,
            final String experimentTypeCode, final IEntityProperty[] experimentProperties,
            PersonPE registrator)
    {
        final List<ExperimentPropertyPE> properties =
                entityPropertiesConverter.convertProperties(experimentProperties,
                        experimentTypeCode, registrator);
        for (final ExperimentPropertyPE experimentProperty : properties)
        {
            result.addProperty(experimentProperty);
        }
    }

    @Override
    public void save()
    {
        assert experiments != null : "Experiments not loaded.";
        assert dataChanged : "Data has not been changed.";
        try
        {
            checkBusinessRules();
            getExperimentDAO().createOrUpdateExperiments(experiments, findPerson());
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of experiments"));
        }
        dataChanged = false;
    }

    private void checkBusinessRules()
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        for (ExperimentPE m : experiments)
        {
            entityPropertiesConverter.checkMandatoryProperties(m.getProperties(),
                    m.getExperimentType(), cache);
        }
    }

}
