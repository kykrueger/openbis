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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.shared.DataType;
import ch.systemsx.cisd.openbis.generic.client.shared.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.shared.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.Person;
import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.Vocabulary;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.PropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.RoleCodeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SearchableEntityTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.TSVRenderer;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.AbstractClientService;

/**
 * The {@link ICommonClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonClientService extends AbstractClientService implements
        ICommonClientService
{
    private final ICommonServer commonServer;

    public CommonClientService(final ICommonServer commonServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.commonServer = commonServer;
    }

    @Override
    protected final IServer getServer()
    {
        return commonServer;
    }

    // ----------- export and listing with cache generic functionality

    @SuppressWarnings("unchecked")
    private final <K> IResultSetManager<K> getResultSetManager()
    {
        return (IResultSetManager<K>) getHttpSession().getAttribute(
                SessionConstants.OPENBIS_RESULT_SET_MANAGER);
    }

    @SuppressWarnings("unchecked")
    private final <T> CacheManager<String, T> getExportManager()
    {
        return (CacheManager<String, T>) getHttpSession().getAttribute(
                SessionConstants.OPENBIS_EXPORT_MANAGER);
    }

    /**
     * Returns and removes cached export criteria.
     */
    private final <T> TableExportCriteria<T> getAndRemoveExportCriteria(final String exportDataKey)
    {
        final CacheManager<String, TableExportCriteria<T>> exportManager = getExportManager();
        final TableExportCriteria<T> exportCriteria = exportManager.tryGetData(exportDataKey);
        assert exportCriteria != null : "No export criteria found at key " + exportDataKey;
        getExportManager().removeData(exportDataKey);
        return exportCriteria;
    }

    private final <T> List<T> fetchCachedEntities(final TableExportCriteria<T> exportCriteria)
    {
        final IResultSetManager<String> resultSetManager = getResultSetManager();
        IResultSetConfig<String, T> resultSetConfig = createExportListCriteria(exportCriteria);
        IOriginalDataProvider<T> dummyDataProvider = createDummyDataProvider();
        final IResultSet<String, T> result =
                resultSetManager.getResultSet(resultSetConfig, dummyDataProvider);
        final ResultSet<T> entities = ResultSetTranslator.translate(result);
        return entities.getList();
    }

    private static <T> IOriginalDataProvider<T> createDummyDataProvider()
    {
        return new IOriginalDataProvider<T>()
            {
                public List<T> getOriginalData() throws UserFailureException
                {
                    throw new IllegalStateException("Data not found in the cache");
                }
            };
    }

    private static <T> IResultSetConfig<String, T> createExportListCriteria(
            final TableExportCriteria<T> exportCriteria)
    {
        final DefaultResultSetConfig<String, T> criteria = DefaultResultSetConfig.createFetchAll();
        criteria.setSortInfo(exportCriteria.getSortInfo());
        criteria.setFilterInfos(exportCriteria.getFilterInfos());
        criteria.setResultSetKey(exportCriteria.getResultSetKey());
        return criteria;
    }

    private <T> ResultSet<T> listEntities(final IResultSetConfig<String, T> criteria,
            IOriginalDataProvider<T> dataProvider)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final IResultSetManager<String> resultSetManager = getResultSetManager();
            final IResultSet<String, T> result =
                    resultSetManager.getResultSet(criteria, dataProvider);
            return ResultSetTranslator.translate(result);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    /**
     * Assumes that preparation of the export ({@link #prepareExportSamples(TableExportCriteria)}
     * has been invoked before and returned with an exportDataKey passed here as a parameter.
     */
    public final String getExportTable(final String exportDataKey)
    {
        // NOTE: no generics in GWT
        return getGenericExportTable(exportDataKey);
    }

    private final <T> String getGenericExportTable(final String exportDataKey)
    {
        try
        {
            // Not directly needed but this refreshes the session.
            getSessionToken();
            final TableExportCriteria<T> exportCriteria = getAndRemoveExportCriteria(exportDataKey);
            final List<T> entities = fetchCachedEntities(exportCriteria);
            return TSVRenderer.createTable(entities, exportCriteria.getColumnDefs());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private <T> String prepareExportEntities(TableExportCriteria<T> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            // Not directly needed but this refreshes the session.
            getSessionToken();
            final CacheManager<String, TableExportCriteria<T>> exportManager = getExportManager();
            return exportManager.saveData(criteria);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void removeResultSet(final String resultSetKey)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            // Not directly needed but this refreshes the session.
            getSessionToken();
            getResultSetManager().removeResultSet(resultSetKey);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // --------------- end export & listing

    //
    // IGenericClientService
    //

    public final List<Group> listGroups(final String databaseInstanceCode)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final DatabaseInstanceIdentifier identifier =
                    new DatabaseInstanceIdentifier(databaseInstanceCode);
            final List<Group> result = new ArrayList<Group>();
            final List<GroupPE> groups = commonServer.listGroups(sessionToken, identifier);
            for (final GroupPE group : groups)
            {
                result.add(GroupTranslator.translate(group));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroup(final String groupCode, final String descriptionOrNull,
            final String groupLeaderOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerGroup(sessionToken, groupCode, descriptionOrNull,
                    groupLeaderOrNull);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<Person> listPersons()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {

        try
        {
            final String sessionToken = getSessionToken();
            final List<Person> result = new ArrayList<Person>();
            final List<PersonPE> persons = commonServer.listPersons(sessionToken);
            for (final PersonPE person : persons)
            {
                result.add(PersonTranslator.translate(person));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerPerson(final String code)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerPerson(sessionToken, code);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<RoleAssignment> listRoles()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<RoleAssignment> result = new ArrayList<RoleAssignment>();
            final List<RoleAssignmentPE> roles = commonServer.listRoles(sessionToken);
            for (final RoleAssignmentPE role : roles)
            {
                result.add(RoleAssignmentTranslator.translate(role));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroupRole(final String roleSetCode, final String group,
            final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            commonServer.registerGroupRole(sessionToken, RoleCodeTranslator.translate(roleSetCode),
                    groupIdentifier, person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerInstanceRole(final String roleSetCode, final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerInstanceRole(sessionToken, RoleCodeTranslator
                    .translate(roleSetCode), person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void deleteGroupRole(final String roleSetCode, final String group,
            final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            commonServer.deleteGroupRole(sessionToken, RoleCodeTranslator.translate(roleSetCode),
                    groupIdentifier, person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final void deleteInstanceRole(final String roleSetCode, final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteInstanceRole(sessionToken,
                    RoleCodeTranslator.translate(roleSetCode), person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final List<SampleType> listSampleTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<SampleTypePE> sampleTypes = commonServer.listSampleTypes(sessionToken);
            final List<SampleType> result = new ArrayList<SampleType>();
            for (final SampleTypePE sampleTypePE : sampleTypes)
            {
                result.add(SampleTypeTranslator.translate(sampleTypePE));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // --------- methods preparing exported content. Note: GWT does not support generic methods :(

    public final String prepareExportSamples(final TableExportCriteria<Sample> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public final String prepareExportExperiments(final TableExportCriteria<Experiment> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public final String prepareExportMatchingEntities(
            final TableExportCriteria<MatchingEntity> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportPropertyTypes(TableExportCriteria<PropertyType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportPropertyTypeAssignments(
            TableExportCriteria<EntityTypePropertyType<?>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportProjects(TableExportCriteria<Project> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportVocabularies(final TableExportCriteria<Vocabulary> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    // ---------------- methods which list entities using cache

    public final ResultSet<Sample> listSamples(final ListSampleCriteria listCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(listCriteria, new ListSamplesOriginalDataProvider(commonServer,
                sessionToken, listCriteria));
    }

    public final ResultSet<Experiment> listExperiments(final ListExperimentsCriteria listCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(listCriteria, new ListExperimentsOriginalDataProvider(commonServer,
                listCriteria, sessionToken));
    }

    public ResultSet<PropertyType> listPropertyTypes(
            DefaultResultSetConfig<String, PropertyType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<PropertyType>()
            {
                public List<PropertyType> getOriginalData() throws UserFailureException
                {
                    return listPropertyTypes();
                }
            });
    }

    public final ResultSet<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText,
            final IResultSetConfig<String, MatchingEntity> resultSetConfig)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[] matchingEntities =
                SearchableEntityTranslator.translate(searchableEntityOrNull);
        return listEntities(resultSetConfig, new ListMatchingEntitiesOriginalDataProvider(
                commonServer, sessionToken, matchingEntities, queryText));
    }

    public ResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignments(
            DefaultResultSetConfig<String, EntityTypePropertyType<?>> criteria)
    {
        return listEntities(criteria, new IOriginalDataProvider<EntityTypePropertyType<?>>()
            {
                public List<EntityTypePropertyType<?>> getOriginalData()
                        throws UserFailureException
                {
                    return extractAssignments(listPropertyTypes());
                }
            });
    }

    private static List<EntityTypePropertyType<?>> extractAssignments(
            List<PropertyType> listPropertyTypes)
    {
        List<EntityTypePropertyType<?>> result = new ArrayList<EntityTypePropertyType<?>>();
        for (PropertyType propertyType : listPropertyTypes)
        {
            extractAssignments(result, propertyType);
        }
        return result;
    }

    private static void extractAssignments(List<EntityTypePropertyType<?>> result,
            final PropertyType propertyType)
    {
        for (ExperimentTypePropertyType etpt : propertyType.getExperimentTypePropertyTypes())
        {
            result.add(etpt);
        }
        for (SampleTypePropertyType etpt : propertyType.getSampleTypePropertyTypes())
        {
            result.add(etpt);
        }
        for (MaterialTypePropertyType etpt : propertyType.getMaterialTypePropertyTypes())
        {
            result.add(etpt);
        }
    }

    public ResultSet<Project> listProjects(DefaultResultSetConfig<String, Project> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<Project>()
            {
                public List<Project> getOriginalData() throws UserFailureException
                {
                    return listProjects();
                }
            });
    }

    private List<Project> listProjects()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<ProjectPE> projects = commonServer.listProjects(sessionToken);
            return ProjectTranslator.translate(projects);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<Vocabulary> listVocabularies(final boolean withTerms,
            final boolean excludeInternal, DefaultResultSetConfig<String, Vocabulary> criteria)
            throws UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<Vocabulary>()
            {
                public List<Vocabulary> getOriginalData() throws UserFailureException
                {
                    return listVocabularies(withTerms, excludeInternal);
                }
            });
    }

    private List<Vocabulary> listVocabularies(final boolean withTerms, boolean excludeInternal)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<VocabularyPE> vocabularies =
                    commonServer.listVocabularies(sessionToken, withTerms, excludeInternal);
            return BeanUtils.createBeanList(Vocabulary.class, vocabularies, DtoConverters
                    .getVocabularyConverter());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // ---------------- end list using cache ----------

    public final List<ExternalData> listExternalData(final String sampleIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
            final List<ExternalDataPE> externalData =
                    commonServer.listExternalData(sessionToken, identifier);
            return ExternalDataTranslator.translate(externalData);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<ExternalData> listExternalDataForExperiment(String experimentIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experimentIdentifier).createIdentifier();
            final List<ExternalDataPE> externalData =
                    commonServer.listExternalData(sessionToken, identifier);
            return ExternalDataTranslator.translate(externalData);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<SearchableEntity> listSearchableEntities()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            // Not directly needed but this refreshes the session.
            getSessionToken();
            final List<SearchableEntity> searchableEntities =
                    BeanUtils.createBeanList(SearchableEntity.class, Arrays
                            .asList(ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity
                                    .values()));
            Collections.sort(searchableEntities);
            return searchableEntities;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<ExperimentType> listExperimentTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<ExperimentType> result = new ArrayList<ExperimentType>();
            final List<ExperimentTypePE> projects = commonServer.listExperimentTypes(sessionToken);
            for (final ExperimentTypePE expType : projects)
            {
                result.add(ExperimentTranslator.translate(expType));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private List<PropertyType> listPropertyTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<PropertyTypePE> propertyTypes = commonServer.listPropertyTypes(sessionToken);
            return PropertyTypeTranslator.translate(propertyTypes);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<DataType> listDataTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<DataTypePE> dataTypes = commonServer.listDataTypes(sessionToken);
            return BeanUtils.createBeanList(DataType.class, dataTypes, DtoConverters
                    .getDataTypeConverter());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public String assignPropertyType(final EntityKind entityKind, final String propertyTypeCode,
            final String entityTypeCode, final boolean isMandatory, final String defaultValue)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return commonServer.assignPropertyType(sessionToken, DtoConverters
                    .convertEntityKind(entityKind), propertyTypeCode, entityTypeCode, isMandatory,
                    defaultValue);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerPropertyType(final PropertyType propertyType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert propertyType != null : "Unspecified property type.";
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerPropertyType(sessionToken, propertyType);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerVocabulary(final Vocabulary vocabulary)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabulary != null : "Unspecified vocabulary.";
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerVocabulary(sessionToken, vocabulary);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void registerProject(Project project)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert project != null : "Unspecified project.";
        try
        {
            final String sessionToken = getSessionToken();
            final ProjectIdentifier projectIdentifier =
                    new ProjectIdentifierFactory(project.getIdentifier()).createIdentifier();
            Person leader = project.getProjectLeader();
            final String leaderId = leader == null ? null : project.getProjectLeader().getUserId();
            commonServer.registerProject(sessionToken, projectIdentifier, project.getDescription(),
                    leaderId);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }
}
