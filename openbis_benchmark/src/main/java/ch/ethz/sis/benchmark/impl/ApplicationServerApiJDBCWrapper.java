package ch.ethz.sis.benchmark.impl;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.DataSetLockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.DataSetUnlockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.delete.ExternalDmsDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.delete.PluginDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.delete.QueryDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.QueryExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.SqlExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QuerySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.delete.SemanticAnnotationDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update.SemanticAnnotationUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.AggregationServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ProcessingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ReportingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.SearchDomainServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.ICustomASServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.TagDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyUpdate;

import java.util.List;
import java.util.Map;

public class IApplicationServerApiJDBCWrapper implements IApplicationServerApi {

    private IApplicationServerApi instance;
    private String jdbcConnectionString;
    private String jdbcUser;
    private String jdbcPass;

    public


    @Override
    public String login(String s, String s1) {
        return null;
    }

    @Override
    public String loginAs(String s, String s1, String s2) {
        return null;
    }

    @Override
    public String loginAsAnonymousUser() {
        return null;
    }

    @Override
    public void logout(String s) {

    }

    @Override
    public SessionInformation getSessionInformation(String s) {
        return null;
    }

    @Override
    public boolean isSessionActive(String s) {
        return false;
    }

    @Override
    public List<SpacePermId> createSpaces(String s, List<SpaceCreation> list) {
        return null;
    }

    @Override
    public List<ProjectPermId> createProjects(String s, List<ProjectCreation> list) {
        return null;
    }

    @Override
    public List<ExperimentPermId> createExperiments(String s, List<ExperimentCreation> list) {
        return null;
    }

    @Override
    public List<EntityTypePermId> createExperimentTypes(String s, List<ExperimentTypeCreation> list) {
        return null;
    }

    @Override
    public List<SamplePermId> createSamples(String s, List<SampleCreation> list) {
        return null;
    }

    @Override
    public List<EntityTypePermId> createSampleTypes(String s, List<SampleTypeCreation> list) {
        return null;
    }

    @Override
    public List<DataSetPermId> createDataSets(String s, List<DataSetCreation> list) {
        return null;
    }

    @Override
    public List<EntityTypePermId> createDataSetTypes(String s, List<DataSetTypeCreation> list) {
        return null;
    }

    @Override
    public List<MaterialPermId> createMaterials(String s, List<MaterialCreation> list) {
        return null;
    }

    @Override
    public List<EntityTypePermId> createMaterialTypes(String s, List<MaterialTypeCreation> list) {
        return null;
    }

    @Override
    public List<PropertyTypePermId> createPropertyTypes(String s, List<PropertyTypeCreation> list) {
        return null;
    }

    @Override
    public List<PluginPermId> createPlugins(String s, List<PluginCreation> list) {
        return null;
    }

    @Override
    public List<VocabularyPermId> createVocabularies(String s, List<VocabularyCreation> list) {
        return null;
    }

    @Override
    public List<VocabularyTermPermId> createVocabularyTerms(String s, List<VocabularyTermCreation> list) {
        return null;
    }

    @Override
    public List<TagPermId> createTags(String s, List<TagCreation> list) {
        return null;
    }

    @Override
    public List<AuthorizationGroupPermId> createAuthorizationGroups(String s, List<AuthorizationGroupCreation> list) {
        return null;
    }

    @Override
    public List<RoleAssignmentTechId> createRoleAssignments(String s, List<RoleAssignmentCreation> list) {
        return null;
    }

    @Override
    public List<PersonPermId> createPersons(String s, List<PersonCreation> list) {
        return null;
    }

    @Override
    public List<ExternalDmsPermId> createExternalDataManagementSystems(String s, List<ExternalDmsCreation> list) {
        return null;
    }

    @Override
    public List<QueryTechId> createQueries(String s, List<QueryCreation> list) {
        return null;
    }

    @Override
    public List<SemanticAnnotationPermId> createSemanticAnnotations(String s, List<SemanticAnnotationCreation> list) {
        return null;
    }

    @Override
    public void updateSpaces(String s, List<SpaceUpdate> list) {

    }

    @Override
    public void updateProjects(String s, List<ProjectUpdate> list) {

    }

    @Override
    public void updateExperiments(String s, List<ExperimentUpdate> list) {

    }

    @Override
    public void updateExperimentTypes(String s, List<ExperimentTypeUpdate> list) {

    }

    @Override
    public void updateSamples(String s, List<SampleUpdate> list) {

    }

    @Override
    public void updateSampleTypes(String s, List<SampleTypeUpdate> list) {

    }

    @Override
    public void updateDataSets(String s, List<DataSetUpdate> list) {

    }

    @Override
    public void updateDataSetTypes(String s, List<DataSetTypeUpdate> list) {

    }

    @Override
    public void updateMaterials(String s, List<MaterialUpdate> list) {

    }

    @Override
    public void updateMaterialTypes(String s, List<MaterialTypeUpdate> list) {

    }

    @Override
    public void updateExternalDataManagementSystems(String s, List<ExternalDmsUpdate> list) {

    }

    @Override
    public void updatePropertyTypes(String s, List<PropertyTypeUpdate> list) {

    }

    @Override
    public void updatePlugins(String s, List<PluginUpdate> list) {

    }

    @Override
    public void updateVocabularies(String s, List<VocabularyUpdate> list) {

    }

    @Override
    public void updateVocabularyTerms(String s, List<VocabularyTermUpdate> list) {

    }

    @Override
    public void updateTags(String s, List<TagUpdate> list) {

    }

    @Override
    public void updateAuthorizationGroups(String s, List<AuthorizationGroupUpdate> list) {

    }

    @Override
    public void updatePersons(String s, List<PersonUpdate> list) {

    }

    @Override
    public void updateOperationExecutions(String s, List<OperationExecutionUpdate> list) {

    }

    @Override
    public void updateSemanticAnnotations(String s, List<SemanticAnnotationUpdate> list) {

    }

    @Override
    public void updateQueries(String s, List<QueryUpdate> list) {

    }

    @Override
    public Map<ISpaceId, Space> getSpaces(String s, List<? extends ISpaceId> list, SpaceFetchOptions spaceFetchOptions) {
        return null;
    }

    @Override
    public Map<IProjectId, Project> getProjects(String s, List<? extends IProjectId> list, ProjectFetchOptions projectFetchOptions) {
        return null;
    }

    @Override
    public Map<IExperimentId, Experiment> getExperiments(String s, List<? extends IExperimentId> list, ExperimentFetchOptions experimentFetchOptions) {
        return null;
    }

    @Override
    public Map<IEntityTypeId, ExperimentType> getExperimentTypes(String s, List<? extends IEntityTypeId> list, ExperimentTypeFetchOptions experimentTypeFetchOptions) {
        return null;
    }

    @Override
    public Map<ISampleId, Sample> getSamples(String s, List<? extends ISampleId> list, SampleFetchOptions sampleFetchOptions) {
        return null;
    }

    @Override
    public Map<IEntityTypeId, SampleType> getSampleTypes(String s, List<? extends IEntityTypeId> list, SampleTypeFetchOptions sampleTypeFetchOptions) {
        return null;
    }

    @Override
    public Map<IDataSetId, DataSet> getDataSets(String s, List<? extends IDataSetId> list, DataSetFetchOptions dataSetFetchOptions) {
        return null;
    }

    @Override
    public Map<IEntityTypeId, DataSetType> getDataSetTypes(String s, List<? extends IEntityTypeId> list, DataSetTypeFetchOptions dataSetTypeFetchOptions) {
        return null;
    }

    @Override
    public Map<IMaterialId, Material> getMaterials(String s, List<? extends IMaterialId> list, MaterialFetchOptions materialFetchOptions) {
        return null;
    }

    @Override
    public Map<IEntityTypeId, MaterialType> getMaterialTypes(String s, List<? extends IEntityTypeId> list, MaterialTypeFetchOptions materialTypeFetchOptions) {
        return null;
    }

    @Override
    public Map<IPropertyTypeId, PropertyType> getPropertyTypes(String s, List<? extends IPropertyTypeId> list, PropertyTypeFetchOptions propertyTypeFetchOptions) {
        return null;
    }

    @Override
    public Map<IPluginId, Plugin> getPlugins(String s, List<? extends IPluginId> list, PluginFetchOptions pluginFetchOptions) {
        return null;
    }

    @Override
    public Map<IVocabularyId, Vocabulary> getVocabularies(String s, List<? extends IVocabularyId> list, VocabularyFetchOptions vocabularyFetchOptions) {
        return null;
    }

    @Override
    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String s, List<? extends IVocabularyTermId> list, VocabularyTermFetchOptions vocabularyTermFetchOptions) {
        return null;
    }

    @Override
    public Map<ITagId, Tag> getTags(String s, List<? extends ITagId> list, TagFetchOptions tagFetchOptions) {
        return null;
    }

    @Override
    public Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(String s, List<? extends IAuthorizationGroupId> list, AuthorizationGroupFetchOptions authorizationGroupFetchOptions) {
        return null;
    }

    @Override
    public Map<IRoleAssignmentId, RoleAssignment> getRoleAssignments(String s, List<? extends IRoleAssignmentId> list, RoleAssignmentFetchOptions roleAssignmentFetchOptions) {
        return null;
    }

    @Override
    public Map<IPersonId, Person> getPersons(String s, List<? extends IPersonId> list, PersonFetchOptions personFetchOptions) {
        return null;
    }

    @Override
    public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(String s, List<? extends IExternalDmsId> list, ExternalDmsFetchOptions externalDmsFetchOptions) {
        return null;
    }

    @Override
    public Map<ISemanticAnnotationId, SemanticAnnotation> getSemanticAnnotations(String s, List<? extends ISemanticAnnotationId> list, SemanticAnnotationFetchOptions semanticAnnotationFetchOptions) {
        return null;
    }

    @Override
    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(String s, List<? extends IOperationExecutionId> list, OperationExecutionFetchOptions operationExecutionFetchOptions) {
        return null;
    }

    @Override
    public Map<IQueryId, Query> getQueries(String s, List<? extends IQueryId> list, QueryFetchOptions queryFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Space> searchSpaces(String s, SpaceSearchCriteria spaceSearchCriteria, SpaceFetchOptions spaceFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Project> searchProjects(String s, ProjectSearchCriteria projectSearchCriteria, ProjectFetchOptions projectFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Experiment> searchExperiments(String s, ExperimentSearchCriteria experimentSearchCriteria, ExperimentFetchOptions experimentFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<ExperimentType> searchExperimentTypes(String s, ExperimentTypeSearchCriteria experimentTypeSearchCriteria, ExperimentTypeFetchOptions experimentTypeFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Sample> searchSamples(String s, SampleSearchCriteria sampleSearchCriteria, SampleFetchOptions sampleFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<SampleType> searchSampleTypes(String s, SampleTypeSearchCriteria sampleTypeSearchCriteria, SampleTypeFetchOptions sampleTypeFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<DataSet> searchDataSets(String s, DataSetSearchCriteria dataSetSearchCriteria, DataSetFetchOptions dataSetFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<DataSetType> searchDataSetTypes(String s, DataSetTypeSearchCriteria dataSetTypeSearchCriteria, DataSetTypeFetchOptions dataSetTypeFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Material> searchMaterials(String s, MaterialSearchCriteria materialSearchCriteria, MaterialFetchOptions materialFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<ExternalDms> searchExternalDataManagementSystems(String s, ExternalDmsSearchCriteria externalDmsSearchCriteria, ExternalDmsFetchOptions externalDmsFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<MaterialType> searchMaterialTypes(String s, MaterialTypeSearchCriteria materialTypeSearchCriteria, MaterialTypeFetchOptions materialTypeFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Plugin> searchPlugins(String s, PluginSearchCriteria pluginSearchCriteria, PluginFetchOptions pluginFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Vocabulary> searchVocabularies(String s, VocabularySearchCriteria vocabularySearchCriteria, VocabularyFetchOptions vocabularyFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<VocabularyTerm> searchVocabularyTerms(String s, VocabularyTermSearchCriteria vocabularyTermSearchCriteria, VocabularyTermFetchOptions vocabularyTermFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Tag> searchTags(String s, TagSearchCriteria tagSearchCriteria, TagFetchOptions tagFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<AuthorizationGroup> searchAuthorizationGroups(String s, AuthorizationGroupSearchCriteria authorizationGroupSearchCriteria, AuthorizationGroupFetchOptions authorizationGroupFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<RoleAssignment> searchRoleAssignments(String s, RoleAssignmentSearchCriteria roleAssignmentSearchCriteria, RoleAssignmentFetchOptions roleAssignmentFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Person> searchPersons(String s, PersonSearchCriteria personSearchCriteria, PersonFetchOptions personFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<CustomASService> searchCustomASServices(String s, CustomASServiceSearchCriteria customASServiceSearchCriteria, CustomASServiceFetchOptions customASServiceFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<SearchDomainService> searchSearchDomainServices(String s, SearchDomainServiceSearchCriteria searchDomainServiceSearchCriteria, SearchDomainServiceFetchOptions searchDomainServiceFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<AggregationService> searchAggregationServices(String s, AggregationServiceSearchCriteria aggregationServiceSearchCriteria, AggregationServiceFetchOptions aggregationServiceFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<ReportingService> searchReportingServices(String s, ReportingServiceSearchCriteria reportingServiceSearchCriteria, ReportingServiceFetchOptions reportingServiceFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<ProcessingService> searchProcessingServices(String s, ProcessingServiceSearchCriteria processingServiceSearchCriteria, ProcessingServiceFetchOptions processingServiceFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<ObjectKindModification> searchObjectKindModifications(String s, ObjectKindModificationSearchCriteria objectKindModificationSearchCriteria, ObjectKindModificationFetchOptions objectKindModificationFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<GlobalSearchObject> searchGlobally(String s, GlobalSearchCriteria globalSearchCriteria, GlobalSearchObjectFetchOptions globalSearchObjectFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<OperationExecution> searchOperationExecutions(String s, OperationExecutionSearchCriteria operationExecutionSearchCriteria, OperationExecutionFetchOptions operationExecutionFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<DataStore> searchDataStores(String s, DataStoreSearchCriteria dataStoreSearchCriteria, DataStoreFetchOptions dataStoreFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<SemanticAnnotation> searchSemanticAnnotations(String s, SemanticAnnotationSearchCriteria semanticAnnotationSearchCriteria, SemanticAnnotationFetchOptions semanticAnnotationFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<PropertyType> searchPropertyTypes(String s, PropertyTypeSearchCriteria propertyTypeSearchCriteria, PropertyTypeFetchOptions propertyTypeFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<PropertyAssignment> searchPropertyAssignments(String s, PropertyAssignmentSearchCriteria propertyAssignmentSearchCriteria, PropertyAssignmentFetchOptions propertyAssignmentFetchOptions) {
        return null;
    }

    @Override
    public SearchResult<Query> searchQueries(String s, QuerySearchCriteria querySearchCriteria, QueryFetchOptions queryFetchOptions) {
        return null;
    }

    @Override
    public void deleteSpaces(String s, List<? extends ISpaceId> list, SpaceDeletionOptions spaceDeletionOptions) {

    }

    @Override
    public void deleteProjects(String s, List<? extends IProjectId> list, ProjectDeletionOptions projectDeletionOptions) {

    }

    @Override
    public IDeletionId deleteExperiments(String s, List<? extends IExperimentId> list, ExperimentDeletionOptions experimentDeletionOptions) {
        return null;
    }

    @Override
    public IDeletionId deleteSamples(String s, List<? extends ISampleId> list, SampleDeletionOptions sampleDeletionOptions) {
        return null;
    }

    @Override
    public IDeletionId deleteDataSets(String s, List<? extends IDataSetId> list, DataSetDeletionOptions dataSetDeletionOptions) {
        return null;
    }

    @Override
    public void deleteMaterials(String s, List<? extends IMaterialId> list, MaterialDeletionOptions materialDeletionOptions) {

    }

    @Override
    public void deletePlugins(String s, List<? extends IPluginId> list, PluginDeletionOptions pluginDeletionOptions) {

    }

    @Override
    public void deletePropertyTypes(String s, List<? extends IPropertyTypeId> list, PropertyTypeDeletionOptions propertyTypeDeletionOptions) {

    }

    @Override
    public void deleteVocabularies(String s, List<? extends IVocabularyId> list, VocabularyDeletionOptions vocabularyDeletionOptions) {

    }

    @Override
    public void deleteVocabularyTerms(String s, List<? extends IVocabularyTermId> list, VocabularyTermDeletionOptions vocabularyTermDeletionOptions) {

    }

    @Override
    public void deleteExperimentTypes(String s, List<? extends IEntityTypeId> list, ExperimentTypeDeletionOptions experimentTypeDeletionOptions) {

    }

    @Override
    public void deleteSampleTypes(String s, List<? extends IEntityTypeId> list, SampleTypeDeletionOptions sampleTypeDeletionOptions) {

    }

    @Override
    public void deleteDataSetTypes(String s, List<? extends IEntityTypeId> list, DataSetTypeDeletionOptions dataSetTypeDeletionOptions) {

    }

    @Override
    public void deleteMaterialTypes(String s, List<? extends IEntityTypeId> list, MaterialTypeDeletionOptions materialTypeDeletionOptions) {

    }

    @Override
    public void deleteExternalDataManagementSystems(String s, List<? extends IExternalDmsId> list, ExternalDmsDeletionOptions externalDmsDeletionOptions) {

    }

    @Override
    public void deleteTags(String s, List<? extends ITagId> list, TagDeletionOptions tagDeletionOptions) {

    }

    @Override
    public void deleteAuthorizationGroups(String s, List<? extends IAuthorizationGroupId> list, AuthorizationGroupDeletionOptions authorizationGroupDeletionOptions) {

    }

    @Override
    public void deleteRoleAssignments(String s, List<? extends IRoleAssignmentId> list, RoleAssignmentDeletionOptions roleAssignmentDeletionOptions) {

    }

    @Override
    public void deleteOperationExecutions(String s, List<? extends IOperationExecutionId> list, OperationExecutionDeletionOptions operationExecutionDeletionOptions) {

    }

    @Override
    public void deleteSemanticAnnotations(String s, List<? extends ISemanticAnnotationId> list, SemanticAnnotationDeletionOptions semanticAnnotationDeletionOptions) {

    }

    @Override
    public void deleteQueries(String s, List<? extends IQueryId> list, QueryDeletionOptions queryDeletionOptions) {

    }

    @Override
    public SearchResult<Deletion> searchDeletions(String s, DeletionSearchCriteria deletionSearchCriteria, DeletionFetchOptions deletionFetchOptions) {
        return null;
    }

    @Override
    public void revertDeletions(String s, List<? extends IDeletionId> list) {

    }

    @Override
    public void confirmDeletions(String s, List<? extends IDeletionId> list) {

    }

    @Override
    public Object executeCustomASService(String s, ICustomASServiceId iCustomASServiceId, CustomASServiceExecutionOptions customASServiceExecutionOptions) {
        return null;
    }

    @Override
    public SearchResult<SearchDomainServiceExecutionResult> executeSearchDomainService(String s, SearchDomainServiceExecutionOptions searchDomainServiceExecutionOptions) {
        return null;
    }

    @Override
    public TableModel executeAggregationService(String s, IDssServiceId iDssServiceId, AggregationServiceExecutionOptions aggregationServiceExecutionOptions) {
        return null;
    }

    @Override
    public TableModel executeReportingService(String s, IDssServiceId iDssServiceId, ReportingServiceExecutionOptions reportingServiceExecutionOptions) {
        return null;
    }

    @Override
    public void executeProcessingService(String s, IDssServiceId iDssServiceId, ProcessingServiceExecutionOptions processingServiceExecutionOptions) {

    }

    @Override
    public TableModel executeQuery(String s, IQueryId iQueryId, QueryExecutionOptions queryExecutionOptions) {
        return null;
    }

    @Override
    public TableModel executeSql(String s, String s1, SqlExecutionOptions sqlExecutionOptions) {
        return null;
    }

    @Override
    public void archiveDataSets(String s, List<? extends IDataSetId> list, DataSetArchiveOptions dataSetArchiveOptions) {

    }

    @Override
    public void unarchiveDataSets(String s, List<? extends IDataSetId> list, DataSetUnarchiveOptions dataSetUnarchiveOptions) {

    }

    @Override
    public void lockDataSets(String s, List<? extends IDataSetId> list, DataSetLockOptions dataSetLockOptions) {

    }

    @Override
    public void unlockDataSets(String s, List<? extends IDataSetId> list, DataSetUnlockOptions dataSetUnlockOptions) {

    }

    @Override
    public IOperationExecutionResults executeOperations(String s, List<? extends IOperation> list, IOperationExecutionOptions iOperationExecutionOptions) {
        return null;
    }

    @Override
    public Map<String, String> getServerInformation(String s) {
        return null;
    }

    @Override
    public List<String> createPermIdStrings(String s, int i) {
        return null;
    }

    @Override
    public List<String> createCodes(String s, String s1, EntityKind entityKind, int i) {
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }
}
