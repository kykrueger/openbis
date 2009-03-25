/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Logger class for {@link CommonServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
final class CommonServerLogger extends AbstractServerLogger implements ICommonServer
{
    /**
     * Creates an instance for the specified session manager and invocation status. The session
     * manager is used to retrieve user information which will be a part of the log message.
     */
    CommonServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful)
    {
        super(sessionManager, invocationSuccessful);
    }

    //
    // IGenericServer
    //

    public List<GroupPE> listGroups(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final String command = "list_groups";
        if (identifier == null || identifier.getDatabaseInstanceCode() == null)
        {
            logAccess(sessionToken, command);
        } else
        {
            logAccess(sessionToken, command, "DATABASE-INSTANCE(%s)", identifier);
        }
        return null;
    }

    public void registerGroup(final String sessionToken, final String groupCode,
            final String descriptionOrNull, final String groupLeaderOrNull)
    {
        logTracking(sessionToken, "register_group", "CODE(%s)", groupCode);
    }

    public List<PersonPE> listPersons(final String sessionToken)
    {
        logAccess(sessionToken, "list_persons");
        return null;
    }

    public void registerPerson(final String sessionToken, final String userID)
    {
        logTracking(sessionToken, "register_person", "CODE(%s)", userID);

    }

    public List<RoleAssignmentPE> listRoles(final String sessionToken)
    {
        logAccess(sessionToken, "list_roles");
        return null;
    }

    public void registerGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s) GROUP(%s) PERSON(%s)", roleCode,
                groupIdentifier, person);

    }

    public void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s)  PERSON(%s)", roleCode, person);

    }

    public void deleteGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) GROUP(%s) PERSON(%s)", roleCode,
                groupIdentifier, person);

    }

    public void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) PERSON(%s)", roleCode, person);

    }

    public final List<SampleTypePE> listSampleTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_sample_types");
        return null;
    }

    public final List<SamplePE> listSamples(final String sessionToken,
            final ListSampleCriteriaDTO criteria)
    {
        logAccess(sessionToken, "list_samples", "TYPE(%s) OWNERS(%s) CONTAINER(%s) EXPERIMENT(%s)",
                criteria.getSampleType(), criteria.getOwnerIdentifiers(), criteria
                        .getContainerIdentifier(), criteria.getExperimentIdentifier());
        return null;
    }

    public final List<SamplePropertyPE> listSamplesProperties(final String sessionToken,
            final ListSampleCriteriaDTO criteria, final List<PropertyTypePE> propertyCodes)
    {
        logAccess(sessionToken, "list_samples_properties", "CRITERIA(%s) PROPERTIES(%s)", criteria,
                propertyCodes.size());
        return null;
    }

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final List<ExternalDataPE> listExternalData(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "list_external_data", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public List<ExternalDataPE> listExternalData(String sessionToken,
            ExperimentIdentifier identifier)
    {
        logAccess(sessionToken, "list_external_data", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final List<SearchHit> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText)
    {
        logAccess(sessionToken, "list_matching_entities", "SEARCHABLE-ENTITIES(%s) QUERY-TEXT(%s)",
                Arrays.toString(searchableEntities), queryText);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S)", newSample
                .getSampleType(), newSample.getIdentifier());
    }

    public List<ExperimentPE> listExperiments(final String sessionToken,
            final ExperimentTypePE experimentType, final ProjectIdentifier project)
    {
        logAccess(sessionToken, "list_experiments", "TYPE(%s) PROJECT(%s)", experimentType, project);
        return null;
    }

    public List<ProjectPE> listProjects(final String sessionToken)
    {
        logAccess(sessionToken, "list_projects");
        return null;
    }

    public List<ExperimentTypePE> listExperimentTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_experiment_types");
        return null;
    }

    public List<PropertyTypePE> listPropertyTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_property_types");
        return null;
    }

    public final List<DataTypePE> listDataTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_data_types");
        return null;
    }

    public final List<VocabularyPE> listVocabularies(final String sessionToken, boolean withTerms,
            boolean excludeInternal)
    {
        logAccess(sessionToken, "list_vocabularies");
        return null;
    }

    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logTracking(sessionToken, "assign_property_type", " PROPERTY_TYPE(%S) " + entityTypeFormat
                + " MANDATORY(%S) DEFAULT(%S)", propertyTypeCode, entityTypeCode, isMandatory,
                defaultValue);
        return null;
    }

    public final void registerPropertyType(final String sessionToken,
            final PropertyType propertyType)
    {
        logTracking(sessionToken, "register_property_type", "PROPERTY_TYPE(%s)", propertyType
                .getCode());
    }

    public final void registerVocabulary(final String sessionToken, final Vocabulary vocabulary)
    {
        logTracking(sessionToken, "register_vocabulary", "VOCABULARY(%s)", vocabulary.getCode());
    }

    public void addVocabularyTerms(String sessionToken, String vocabularyCode,
            List<String> vocabularyTerms)
    {
        logTracking(sessionToken, "add_vocabulary_terms", "VOCABULARY(%s) NUMBER_OF_TERMS(%s)",
                vocabularyCode, Integer.toString(vocabularyTerms.size()));
    }

    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId)
    {
        logTracking(sessionToken, "register_project", "PROJECT(%s)", projectIdentifier);
    }

    public List<ExternalDataPE> searchForDataSets(String sessionToken,
            DataSetSearchCriteria criteria)
    {
        logAccess(sessionToken, "search_for_datasets");
        return null;
    }

    public List<MaterialTypePE> listMaterialTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_material_types");
        return null;
    }

    public List<MaterialPE> listMaterials(String sessionToken, MaterialTypePE materialType)
    {
        logAccess(sessionToken, "list_materials", "TYPE(%s)", materialType);
        return null;
    }

    public void registerMaterialType(String sessionToken, MaterialType entityType)
    {
        logTracking(sessionToken, "register_material_type", "CODE(%s)", entityType.getCode());
    }

    public void registerSampleType(String sessionToken, SampleType entityType)
    {
        logTracking(sessionToken, "register_sample_type", "CODE(%s)", entityType.getCode());
    }

    public void registerExperimentType(String sessionToken, ExperimentType entityType)
    {
        logTracking(sessionToken, "register_experiment_type", "CODE(%s)", entityType.getCode());
    }

    public void editExperiment(String sessionToken, ExperimentIdentifier experimentIdentifier,
            List<ExperimentProperty> properties, List<AttachmentPE> attachments,
            ProjectIdentifier newProjectIdentifierOrNull, Date version)
    {
        logTracking(sessionToken, "edit_experiment",
                "EXPERIMENT(%s) ATTACHMENTS_ADDED(%s) NEW_PROJECT(%s)", experimentIdentifier,
                attachments.size(), newProjectIdentifierOrNull);
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason)
    {
        logTracking(sessionToken, "delete_data_sets", "CODES(%s) REASON(%s)", dataSetCodes, reason);
    }

    public void uploadDataSets(String sessionToken, List<String> dataSetCodes, String cifexURL,
            String password)
    {
        logTracking(sessionToken, "upload_data_sets", "CODES(%s) CIFEX-URL(%s)", dataSetCodes,
                cifexURL);
    }

    public void editMaterial(String sessionToken, MaterialIdentifier identifier,
            List<MaterialProperty> properties, Date version)
    {
        logTracking(sessionToken, "edit_material", "MATERIAL(%s)", identifier);
    }

    public void editSample(String sessionToken, SampleIdentifier identifier,
            List<SampleProperty> properties, ExperimentIdentifier experimentIdentifierOrNull,
            Date version)
    {
        logTracking(sessionToken, "edit_sample", "SAMPLE(%s), CHANGE_TO_EXPERIMENT(%S)",
                identifier, experimentIdentifierOrNull);

    }

    public List<VocabularyTermWithStats> listVocabularyTerms(String sessionToken,
            Vocabulary vocabulary)
    {
        logAccess(sessionToken, "list_vocabulary_terms", "VOCABULARY(%s)", vocabulary.getCode());
        return null;
    }

    public List<DataSetTypePE> listDataSetTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_data_set_types");
        return null;
    }
}
