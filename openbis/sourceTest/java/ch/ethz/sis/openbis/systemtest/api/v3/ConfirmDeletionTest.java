/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import java.util.Collections;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.DeletionTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class ConfirmDeletionTest extends AbstractDeletionTest
{

    @Test
    public void testConfirmDeletionOfExperimentWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();
        SamplePermId sampleId = createCisdSample(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");

        assertExperimentExists(experimentId);
        assertSampleExists(sampleId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertDeletionExists(deletionId);
        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));

        assertDeletionDoesNotExist(deletionId);
        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);
    }

    @Test
    public void testConfirmDeletionOfSampleWithDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId sampleId = new SamplePermId("200902091225616-1027");
        DataSetPermId dataSetId1 = new DataSetPermId("20081105092159333-3");
        DataSetPermId dataSetId2 = new DataSetPermId("20110805092359990-17");

        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("It is just a test");

        assertSampleExists(sampleId);
        assertDataSetExists(dataSetId1);
        assertDataSetExists(dataSetId2);

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(sampleId), deletionOptions);

        assertDeletionExists(deletionId);
        assertSampleDoesNotExist(sampleId);
        assertDataSetDoesNotExist(dataSetId1);
        assertDataSetDoesNotExist(dataSetId2);

        v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));

        assertDeletionDoesNotExist(deletionId);
        assertSampleDoesNotExist(sampleId);
        assertDataSetDoesNotExist(dataSetId1);
        assertDataSetDoesNotExist(dataSetId2);
    }

    @Test
    public void testConfirmDeletionWithNonexistentDeletion()
    {
        final IDeletionId deletionId = new DeletionTechId(-1L);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_USER, PASSWORD);
                    v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));
                }
            }, deletionId);
    }

    @Test
    public void testConfirmDeletionWithUnauthorizedDeletion()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        final IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken2 = v3api.login(TEST_SPACE_USER, PASSWORD);
                    v3api.confirmDeletions(sessionToken2, Collections.singletonList(deletionId));
                }
            }, deletionId);
    }

}
