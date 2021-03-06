/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IGetServerInformationOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.datastore.ISearchDataStoresOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion.IConfirmDeletionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion.IRevertDeletionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion.ISearchDeletionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ICreateCodesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ICreatePermIdsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.event.ISearchEventsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.globalsearch.ISearchGloballyOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.objectkindmodification.ISearchObjectKindModificationsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.delete.IDeleteOperationExecutionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.get.IGetOperationExecutionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.internal.IInternalOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.search.ISearchOperationExecutionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.update.IUpdateOperationExecutionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.rights.IGetRightsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment.ICreateRoleAssignmentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment.IDeleteRoleAssignmentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment.IGetRoleAssignmentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment.ISearchRoleAssignmentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.session.IGetSessionInformationOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.utils.ExceptionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PersistenceException;
import java.util.*;

/**
 * @author pkupczyk
 */
@Component
public class OperationsExecutor implements IOperationsExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IDeleteSpacesOperationExecutor deleteSpacesExecutor;

    @Autowired
    private IDeleteProjectsOperationExecutor deleteProjectsExecutor;

    @Autowired
    private IDeleteExperimentsOperationExecutor deleteExperimentsExecutor;

    @Autowired
    private IDeleteSamplesOperationExecutor deleteSamplesExecutor;

    @Autowired
    private IDeleteDataSetsOperationExecutor deleteDataSetsExecutor;

    @Autowired
    private IDeleteMaterialsOperationExecutor deleteMaterialsExecutor;

    @Autowired
    private IDeleteExternalDmsOperationExecutor deleteExternalDmsExecutor;

    @Autowired
    private IDeleteTagsOperationExecutor deleteTagsExecutor;

    @Autowired
    private IDeleteAuthorizationGroupsOperationExecutor deleteAuthorizationGroupsExecutor;

    @Autowired
    private IDeleteRoleAssignmentsOperationExecutor deleteRoleAssignmentsExecutor;

    @Autowired
    private IDeleteExperimentTypesOperationExecutor deleteExperimentTypesExecutor;

    @Autowired
    private IDeleteSampleTypesOperationExecutor deleteSampleTypesExecutor;

    @Autowired
    private IDeleteDataSetTypesOperationExecutor deleteDataSetTypesExecutor;

    @Autowired
    private IDeleteMaterialTypesOperationExecutor deleteMaterialTypesExecutor;

    @Autowired
    private IDeletePluginsOperationExecutor deletePluginsExecutor;

    @Autowired
    private IDeletePropertyTypesOperationExecutor deletePropertyTypesExecutor;

    @Autowired
    private IDeleteVocabulariesOperationExecutor deleteVocabulariesExecutor;

    @Autowired
    private IDeleteVocabularyTermsOperationExecutor deleteVocabularyTermsExecutor;

    @Autowired
    private IDeleteOperationExecutionsOperationExecutor deleteOperationExecutionsExecutor;

    @Autowired
    private IDeleteSemanticAnnotationsOperationExecutor deleteSemanticAnnotationsExecutor;

    @Autowired
    private IDeleteQueriesOperationExecutor deleteQueriesExecutor;

    @Autowired
    private IDeletePersonsOperationExecutor deletePersonsExecutor;

    @Autowired
    private ICreateSpacesOperationExecutor createSpacesExecutor;

    @Autowired
    private ICreateProjectsOperationExecutor createProjectsExecutor;

    @Autowired
    private ICreateExperimentsOperationExecutor createExperimentsExecutor;

    @Autowired
    private ICreateSamplesOperationExecutor createSamplesExecutor;

    @Autowired
    private ICreateDataSetsOperationExecutor createDataSetsExecutor;

    @Autowired
    private ICreateMaterialsOperationExecutor createMaterialsExecutor;

    @Autowired
    private ICreateTagsOperationExecutor createTagsExecutor;

    @Autowired
    private ICreateAuthorizationGroupsOperationExecutor createAuthorizationGroupsExecutor;

    @Autowired
    private ICreateRoleAssignmentsOperationExecutor createRoleAssignmentsExecutor;

    @Autowired
    private ICreatePersonsOperationExecutor createPersonsExecutor;

    @Autowired
    private ICreateSemanticAnnotationsOperationExecutor createSemanticAnnotationsExecutor;

    @Autowired
    private ICreatePropertyTypesOperationExecutor createPropertyTypesExecutor;

    @Autowired
    private ICreatePluginsOperationExecutor createPluginsExecutor;

    @Autowired
    private ICreateVocabulariesOperationExecutor createVocabulariesExecutor;

    @Autowired
    private ICreateVocabularyTermsOperationExecutor createVocabularyTermsExecutor;

    @Autowired
    private ICreateExternalDmsOperationExecutor createExternalDmsExecutor;

    @Autowired
    private ICreateExperimentTypesOperationExecutor createExperimentTypesExecutor;

    @Autowired
    private ICreateSampleTypesOperationExecutor createSampleTypesExecutor;

    @Autowired
    private ICreateDataSetTypesOperationExecutor createDataSetTypesExecutor;

    @Autowired
    private ICreateMaterialTypesOperationExecutor createMaterialTypesExecutor;

    @Autowired
    private ICreateQueriesOperationExecutor createQueriesExecutor;

    @Autowired
    private ICreateCodesOperationExecutor createCodesExecutor;

    @Autowired
    private ICreatePermIdsOperationExecutor createPermIdsExecutor;

    @Autowired
    private IUpdateSpacesOperationExecutor updateSpacesExecutor;

    @Autowired
    private IUpdateProjectsOperationExecutor updateProjectsExecutor;

    @Autowired
    private IUpdateExperimentsOperationExecutor updateExperimentsExecutor;

    @Autowired
    private IUpdateExperimentTypesOperationExecutor updateExperimentTypesExecutor;

    @Autowired
    private IUpdateSamplesOperationExecutor updateSamplesExecutor;

    @Autowired
    private IUpdateSampleTypesOperationExecutor updateSampleTypesExecutor;

    @Autowired
    private IUpdateDataSetsOperationExecutor updateDataSetsExecutor;

    @Autowired
    private IUpdateDataSetTypesOperationExecutor updateDataSetTypesExecutor;

    @Autowired
    private IUpdateMaterialsOperationExecutor updateMaterialsExecutor;

    @Autowired
    private IUpdateMaterialTypesOperationExecutor updateMaterialTypesExecutor;

    @Autowired
    private IUpdateTagsOperationExecutor updateTagsExecutor;

    @Autowired
    private IUpdateAuthorizationGroupsOperationExecutor updateAuthorizationGroupsExecutor;

    @Autowired
    private IUpdatePersonsOperationExecutor updatePersonsExecutor;

    @Autowired
    private IUpdateExternalDmsOperationExecutor updateExternalDmsExecutor;

    @Autowired
    private IUpdatePropertyTypesOperationExecutor updatePropertyTypesExecutor;

    @Autowired
    private IUpdatePluginsOperationExecutor updatePluginsExecutor;

    @Autowired
    private IUpdateVocabulariesOperationExecutor updateVocabulariesExecutor;

    @Autowired
    private IUpdateVocabularyTermsOperationExecutor updateVocabularyTermsExecutor;

    @Autowired
    private IUpdateOperationExecutionsOperationExecutor updateOperationExecutionsExecutor;

    @Autowired
    private IUpdateSemanticAnnotationsOperationExecutor updateSemanticAnnotationsExecutor;

    @Autowired
    private IUpdateQueriesOperationExecutor updateQueriesExecutor;

    @Autowired
    private IVerifyExperimentsOperationExecutor verifyExperimentsExecutor;

    @Autowired
    private IVerifySamplesOperationExecutor verifySamplesExecutor;

    @Autowired
    private IVerifyDataSetsOperationExecutor verifyDataSetsExecutor;

    @Autowired
    private IVerifyMaterialsOperationExecutor verifyMaterialsExecutor;

    @Autowired
    private IInternalOperationExecutor internalOperationExecutor;

    @Autowired
    private IGetRightsOperationExecutor getRightsExecutor;

    @Autowired
    private IGetSpacesOperationExecutor getSpacesExecutor;

    @Autowired
    private IGetProjectsOperationExecutor getProjectsExecutor;

    @Autowired
    private IGetExperimentsOperationExecutor getExperimentsExecutor;

    @Autowired
    private IGetExperimentTypesOperationExecutor getExperimentTypesExecutor;

    @Autowired
    private IGetSamplesOperationExecutor getSamplesExecutor;

    @Autowired
    private IGetSampleTypesOperationExecutor getSampleTypesExecutor;

    @Autowired
    private IGetDataSetsOperationExecutor getDataSetsExecutor;

    @Autowired
    private IGetDataSetTypesOperationExecutor getDataSetTypesExecutor;

    @Autowired
    private IGetMaterialsOperationExecutor getMaterialsExecutor;

    @Autowired
    private IGetMaterialTypesOperationExecutor getMaterialTypesExecutor;

    @Autowired
    private IGetTagsOperationExecutor getTagsExecutor;

    @Autowired
    private IGetAuthorizationGroupsOperationExecutor getAuthorizationGroupsExecutor;

    @Autowired
    private IGetRoleAssignmentsOperationExecutor getRoleAssignmentsExecutor;

    @Autowired
    private IGetPersonsOperationExecutor getPersonsExecutor;

    @Autowired
    private IGetPropertyTypesOperationExecutor getPropertyTypesExecutor;

    @Autowired
    private IGetPluginsOperationExecutor getPluginsExecutor;

    @Autowired
    private IGetVocabulariesOperationExecutor getVocabulariesExecutor;

    @Autowired
    private IGetVocabularyTermsOperationExecutor getVocabularyTermsExecutor;

    @Autowired
    private IGetExternalDmsOperationExecutor getExternalDmsExecutor;

    @Autowired
    private IGetOperationExecutionsOperationExecutor getOperationExecutionsExecutor;

    @Autowired
    private IGetSemanticAnnotationsOperationExecutor getSemanticAnnotationsExecutor;

    @Autowired
    private IGetQueriesOperationExecutor getQueriesExecutor;

    @Autowired
    private IGetQueryDatabasesOperationExecutor getQueryDatabasesExecutor;

    @Autowired
    private IGetServerInformationOperationExecutor getServerInformationExecutor;

    @Autowired
    private ISearchSpacesOperationExecutor searchSpacesExecutor;

    @Autowired
    private ISearchProjectsOperationExecutor searchProjectsExecutor;

    @Autowired
    private ISearchExperimentsOperationExecutor searchExperimentsExecutor;

    @Autowired
    private ISearchSamplesOperationExecutor searchSamplesExecutor;

    @Autowired
    private ISearchDataSetsOperationExecutor searchDataSetsExecutor;

    @Autowired
    private ISearchMaterialsOperationExecutor searchMaterialsExecutor;

    @Autowired
    private ISearchTagsOperationExecutor searchTagsExecutor;

    @Autowired
    private ISearchAuthorizationGroupsOperationExecutor searchAuthorizationGroupsExecutor;

    @Autowired
    private ISearchRoleAssignmentsOperationExecutor searchRoleAssignmentsExecutor;

    @Autowired
    private ISearchPersonsOperationExecutor searchPersonsExecutor;

    @Autowired
    private ISearchExternalDmsOperationExecutor searchExternalDmsExecutor;

    @Autowired
    private ISearchPluginsOperationExecutor searchPluginsExecutor;

    @Autowired
    private ISearchVocabulariesOperationExecutor searchVocabulariesExecutor;

    @Autowired
    private ISearchVocabularyTermsOperationExecutor searchVocabularyTermsExecutor;

    @Autowired
    private ISearchExperimentTypesOperationExecutor searchExperimentTypesExecutor;

    @Autowired
    private ISearchSampleTypesOperationExecutor searchSampleTypesExecutor;

    @Autowired
    private ISearchDataSetTypesOperationExecutor searchDataSetTypesExecutor;

    @Autowired
    private ISearchMaterialTypesOperationExecutor searchMaterialTypesExecutor;

    @Autowired
    private ISearchCustomASServicesOperationExecutor searchCustomASServicesExecutor;

    @Autowired
    private ISearchSearchDomainServicesOperationExecutor searchSearchDomainServicesExecutor;

    @Autowired
    private ISearchAggregationServicesOperationExecutor searchAggregationServicesExecutor;

    @Autowired
    private ISearchReportingServicesOperationExecutor searchReportingServicesExecutor;

    @Autowired
    private ISearchProcessingServicesOperationExecutor searchProcessingServicesExecutor;

    @Autowired
    private ISearchDeletionsOperationExecutor searchDeletionsExecutor;

    @Autowired
    private ISearchEventsOperationExecutor searchEventsExecutor;

    @Autowired
    private ISearchGloballyOperationExecutor searchGloballyExecutor;

    @Autowired
    private ISearchObjectKindModificationsOperationExecutor searchObjectKindModificationsExecutor;

    @Autowired
    private ISearchOperationExecutionsOperationExecutor searchOperationExecutionsExecutor;

    @Autowired
    private ISearchDataStoresOperationExecutor searchDataStoresExecutionsExecutor;

    @Autowired
    private ISearchSemanticAnnotationsOperationExecutor searchSemanticAnnotationsExecutor;

    @Autowired
    private ISearchPropertyTypesOperationExecutor searchPropertyTypesExecutor;

    @Autowired
    private ISearchPropertyAssignmentsOperationExecutor searchPropertyAssignmentsExecutor;

    @Autowired
    private ISearchQueriesOperationExecutor searchQueriesExecutor;

    @Autowired
    private ISearchQueryDatabasesOperationExecutor searchQueryDatabasesExecutor;

    @Autowired
    private IExecuteCustomASServiceOperationExecutor executeCustomASServiceExecutor;

    @Autowired
    private IExecuteAggregationServiceOperationExecutor executeAggregationServiceExecutor;

    @Autowired
    private IExecuteReportingServiceOperationExecutor executeReportingServiceExecutor;

    @Autowired
    private IExecuteProcessingServiceOperationExecutor executeProcessingServiceExecutor;

    @Autowired
    private IExecuteSearchDomainServiceOperationExecutor executeSearchDomainServiceExecutor;

    @Autowired
    private IExecuteQueryOperationExecutor executeQueryExecutor;

    @Autowired
    private IExecuteSqlOperationExecutor executeSqlExecutor;

    @Autowired
    private IEvaluatePluginOperationExecutor evaluatePluginExecutor;

    @Autowired
    private IRevertDeletionsOperationExecutor revertDeletionsExecutor;

    @Autowired
    private IConfirmDeletionsOperationExecutor confirmDeletionsExecutor;

    @Autowired
    private IArchiveDataSetsOperationExecutor archiveDataSetsExecutor;

    @Autowired
    private IUnarchiveDataSetsOperationExecutor unarchiveDataSetsExecutor;

    @Autowired
    private ILockDataSetsOperationExecutor lockDataSetsExecutor;

    @Autowired
    private IUnlockDataSetsOperationExecutor unlockDataSetsExecutor;

    @Autowired
    private IGetSessionInformationOperationExecutor getSessionInformationExecutor;

    @Override
    public List<IOperationResult> execute(IOperationContext context, List<? extends IOperation> operations, IOperationExecutionOptions options)
    {
        if (options != null && options.isExecuteInOrder())
        {
            List<IOperationResult> results = new ArrayList<IOperationResult>();

            for (IOperation operation : operations)
            {
                List<IOperationResult> result = doExecute(context, Collections.singletonList(operation), options);
                results.add(result.get(0));
            }

            return results;
        } else
        {
            return doExecute(context, operations, options);
        }
    }

    private List<IOperationResult> doExecute(IOperationContext context, List<? extends IOperation> operations, IOperationExecutionOptions options)
    {
        Map<IOperation, IOperationResult> resultMap = new HashMap<IOperation, IOperationResult>();

        try
        {
            executeDeletions(operations, resultMap, context);
            executeCreations(operations, resultMap, context);
            executeUpdates(operations, resultMap, context);
            resultMap.putAll(internalOperationExecutor.execute(context, operations));

            flushCurrentSession();
            clearCurrentSession();

            verify(operations, resultMap, context);

            executeGets(operations, resultMap, context);
            executeSearches(operations, resultMap, context);
            executeOthers(operations, resultMap, context);

            List<IOperationResult> resultList = new ArrayList<IOperationResult>();
            for (IOperation operation : operations)
            {
                resultList.add(resultMap.get(operation));
            }

            return resultList;
        } catch (Throwable e)
        {
            throw ExceptionUtils.create(context, e);
        } finally
        {
            clearCurrentSession();
        }
    }

    private void executeOthers(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(executeCustomASServiceExecutor.execute(context, operations));
        resultMap.putAll(executeAggregationServiceExecutor.execute(context, operations));
        resultMap.putAll(executeProcessingServiceExecutor.execute(context, operations));
        resultMap.putAll(executeReportingServiceExecutor.execute(context, operations));
        resultMap.putAll(executeSearchDomainServiceExecutor.execute(context, operations));
        resultMap.putAll(revertDeletionsExecutor.execute(context, operations));
        resultMap.putAll(confirmDeletionsExecutor.execute(context, operations));
        resultMap.putAll(lockDataSetsExecutor.execute(context, operations));
        resultMap.putAll(unlockDataSetsExecutor.execute(context, operations));
        resultMap.putAll(archiveDataSetsExecutor.execute(context, operations));
        resultMap.putAll(unarchiveDataSetsExecutor.execute(context, operations));
        resultMap.putAll(getSessionInformationExecutor.execute(context, operations));
        resultMap.putAll(executeQueryExecutor.execute(context, operations));
        resultMap.putAll(executeSqlExecutor.execute(context, operations));
        resultMap.putAll(evaluatePluginExecutor.execute(context, operations));
    }

    private void executeSearches(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(searchSpacesExecutor.execute(context, operations));
        resultMap.putAll(searchProjectsExecutor.execute(context, operations));
        resultMap.putAll(searchExperimentsExecutor.execute(context, operations));
        resultMap.putAll(searchSamplesExecutor.execute(context, operations));
        resultMap.putAll(searchDataSetsExecutor.execute(context, operations));
        resultMap.putAll(searchMaterialsExecutor.execute(context, operations));
        resultMap.putAll(searchTagsExecutor.execute(context, operations));
        resultMap.putAll(searchAuthorizationGroupsExecutor.execute(context, operations));
        resultMap.putAll(searchRoleAssignmentsExecutor.execute(context, operations));
        resultMap.putAll(searchPersonsExecutor.execute(context, operations));
        resultMap.putAll(searchExternalDmsExecutor.execute(context, operations));
        resultMap.putAll(searchPluginsExecutor.execute(context, operations));
        resultMap.putAll(searchVocabulariesExecutor.execute(context, operations));
        resultMap.putAll(searchVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(searchExperimentTypesExecutor.execute(context, operations));
        resultMap.putAll(searchSampleTypesExecutor.execute(context, operations));
        resultMap.putAll(searchDataSetTypesExecutor.execute(context, operations));
        resultMap.putAll(searchMaterialTypesExecutor.execute(context, operations));
        resultMap.putAll(searchCustomASServicesExecutor.execute(context, operations));
        resultMap.putAll(searchAggregationServicesExecutor.execute(context, operations));
        resultMap.putAll(searchReportingServicesExecutor.execute(context, operations));
        resultMap.putAll(searchProcessingServicesExecutor.execute(context, operations));
        resultMap.putAll(searchSearchDomainServicesExecutor.execute(context, operations));
        resultMap.putAll(searchDeletionsExecutor.execute(context, operations));
        resultMap.putAll(searchEventsExecutor.execute(context, operations));
        resultMap.putAll(searchGloballyExecutor.execute(context, operations));
        resultMap.putAll(searchObjectKindModificationsExecutor.execute(context, operations));
        resultMap.putAll(searchOperationExecutionsExecutor.execute(context, operations));
        resultMap.putAll(searchDataStoresExecutionsExecutor.execute(context, operations));
        resultMap.putAll(searchSemanticAnnotationsExecutor.execute(context, operations));
        resultMap.putAll(searchPropertyTypesExecutor.execute(context, operations));
        resultMap.putAll(searchPropertyAssignmentsExecutor.execute(context, operations));
        resultMap.putAll(searchQueriesExecutor.execute(context, operations));
        resultMap.putAll(searchQueryDatabasesExecutor.execute(context, operations));
    }

    private void executeGets(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(getRightsExecutor.execute(context, operations));
        resultMap.putAll(getSpacesExecutor.execute(context, operations));
        resultMap.putAll(getProjectsExecutor.execute(context, operations));
        resultMap.putAll(getExperimentsExecutor.execute(context, operations));
        resultMap.putAll(getExperimentTypesExecutor.execute(context, operations));
        resultMap.putAll(getSamplesExecutor.execute(context, operations));
        resultMap.putAll(getSampleTypesExecutor.execute(context, operations));
        resultMap.putAll(getDataSetsExecutor.execute(context, operations));
        resultMap.putAll(getDataSetTypesExecutor.execute(context, operations));
        resultMap.putAll(getMaterialsExecutor.execute(context, operations));
        resultMap.putAll(getMaterialTypesExecutor.execute(context, operations));
        resultMap.putAll(getTagsExecutor.execute(context, operations));
        resultMap.putAll(getAuthorizationGroupsExecutor.execute(context, operations));
        resultMap.putAll(getRoleAssignmentsExecutor.execute(context, operations));
        resultMap.putAll(getPersonsExecutor.execute(context, operations));
        resultMap.putAll(getPropertyTypesExecutor.execute(context, operations));
        resultMap.putAll(getPluginsExecutor.execute(context, operations));
        resultMap.putAll(getVocabulariesExecutor.execute(context, operations));
        resultMap.putAll(getVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(getExternalDmsExecutor.execute(context, operations));
        resultMap.putAll(getOperationExecutionsExecutor.execute(context, operations));
        resultMap.putAll(getSemanticAnnotationsExecutor.execute(context, operations));
        resultMap.putAll(getQueriesExecutor.execute(context, operations));
        resultMap.putAll(getQueryDatabasesExecutor.execute(context, operations));
        resultMap.putAll(getServerInformationExecutor.execute(context, operations));
    }

    private void verify(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        verifyMaterialsExecutor.verify(context, operations, resultMap);
        verifyExperimentsExecutor.verify(context, operations, resultMap);
        verifySamplesExecutor.verify(context, operations, resultMap);
        verifyDataSetsExecutor.verify(context, operations, resultMap);
    }

    private void executeUpdates(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(updateSemanticAnnotationsExecutor.execute(context, operations));
        resultMap.putAll(updateOperationExecutionsExecutor.execute(context, operations));
        resultMap.putAll(updatePluginsExecutor.execute(context, operations));
        resultMap.putAll(updateVocabulariesExecutor.execute(context, operations));
        resultMap.putAll(updatePropertyTypesExecutor.execute(context, operations));
        resultMap.putAll(updateVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(updateMaterialTypesExecutor.execute(context, operations));
        resultMap.putAll(updateExperimentTypesExecutor.execute(context, operations));
        resultMap.putAll(updateSampleTypesExecutor.execute(context, operations));
        resultMap.putAll(updateDataSetTypesExecutor.execute(context, operations));
        resultMap.putAll(updateTagsExecutor.execute(context, operations));
        resultMap.putAll(updatePersonsExecutor.execute(context, operations));
        resultMap.putAll(updateAuthorizationGroupsExecutor.execute(context, operations));
        resultMap.putAll(updateMaterialsExecutor.execute(context, operations));
        resultMap.putAll(updateExternalDmsExecutor.execute(context, operations));
        resultMap.putAll(updateSpacesExecutor.execute(context, operations));
        resultMap.putAll(updateProjectsExecutor.execute(context, operations));
        resultMap.putAll(updateExperimentsExecutor.execute(context, operations));
        resultMap.putAll(updateSamplesExecutor.execute(context, operations));
        resultMap.putAll(updateDataSetsExecutor.execute(context, operations));
        resultMap.putAll(updateQueriesExecutor.execute(context, operations));
    }

    private void executeCreations(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(createPersonsExecutor.execute(context, operations));
        resultMap.putAll(createPluginsExecutor.execute(context, operations));
        resultMap.putAll(createVocabulariesExecutor.execute(context, operations));
        resultMap.putAll(createVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(createPropertyTypesExecutor.execute(context, operations));
        resultMap.putAll(createExperimentTypesExecutor.execute(context, operations));
        resultMap.putAll(createSampleTypesExecutor.execute(context, operations));
        resultMap.putAll(createDataSetTypesExecutor.execute(context, operations));
        resultMap.putAll(createMaterialTypesExecutor.execute(context, operations));
        resultMap.putAll(createMaterialsExecutor.execute(context, operations));
        resultMap.putAll(createSpacesExecutor.execute(context, operations));
        resultMap.putAll(createProjectsExecutor.execute(context, operations));
        resultMap.putAll(createExperimentsExecutor.execute(context, operations));
        resultMap.putAll(createSamplesExecutor.execute(context, operations));
        resultMap.putAll(createExternalDmsExecutor.execute(context, operations));
        resultMap.putAll(createDataSetsExecutor.execute(context, operations));
        resultMap.putAll(createSemanticAnnotationsExecutor.execute(context, operations));
        resultMap.putAll(createTagsExecutor.execute(context, operations));
        resultMap.putAll(createAuthorizationGroupsExecutor.execute(context, operations));
        resultMap.putAll(createRoleAssignmentsExecutor.execute(context, operations));
        resultMap.putAll(createQueriesExecutor.execute(context, operations));
        resultMap.putAll(createCodesExecutor.execute(context, operations));
        resultMap.putAll(createPermIdsExecutor.execute(context, operations));
    }

    private void executeDeletions(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(deleteQueriesExecutor.execute(context, operations));
        resultMap.putAll(deleteSemanticAnnotationsExecutor.execute(context, operations));
        resultMap.putAll(deleteExperimentsExecutor.execute(context, operations));
        resultMap.putAll(deleteSamplesExecutor.execute(context, operations));
        resultMap.putAll(deleteDataSetsExecutor.execute(context, operations));
        resultMap.putAll(deleteProjectsExecutor.execute(context, operations));
        resultMap.putAll(deleteSpacesExecutor.execute(context, operations));
        resultMap.putAll(deleteMaterialsExecutor.execute(context, operations));
        resultMap.putAll(deleteExternalDmsExecutor.execute(context, operations));
        resultMap.putAll(deleteTagsExecutor.execute(context, operations));
        resultMap.putAll(deleteRoleAssignmentsExecutor.execute(context, operations));
        resultMap.putAll(deleteAuthorizationGroupsExecutor.execute(context, operations));
        resultMap.putAll(deleteExperimentTypesExecutor.execute(context, operations));
        resultMap.putAll(deleteSampleTypesExecutor.execute(context, operations));
        resultMap.putAll(deleteDataSetTypesExecutor.execute(context, operations));
        resultMap.putAll(deleteMaterialTypesExecutor.execute(context, operations));
        resultMap.putAll(deletePropertyTypesExecutor.execute(context, operations));
        resultMap.putAll(deletePluginsExecutor.execute(context, operations));
        resultMap.putAll(deleteVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(deleteVocabulariesExecutor.execute(context, operations));
        resultMap.putAll(deleteOperationExecutionsExecutor.execute(context, operations));
        resultMap.putAll(deletePersonsExecutor.execute(context, operations));
    }

    protected void clearCurrentSession()
    {
        daoFactory.getSessionFactory().getCurrentSession().clear();
    }

    protected void flushCurrentSession()
    {
        try
        {
            daoFactory.getSessionFactory().getCurrentSession().flush();
        } catch (PersistenceException e)
        {
            Throwable endOfChain = ch.systemsx.cisd.common.exceptions.ExceptionUtils.getEndOfChain(e);
            throw new UserFailureException(endOfChain.getMessage(), endOfChain);
        }
    }

}
