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

package ch.ethz.sis.openbis.generic.server.api.v3;

import java.util.List;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class ApplicationServerApiLogger extends AbstractServerLogger implements
        IApplicationServerApi
{
    public ApplicationServerApiLogger(ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public int getMajorVersion()
    {
        return 3;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public String login(String userId, String password)
    {
        return null;
    }

    @Override
    public String loginAs(String userId, String password, String asUser)
    {
        return null;
    }

    @Override
    public List<? extends IOperationResult> performOperations(String sessionToken, List<? extends IOperation> operations)
    {
        logAccess(sessionToken, "perform-operations", "OPERATIONS(%s)", operations);
        return null;
    }

    @Override
    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments)
    {
        logAccess(sessionToken, "create-experiments", "NEW_EXPERIMENTS(%s)", newExperiments);
        return null;
    }

    @Override
    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples)
    {
        logAccess(sessionToken, "create-samples", "NEW_SAMPLES(%s)", newSamples);
        return null;
    }

    @Override
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates)
    {
        logAccess(sessionToken, "update-experiments", "EXPERIMENT_UPDATES(%s)", experimentUpdates);
    }

    @Override
    public void updateSamples(String sessionToken, List<SampleUpdate> sampleUpdates)
    {
        logAccess(sessionToken, "update-samples", "SAMPLE_UPDATES(%s)", sampleUpdates);
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "list-experiments", "EXPERIMENT_IDS(%s) FETCH_OPTIONS(%s)", experimentIds, fetchOptions);
        return null;
    }

    @Override
    public List<Sample> listSamples(String sessionToken,
            List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "list-samples", "SAMPLE_IDS(%s) FETCH_OPTIONS(%s)", sampleIds, fetchOptions);
        return null;
    }

    @Override
    public List<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriterion searchCriterion, ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-for-experiments", "SEARCH_CRITERION:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriterion, fetchOptions);
        return null;
    }

    @Override
    public List<Sample> searchSamples(String sessionToken, SampleSearchCriterion searchCriterion, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-for-samples", "SEARCH_CRITERION:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriterion, fetchOptions);
        return null;
    }

}
