/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3;

import java.util.List;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.systemsx.cisd.common.api.IRpcService;

/**
 * @author pkupczyk
 */
public interface IApplicationServerApi extends IRpcService
{
    /**
     * Name of this service for which it is registered as Spring bean
     */
    public static final String INTERNAL_SERVICE_NAME = "application-server_INTERNAL";

    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "application-server";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v3";

    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    public String login(String userId, String password);

    public String loginAs(String userId, String password, String asUserId);

    public void logout(String sessionToken);

    public List<? extends IOperationResult> performOperations(String sessionToken, List<? extends IOperation> operations);

    // REPLACES:
    // - ServiceForDataStoreServer.registerExperiment()

    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments);

    // REPLACES:
    // - ServiceForDataStoreServer.registerSamples()
    // - ServiceForDataStoreServer.registerSample()

    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples);

    // REPLACES:
    // - ServiceForDataStoreServer.updateExperiment()

    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates);

    // REPLACES:
    // - ServiceForDataStoreServer.updateSamples()
    // - ServiceForDataStoreServer.updateSample()

    public void updateSamples(String sessionToken, List<SampleUpdate> sampleUpdates);

    // REPLACES:
    // - ServiceForDataStoreServer.tryGetExperiment(ExperimentIdentifier)
    // - ServiceForDataStoreServer.listExperiments(List<ExperimentIdentifier>, ExperimentFetchOptions)
    // - GeneralInformationService.listExperiments(List<String> experimentIdentifiers)

    public List<Experiment> listExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions);

    // REPLACES:
    // - ServiceForDataStoreServer.tryGetSampleWithExperiment(SampleIdentifier)
    // - ServiceForDataStoreServer.listSamplesByPermId(List<String>)
    // - ServiceForDataStoreServer.tryGetPropertiesOfSample(SampleIdentifier)

    public List<Sample> listSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions);

    // REPLACES:
    // - ServiceForDataStoreServer.listExperimentsForProjects(List<ProjectIdentifier>, ExperimentFetchOptions)
    // - ServiceForDataStoreServer.listExperiments(ProjectIdentifier)
    // - GeneralInformationService.listExperiments(List<Project>, String experimentType)
    // - GeneralInformationService.listExperimentsHavingDataSets(List<Project>, String experimentType) - TODO add "HAVING_DATASETS" criteria
    // - GeneralInformationService.listExperimentsHavingSamples(List<Project>, String experimentType) - TODO add "HAVING_SAMPLES" criteria
    // - GeneralInformationService.searchForExperiments(SearchCriteria)

    public List<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriterion searchCriterion, ExperimentFetchOptions fetchOptions);

    // REPLACES:
    // - ServiceForDataStoreServer.listSamples(ListSampleCriteria)
    // - ServiceForDataStoreServer.listSamplesByCriteria(ListSamplesByPropertyCriteria)
    // - ServiceForDataStoreServer.searchForSamples(SearchCriteria)
    // - GeneralInformationService.searchForSamples(SearchCriteria)
    // - GeneralInformationService.searchForSamples(SearchCriteria, EnumSet<SampleFetchOption>)
    // - GeneralInformationService.listSamplesForExperiment(String experimentIdentifier)

    public List<Sample> searchSamples(String sessionToken, SampleSearchCriterion searchCriterion, SampleFetchOptions fetchOptions);

    // REPLACES:
    // - IGeneralInformationChangingService.deleteExperiments(List<Long>, String, DeletionType)
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions);

    // REPLACES:
    // - IGeneralInformationChangingService.deleteSamples(List<Long>, String, DeletionType)
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions);

    // REPLACES:
    // - IGeneralInformationService.listDeletions(EnumSet<DeletionFetchOption>)
    public List<Deletion> listDeletions(String sessionToken, DeletionFetchOptions fetchOptions);

    // REPLACES:
    // - IGeneralInformationChangingService.revertDeletions(List<Long>)
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds);

    // REPLACES:
    // - IGeneralInformationChangingService.deletePermanently(List<Long>)
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds);

    // NOTES:
    // - initially the new API methods should operate on concrete types (not interfaces) but in the future we want to operate on interfaces only
    // - attachments should be fetched with entities they refer to (e.g. projects, experiments etc.) when appropriate fetch option is set
    // - we should replace generic SearchCriteria with entity specific search criteria, e.g. ExperimentSearchCriteria, SampleSearchCriteria etc. that
    // only have a subset of search parameters which make sense in the context of the given entity kind

    // OPEN QUESTIONS:
    // - shall we still use database instance in our return value filters or shall we validate access only using spaces?
    // - how should we handle "onBehalfOf" methods? Maybe replace the first sessionToken parameter with something like IUserContext where we would
    // have two implementations, UserContext(sessionToken), UserContextOnBehalfOf(sessionToken, userId)?
}
