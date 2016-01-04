/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.Collection;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleGenericBusinessRules;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class VerifySampleContainerExecutor implements IVerifySampleContainerExecutor
{

    @Override
    public void verify(IOperationContext context, Collection<SamplePE> samples)
    {
        for (SamplePE sample : samples)
        {
            SamplePE containerCandidate = sample.getContainer();

            while (containerCandidate != null)
            {
                if (sample.equals(containerCandidate))
                {
                    throw UserFailureException.fromTemplate("'%s' cannot be it's own container.",
                            sample.getIdentifier());
                }
                containerCandidate = containerCandidate.getContainer();
            }

            SampleGenericBusinessRules.assertValidContainer(sample);
            SampleGenericBusinessRules.assertValidComponents(sample);
        }
    }

}