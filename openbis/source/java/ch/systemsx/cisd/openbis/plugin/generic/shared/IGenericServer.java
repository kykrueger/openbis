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

package ch.systemsx.cisd.openbis.plugin.generic.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.shared.NewSample;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.IPluginCommonServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * Definition of the client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericServer extends IPluginCommonServer
{

    /**
     * For given {@link ExperimentIdentifier} returns the corresponding {@link ExperimentPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public ExperimentPE getExperimentInfo(String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            ExperimentIdentifier identifier);

    /**
     * Returns attachment described by given experiment identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public AttachmentPE getExperimentFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            ExperimentIdentifier experimentIdentifier, String filename, int version)
            throws UserFailureException;

    /**
     * Registers samples in batch.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    public void registerSamples(final String sessionToken, SampleType sampleType,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final List<NewSample> newSamples) throws UserFailureException;
}
