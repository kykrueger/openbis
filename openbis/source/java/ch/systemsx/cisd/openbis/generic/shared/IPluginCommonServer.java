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

package ch.systemsx.cisd.openbis.generic.shared;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Common interface for all plugin client-server interfaces.
 *
 * @author Franz-Josef Elmer
 */
public interface IPluginCommonServer extends IServer
{

    /**
     * For given <var>sampleIdentifier</var> returns the {@link SamplePE} and its children.
     * 
     * @return never <code>null</code>.
     * @throws UserFailureException if given <var>sessionToken</var> is invalid or whether sample
     *             uniquely identified by given <var>sampleIdentifier</var> does not exist.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public SampleGenerationDTO getSampleInfo(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /**
     * Registers a new sample.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    public void registerSample(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample);

}
