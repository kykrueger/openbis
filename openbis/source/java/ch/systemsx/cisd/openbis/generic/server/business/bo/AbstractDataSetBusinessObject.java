/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataSetBusinessObject extends AbstractSampleIdentifierBusinessObject
{

    public AbstractDataSetBusinessObject(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session, EntityKind.DATA_SET);
    }

    public AbstractDataSetBusinessObject(IDAOFactory daoFactory, Session session,
            IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session, entityPropertiesConverter);
    }

    protected void enrichWithParentsAndExperiment(DataPE dataPE)
    {
        HibernateUtils.initialize(dataPE.getParents());
        HibernateUtils.initialize(dataPE.getExperiment());
    }

    protected void enrichWithChildren(DataPE dataPE)
    {
        HibernateUtils.initialize(dataPE.getChildren());
    }

    protected void updateBatchProperties(DataPE data, List<IEntityProperty> newProperties,
            Set<String> set)
    {
        final Set<DataSetPropertyPE> existingProperties = data.getProperties();
        final DataSetTypePE type = data.getDataSetType();
        final PersonPE registrator = findRegistrator();
        data.setProperties(entityPropertiesConverter.updateProperties(existingProperties, type,
                newProperties, registrator, set));
    }

    protected void updateProperties(DataPE data, List<IEntityProperty> newProperties)
    {
        final Set<DataSetPropertyPE> existingProperties = data.getProperties();
        final DataSetTypePE type = data.getDataSetType();
        data.setProperties(convertProperties(type, existingProperties, newProperties));
    }

    protected void checkPropertiesBusinessRules(DataPE data)
    {
        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType());
    }

    protected void updateSample(DataPE data, SampleIdentifier sampleIdentifierOrNull)
    {
        assert sampleIdentifierOrNull != null;
        SamplePE newSample = getSampleByIdentifier(sampleIdentifierOrNull);
        SamplePE previousSampleOrNull = data.tryGetSample();

        if (newSample.equals(previousSampleOrNull))
        {
            return; // nothing to change
        }
        if (newSample.getSpace() == null)
        {
            throw createWrongSampleException(data, newSample, "the new sample is shared");
        }
        ExperimentPE experiment = newSample.getExperiment();
        if (experiment == null)
        {
            throw createWrongSampleException(data, newSample,
                    "the new sample is not connected to any experiment");
        }

        // move dataset to the experiment if needed
        updateExperiment(data, experiment);

        data.setSample(newSample);
    }

    protected void updateExperiment(DataPE data, ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null;
        ExperimentPE experiment = getExperimentByIdentifier(experimentIdentifier);
        updateExperiment(data, experiment);
    }

    protected void updateExperiment(DataPE data, ExperimentPE experiment)
    {
        if (experiment.equals(data.getExperiment()) == false)
        {
            data.setExperiment(experiment);
        }
    }

    protected ExperimentPE getExperimentByIdentifier(final ExperimentIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final ProjectPE project =
                getProjectDAO().tryFindProject(identifier.getDatabaseInstanceCode(),
                        identifier.getSpaceCode(), identifier.getProjectCode());
        if (project == null)
        {
            throw new UserFailureException("Unkown experiment because of unkown project: "
                    + identifier);
        }
        final ExperimentPE exp =
                getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
        return exp;
    }

    protected void updateParents(DataPE data, String[] modifiedParentDatasetCodesOrNull)
    {
        if (modifiedParentDatasetCodesOrNull == null)
        {
            return; // parents were not changed
        } else
        {
            final Set<DataPE> currentParents = data.getParents();
            final Set<String> currentParentCodes = extractCodes(currentParents);
            final Set<String> newCodes = asSet(modifiedParentDatasetCodesOrNull);
            newCodes.removeAll(currentParentCodes);

            // quick check for direct cycle
            if (newCodes.contains(data.getCode()))
            {
                throw new UserFailureException("Data set '" + data.getCode()
                        + "' can not be its own parent.");
            }

            final List<DataPE> parentsToAdd = findDataSetsByCodes(newCodes);
            validateParentsRelationshipGraph(data, parentsToAdd);
            addParents(data, parentsToAdd);

            final Set<String> removedCodes = currentParentCodes;
            removedCodes.removeAll(asSet(modifiedParentDatasetCodesOrNull));
            removeParents(data, filterDataSets(currentParents, removedCodes));
        }
    }

    private void addParents(DataPE data, Collection<DataPE> parentsToAdd)
    {
        for (DataPE parent : parentsToAdd)
        {
            data.addParent(parent);
        }
    }

    private void removeParents(DataPE data, Collection<DataPE> parentsToRemove)
    {
        for (DataPE parent : parentsToRemove)
        {
            data.removeParent(parent);
        }
    }

    /**
     * Throws {@link UserFailureException} if adding specified parents to this data set will create
     * a cycle in data set relationships.
     */
    protected void validateParentsRelationshipGraph(DataPE data, Collection<DataPE> parentsToAdd)
    {
        // DFS from new parents that are to be added to this business object going in direction
        // of parent relationship until:
        // - all related ancestors are visited == graph has no cycles
        // - we get to this business object == cycle is found
        // NOTE: The assumption is that there were no cycles in the graph of relationship before.
        // This algorithm will not find cycles that don't include this business object,
        // although such cycles shouldn't cause it to loop forever.

        // Algorithm operates only on data set ids to make it perform better
        // - there is no need to join DB tables.
        // To be able to inform user about the exact data set that cannot be connected as a parent
        // we need start seeking cycles starting from each parent to be added separately. Otherwise
        // we would need to get invoke more queries to DB (not going layer by layer of graph depth
        // per query) or use BFS instead (which would also be slower in a general case).
        for (DataPE parentToAdd : parentsToAdd)
        {
            validateParentsRelationshipGraph(data, parentToAdd);
        }
    }

    private void validateParentsRelationshipGraph(DataPE data, DataPE parentToAdd)
    {
        final TechId updatedDataSetId = TechId.create(data);
        final Set<TechId> visited = new HashSet<TechId>();
        Set<TechId> toVisit = new HashSet<TechId>();
        toVisit.add(TechId.create(parentToAdd));
        while (toVisit.isEmpty() == false)
        {
            if (toVisit.contains(updatedDataSetId))
            {
                throw UserFailureException.fromTemplate(
                        "Data Set '%s' is an ancestor of Data Set '%s' "
                                + "and cannot be at the same time set as its child.",
                        data.getCode(), parentToAdd.getCode());
            } else
            {
                final Set<TechId> nextToVisit = findParentIds(toVisit);
                visited.addAll(toVisit);
                nextToVisit.removeAll(visited);
                toVisit = nextToVisit;
            }
        }
    }

    private Set<TechId> findParentIds(Set<TechId> dataSetIds)
    {
        return getDataDAO().findParentIds(dataSetIds);
    }

    protected List<DataPE> findDataSetsByCodes(Collection<String> codes)
    {
        final IDataDAO dao = getDataDAO();
        final List<DataPE> dataSets = new ArrayList<DataPE>();
        final List<String> missingDataSetCodes = new ArrayList<String>();
        for (String code : codes)
        {
            DataPE dataSetOrNull = dao.tryToFindDataSetByCode(code);
            if (dataSetOrNull == null)
            {
                missingDataSetCodes.add(code);
            } else
            {
                dataSets.add(dataSetOrNull);
            }
        }
        if (missingDataSetCodes.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Data Sets with following codes do not exist: '%s'.",
                    CollectionUtils.abbreviate(missingDataSetCodes, 10));
        } else
        {
            return dataSets;
        }
    }

    private List<DataPE> filterDataSets(Collection<DataPE> dataSets, Collection<String> seekenCodes)
    {
        List<DataPE> result = new ArrayList<DataPE>();
        for (DataPE dataSet : dataSets)
        {
            if (seekenCodes.contains(dataSet.getCode()))
            {
                result.add(dataSet);
            }
        }
        return result;
    }

    protected void updateContainer(DataPE data, String containerCode)
    {
        if (containerCode == null)
        {
            if (data.getContainer() != null)
            {
                data.getContainer().removeComponent(data);
            }
        } else
        {
            DataPE container = getDataDAO().tryToFindDataSetByCode(containerCode);

            if (container == null)
            {
                throw UserFailureException.fromTemplate(
                        "Data Set with a following code doesn't exist: '%s'.", containerCode);
            }
            if (!container.isContainer())
            {
                throw UserFailureException.fromTemplate(
                        "Data Set with a following code is not a container: '%s'.", containerCode);
            }

            validateContainerContainedRelationshipGraph(container, data);
            container.addComponent(data);
        }
    }

    private void validateContainerContainedRelationshipGraph(DataPE container, DataPE contained)
    {
        if (container.getCode().equals(contained.getCode()))
        {
            throw new UserFailureException(
                    "Data set '"
                            + container.getCode()
                            + "' cannot contain itself as a component neither directly nor via subordinate components.");
        }

        if (container.getContainer() != null)
        {
            validateContainerContainedRelationshipGraph(container.getContainer(), contained);
        }
    }

    protected void updateFileFormatType(DataPE data, String fileFormatTypeCode)
    {
        if (data.isExternalData())
        {
            if (fileFormatTypeCode == null)
            {
                throw new UserFailureException("Data set '" + data.getCode()
                        + "' cannot have empty file format.");
            }
            ExternalDataPE externalData = data.tryAsExternalData();
            FileFormatTypePE fileFormatTypeOrNull =
                    getFileFormatTypeDAO().tryToFindFileFormatTypeByCode(fileFormatTypeCode);
            if (fileFormatTypeOrNull == null)
            {
                throw new UserFailureException(String.format("File type '%s' does not exist.",
                        fileFormatTypeCode));
            } else
            {
                externalData.setFileFormatType(fileFormatTypeOrNull);
            }
        }
    }

    protected void checkSameSpace(DataPE container, DataPE component)
    {
        // see LMS-2282
        if (container.getSpace().equals(component.getSpace()))
        {
            return;
        }
        throw UserFailureException.fromTemplate(
                "Data set '%s' must be in the same space ('%s') as its container.",
                component.getCode(), container.getSpace().getCode());
    }

    protected void checkSameSpace(DataPE container, List<DataPE> components)
    {
        for (DataPE component : components)
        {
            checkSameSpace(container, component);
        }
    }

    protected static Set<String> extractCodes(Collection<DataPE> parents)
    {
        Set<String> codes = new HashSet<String>(parents.size());
        for (DataPE parent : parents)
        {
            codes.add(parent.getCode());
        }
        return codes;
    }

    protected static Set<String> asSet(String[] objects)
    {
        return new LinkedHashSet<String>(Arrays.asList(objects)); // keep the ordering
    }

    private UserFailureException createWrongSampleException(DataPE data, SamplePE sample,
            String reason)
    {
        return UserFailureException.fromTemplate(
                "The dataset '%s' cannot be connected to the sample '%s'" + " because %s.",
                data.getCode(), sample.getIdentifier(), reason);
    }

}
